package com.mandelbrot;


import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;


/**
 * Note:
 * Window properties are preset in the constructor.
 * Later on, this will be handled by a settings file.
 */


/**
 * Window class for creating and managing a GLFW window.
 */
public class Window 
{
    // Window width
    private int width;

    // Window height
    private int height;


    // Window name
    private String title;


    // Window handle
    private long handle;


    // GLFW initialization state
    private static boolean initialized = false;

    // Window creation state
    private static boolean created = false;


    // --- Constructors --- //


    /**
     * Constructor for creating a window with the primary monitor's resolution.
     * @param title The title of the window.
     */
    public Window(String title) 
    {
        // Ensure GLFW is initialized before creating a window)
        if ( !initialized ) 
            Window.init();


        // Get the video mode (resolution, etc.) for the primary monitor
        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if ( vidMode != null ) {
            this.width = vidMode.width();  // Set window width to the monitor width
            this.height = vidMode.height();  // Set window height to the monitor height
        } 
        else
        {
            throw new IllegalStateException("Unable to get video mode for the primary monitor.");
        }


        // Set the title of the window
        this.title = title;

        // Mark window as not created
        created = false;
    }

    /**
     * Constructor for creating a window with custom dimensions.
     * @param width The width of the window.
     * @param height The height of the window.
     * @param title The title of the window.
     */
    public Window(int width, int height, String title) 
    {
        // Ensure GLFW is initialized before creating a window)
        if ( !initialized ) 
            Window.init();
        

        this.width = width;  // Set the width of the window
        this.height = height;  // Set the height of the window
        this.title = title;  // Set the title of the window

        // Mark window as not created
        created = false;
    }


    // --- Methods --- //


    /**
     * Initializes the GLFW library.
     * This should be called before creating any windows.
     */
    public static void init()
    {
        // Set GLFW error callback to print errors to the standard error stream
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. If it fails, an exception is thrown.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        
        // Set GLFW window hints
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // Window will be hidden initially
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE); // Disable resizing of the window
        glfwWindowHint(GLFW_DECORATED, GL_FALSE); // Disable window decorations (e.g., title bar, borders)

        initialized = true; // Mark GLFW as initialized
    }

    /**
     * Creates a window with the specified width, height, and title.
     */
    public void create()
    {
        // Set the window size and title
        if ( created )
            throw new IllegalStateException("Window has already been created.");

        
        // Create the window
        handle = glfwCreateWindow(width, height, title, NULL, NULL);

        // Check if the window creation failed
        if ( handle == NULL )
            throw new RuntimeException("Failed to create GLFW window");
        

        // Get the thread stack and push a new frame
		try ( MemoryStack stack = MemoryStack.stackPush() ) 
        {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(handle, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				handle,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		}


        // Make the OpenGL context current for this window (so subsequent OpenGL calls affect this window)
        glfwMakeContextCurrent(handle);

        // REQUIRED for OpenGL to work
        org.lwjgl.opengl.GL.createCapabilities();

        // Enable V-Sync (limit frame rate to avoid tearing)
        glfwSwapInterval(1);

        // Set the clear color (background color) to black
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        glEnable(GL_DEPTH_BUFFER_BIT);

        // Make the window visible by showing it
        glfwShowWindow(handle);
        
        created = true; // Mark the window as created

    }

    /**
     * Swaps the front and back buffers and polls for window events.
     * This should be called every frame.
     */
    public void refresh() 
    {
        glfwSwapBuffers(handle);
        glfwPollEvents();
    }

    /**
     * Clears the window
     */
    public void clear()
    {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }


    // --- Checkers --- //


    /**
     * Checks if a specific key is pressed.
     * 
     * @param key The key to check (use GLFW key codes, e.g., GLFW_KEY_W).
     * @return True if the key is pressed, false otherwise.
     */
    public boolean isKeyPressed( int key ) 
    {
        return glfwGetKey(handle, key) == GLFW_PRESS;
    }

    /** 
     * Checks if GLFW window should close
     * 
     * @return True if the window should close, false otherwise. 
    */
    public boolean shouldClose() 
    {
        return glfwWindowShouldClose(handle);
    }


    // --- Getters --- //


    /**
     * Gets the title of the window.
     * @return The title of the window.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the width of the window.
     * @return The width of the window.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the window.
     * @return The height of the window.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the handle of the window.
     * @return The handle of the window.
     */
    public long getHandle() {
        return handle;
    }


    // --- Cleanup --- //


    /**
     * Destroys the window and frees its resources.
     * This should be called when the window is no longer needed.
     */
    public void destroy()
    {
        if ( handle != NULL ) 
        {
            glfwDestroyWindow(handle);
            handle = NULL;
            created = false;
        }
    }

    /**
     * Releases window resources and terminates GLFW.
     * This should be called when the application is exiting.
     */
    public void cleanup() 
    {
        // Check if GLFW was initialized and the window handle is valid.
        if ( initialized ) 
        {
            // Free any GLFW callbacks associated with this window.
            if ( handle != NULL ) 
            {
                glfwSetWindowCloseCallback(handle, null);
                glfwDestroyWindow(handle);
                handle = NULL;
            }

            // Terminate GLFW and free the error callback.
            glfwTerminate();
            glfwSetErrorCallback(null).free();

            initialized = false; // Indicate GLFW is no longer initialized.
        } 
        else 
        {
            System.err.println("Error: GLFW has not been initialized or resources were already cleaned up.");
        }
    }
}
