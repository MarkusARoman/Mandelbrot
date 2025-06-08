package com.mandelbrot;


import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;

public class Shader 
{
    // Shader program identifier
    private int program_ID;

    // Specific shader codes
    private String vertex_shader;
    private String fragment_shader;

    private final Map<String, Integer> uniformCache = new HashMap<>();

    // Toggle uniform not-found warnings
    private boolean verbose = true;
    public void setVerbose(boolean verbose) 
    {
        this.verbose = verbose;
    }

    private final String shader_file_path = "mandelbrot/src/main/resource/shader/"; // Base path for shader files

    
    /**
     * Constructor that takes separate file paths for the vertex and fragment shaders.
     * Loads the content of each file into the respective shader code variables.
     *
     * @param vertex_file_path   The path to the vertex shader source file.
     * @param fragment_file_path The path to the fragment shader source file.
     */
    public Shader(String shader_name) 
    {
        final String path = shader_file_path + shader_name;

        try 
        {
            this.vertex_shader =   loadShaderSource(path + ".vert");
            this.fragment_shader = loadShaderSource(path + ".frag");
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new RuntimeException("Error: Couldn't open one of the shader files.");
        }
    }

    /**
     * Loads the shader source from a file.
     *
     * @param file_path The shader source file path.
     * @return The shader source code as a string.
     * @throws IOException If there's an error reading the file.
     */
    private String loadShaderSource(String file_path) throws IOException 
    {
        // Check if the file exists before attempting to read it
        if (!Files.exists(Paths.get(file_path))) 
        {
            throw new RuntimeException("Shader file not found: " + file_path);
        }

        return Files.readString(Paths.get(file_path), StandardCharsets.UTF_8);
    }

    /**
     * Compiles and links the vertex and fragment shaders into a single shader program.
     */
    public void compile() 
    {
        int vertex_ID = compileShader(vertex_shader, GL20.GL_VERTEX_SHADER);
        int fragment_ID = compileShader(fragment_shader, GL20.GL_FRAGMENT_SHADER);

        program_ID = GL20.glCreateProgram();
        GL20.glAttachShader(program_ID, vertex_ID);
        GL20.glAttachShader(program_ID, fragment_ID);

        GL20.glLinkProgram(program_ID);

        checkLinkingErrors();

        GL20.glDeleteShader(vertex_ID);
        GL20.glDeleteShader(fragment_ID);
    }


    /**
     * Compiles an individual shader (vertex or fragment).
     *
     * @param source The GLSL source code for the shader.
     * @param type   The type of shader (GL_VERTEX_SHADER or GL_FRAGMENT_SHADER).
     * @return The OpenGL shader ID.
     */
    private int compileShader(String source, int type) 
    {
        int shader_ID = GL20.glCreateShader(type);
        GL20.glShaderSource(shader_ID, source);
        GL20.glCompileShader(shader_ID);

        checkCompilationErrors(shader_ID, type);

        return shader_ID;
    }


    /**
     * Checks for shader compilation errors.
     *
     * @param shader_ID The ID of the shader to check.
     * @param type      The type of shader (vertex or fragment).
     */
    private void checkCompilationErrors(int shader_ID, int type) 
    {
        if (GL20.glGetShaderi(shader_ID, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) 
        {
            String log = GL20.glGetShaderInfoLog(shader_ID);
            System.err.println("Shader compilation error (" + type + "):\n" + log);
            throw new RuntimeException("Shader compilation failed (" + type + ")");
        }
    }


    /**
     * Checks for shader program linking errors.
     */
    private void checkLinkingErrors() {
        if (GL20.glGetProgrami(program_ID, GL20.GL_LINK_STATUS) == GL20.GL_FALSE) 
        {
            String log = GL20.glGetProgramInfoLog(program_ID);
            System.err.println("Shader linking error:\n" + log);
            throw new RuntimeException("Shader linking failed.");
        }
    }

    /**
     * Retrieves the location of a uniform variable in the shader program.
     * Caches the location for future use to avoid repeated lookups.
     * 
     * @param name
     * @return The location of the uniform variable, or -1 if not found.
     */
    public int getUniformLocation(String name) 
    {
        return uniformCache.computeIfAbsent(name, key -> 
        {
            int location = GL20.glGetUniformLocation(program_ID, key);
            if (location == -1 && verbose) 
            {
                System.err.println("Warning: Uniform '" + key + "' not found.");
            }
            return location;
        });
    }

    /**
     * Executes the provided action if the uniform variable is found in the shader program.
     * 
     * @param name   The name of the uniform variable.
     * @param action The action to perform with the uniform location.
     */
    private void withUniform(String name, IntConsumer action) 
    {
        int location = getUniformLocation(name);

        // Only execute the action if the uniform location is valid (not -1)
        if (location != -1) 
        {
            action.accept(location);
        }
    }


     // --- Setters for uniform values ---

    // Sets a float uniform in the shader program.
    public void setUniform1f(String name, float v) 
    {
        withUniform(name, loc -> GL20.glUniform1f(loc, v));
    }

    // Sets a 1D vector uniform in the shader program.
    public void setUniform1i(String name, int v) 
    {
        withUniform(name, loc -> GL20.glUniform1i(loc, v));
    }

    // Sets a 1D vector uniform in the shader program.
    public void setUniform2f(String name, float x, float y) 
    {
        withUniform(name, loc -> GL20.glUniform2f(loc, x, y));
    }

    // Sets a 2D vector uniform in the shader program.
    public void setUniform2f(String name, Vector2f v) 
    {
        setUniform2f(name, v.x, v.y);
    }

    // Sets a 2D vector uniform in the shader program.
    public void setUniform2i(String name, int x, int y) 
    {
        withUniform(name, loc -> GL20.glUniform2i(loc, x, y));
    }

    // Sets a 2D vector uniform in the shader program.
    public void setUniform3f(String name, float x, float y, float z) 
    {
        withUniform(name, loc -> GL20.glUniform3f(loc, x, y, z));
    }

    // Sets a 3D vector uniform in the shader program.
    public void setUniform3f(String name, Vector3f v)
    {
        setUniform3f(name, v.x, v.y, v.z);
    }

    // Sets a 3D vector uniform in the shader program.
    public void setUniform4f(String name, float x, float y, float z, float w) 
    {
        withUniform(name, loc -> GL20.glUniform4f(loc, x, y, z, w));
    }

    // Sets a 4D vector uniform in the shader program.
    public void setUniform4f(String name, Vector4f v) 
    {
        setUniform4f(name, v.x, v.y, v.z, v.w);
    }

    // Sets a 4x4 matrix uniform in the shader program.
    public void setUniformMatrix4f(String name, Matrix4f matrix) 
    {
        withUniform(name, loc -> 
        {
            float[] buffer = new float[16];
            matrix.get(buffer);
            GL20.glUniformMatrix4fv(loc, false, buffer);
        });
    }


    /**
     * Activates the shader program.
     */
    public void use() 
    {
        GL20.glUseProgram(program_ID);
    }


    /**
     * Deactivates any currently bound shader.
     */
    public void detach() 
    {
        GL20.glUseProgram(0);
    }


    /**
     * Deletes the shader program from OpenGL.
     */
    public void delete() 
    {
        GL20.glDeleteProgram(program_ID);
    }


    /**
     * @return The program ID of the shader.
     */
    public int getProgramID() 
    {
        return program_ID;
    }
}
