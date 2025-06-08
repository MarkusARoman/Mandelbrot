package com.mandelbrot;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.stb.STBImage;

public class Main {
    private final Window window;
    private final Shader shader;
    private final Quad quad;

    private int textureId;

    private final int width, height;

    // Mandelbrot state
    private float zoom = 4.0f;
    private float offsetX = 0.0f, offsetY = 0.0f;

    private double lastMouseX, lastMouseY;
    private boolean dragging = false;
    private final float dragSpeed = 1.5f;

    public Main() {
        window = new Window("Mandelbrot Set");
        window.create();

        width = window.getWidth();
        height = window.getHeight();


        glfwSetMouseButtonCallback(window.getHandle(), (windowHandle, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    dragging = true;
                    double[] x = new double[1], y = new double[1];
                    glfwGetCursorPos(windowHandle, x, y);
                    lastMouseX = x[0];
                    lastMouseY = y[0];
                } else if (action == GLFW_RELEASE) {
                    dragging = false;
                }
            }

        });

        glfwSetCursorPosCallback(window.getHandle(), (windowHandle, xpos, ypos) -> {
            if (dragging) {
                double dx = xpos - lastMouseX;
                double dy = ypos - lastMouseY;

                // Normalize by screen size, scale with zoom
                offsetX -= (float)(dx*dragSpeed / height) * zoom;
                offsetY += (float)(dy*dragSpeed / height) * zoom;

                lastMouseX = xpos;
                lastMouseY = ypos;
            }
        });

        glfwSetScrollCallback(window.getHandle(), (windowHandle, xoffset, yoffset) -> {
            float zoomFactor = (float)Math.pow(1.1, -yoffset);
            zoom *= zoomFactor;
        });


        // Shader and Quad
        shader = new Shader("mandelbrot");
        shader.compile();

        quad = new Quad();

        textureId = loadTexture("mandelbrot/src/main/resource/image.jpg");

        loop();

        // Cleanup
        quad.delete();
        shader.delete();
        window.destroy();
    }

    private void loop() {
        while (!window.shouldClose()) {
            glClear(GL_COLOR_BUFFER_BIT);

            shader.use();
            shader.setUniform2f("u_resolution", width, height);
            shader.setUniform1f("u_zoom", zoom);
            shader.setUniform2f("u_offset", offsetX, offsetY);

            // Bind texture unit 0 and bind your texture
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureId);

            // Tell the shader that the sampler2D uniform "u_texture" is texture unit 0
            shader.setUniform1i("u_texture", 0);

            quad.render();

            window.refresh();;
        }
    }

    private int loadTexture(String path) {
        int textureId;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            // Load image file data into a ByteBuffer
            ByteBuffer image = STBImage.stbi_load(path, width, height, channels, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load a texture file!"
                    + System.lineSeparator() + STBImage.stbi_failure_reason());
            }

            // Generate texture ID
            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);

            // Setup texture parameters
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            // Upload texture data to GPU
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width.get(0), height.get(0), 0,
                        GL_RGBA, GL_UNSIGNED_BYTE, image);

            // Free image memory
            STBImage.stbi_image_free(image);
        }
        return textureId;
    }


    public static void main(String[] args) {
        new Main();
    }
}
