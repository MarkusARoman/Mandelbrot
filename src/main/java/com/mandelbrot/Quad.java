package com.mandelbrot;


import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Class for creating a 2D model with 4 vertices and 2 triangles in float format.
 **/
public class Quad
{
    // The model is a square with 4 vertices
    public float[] VERTICES =
    {
             // Pos         // Tex
            -1f, -1f,       0f, 0f,
             1f, -1f,       1f, 0f,
             1f,  1f,       1f, 1f,
            -1f,  1f,       0f, 1f,
    };

    // The 4 vertices combine into two triangles
    private final int[] INDICES =
    {
            0, 1, 2,
            2, 3, 0,
    };

    // The number of vertices and indices
    public static final int VERTEX_COUNT = 4;
    public static final int INDEX_COUNT = 6;


    // The vertex array object
    private final int VAO;

    // The vertex buffer object
    private final int VBO;

    // The element buffer object
    private final int EBO;

    /**
     * Constructor
     **/
    public Quad()
    {
        // Create the vertex array object
        VAO = glGenVertexArrays();

        // Bind the vertex array object
        VBO = glGenBuffers();

        // Bind the vertex buffer object
        EBO = glGenBuffers();

        // Bind the vertex array object
        glBindVertexArray(VAO);

        // Bind the vertex buffer object
        glBindBuffer(GL_ARRAY_BUFFER, VBO);

        // Copy the vertex data to the vertex buffer object
        glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);

        // Bind the element buffer object
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);

        // Copy the index data to the element buffer object
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, INDICES, GL_STATIC_DRAW);

        // Set the vertex attribute pointers
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Unbind the vertex array object
        glBindVertexArray(0);

        // Unbind the vertex buffer object
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // Unbind the element buffer object
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Render the model
     **/
    public void render()
    {
        // Bind the vertex array object
        glBindVertexArray(VAO);

        // Draw the model
        glDrawElements(GL_TRIANGLES, INDEX_COUNT, GL_UNSIGNED_INT, 0);

        // Unbind the vertex array object
        glBindVertexArray(0);
    }

    /**
     * Delete the model
     **/
    public void delete()
    {
        // Delete the vertex buffer object
        glDeleteBuffers(VBO);

        // Delete the element buffer object
        glDeleteBuffers(EBO);

        // Delete the vertex array object
        glDeleteVertexArrays(VAO);
    }
}
