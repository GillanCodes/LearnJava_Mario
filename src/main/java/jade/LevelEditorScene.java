package jade;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {

    private String vertexShaderSrc = "#version 330 core\n" +
            "layout (location=0) in vec3 aPos;\n" +
            "layout (location=1) in vec4 aColor;\n" +
            "\n" +
            "out vec4 fColor;\n" +
            "\n" +
            "void main() {\n" +
            "    fColor = aColor;\n" +
            "    gl_Position = vec4(aPos, 1.0);\n" +
            "}";

    private  String fragmentShaderSrc = "#version 330 core\n" +
            "\n" +
            "in vec4 fColor;\n" +
            "\n" +
            "out vec4 color;\n" +
            "\n" +
            "void main() {\n" +
            "    color = fColor;\n" +
            "}";

    private int vertexID, fragmentID, shaderProgram;
    private float[] vertexArray = {
            //position              //color
            0.5f, -0.5f, 0.0f,      1.0f, 0.0f,  0.0f, 1.0f, //Bottom Right 0
            -0.5f, 0.5f, 0.0f,      0.0f, 1.0f,  0.0f, 1.0f, //top left 1
            0.5f, 0.5f, 0.0f,       0.0f, 0.0f,  1.0f, 1.0f, //top right 2
            -0.5f, -0.5f, 0.0f,     1.0f, 1.0f,  0.0f, 1.0f, //bottom left 3
    };

    //counter-clockwise order
    private int[] elementArray = {
            2,1,0, //Top Right Triangle
            0,1,3 // bottom left Triangle
    };

    private int vaoID, vboID, eboID;

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        //Compile and like shader

        //Load and comple vertex shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);

        //pass the shader source code
        glShaderSource(vertexID, vertexShaderSrc);
        glCompileShader(vertexID);

        //check for errors
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE){
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("Error : 'Default Shader.glsl'\n\tVertex shader compilation failed.");
            System.out.println(glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }


        //Load and comple fragment shader
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);

        //pass the shader source code
        glShaderSource(fragmentID, fragmentShaderSrc);
        glCompileShader(fragmentID);

        //check for errors
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE){
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("Error : 'Default Shader.glsl'\n\tFragment shader compilation failed.");
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        //link shader and check errors
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexID);
        glAttachShader(shaderProgram, fragmentID);
        glLinkProgram(shaderProgram);

        //check for linking errors
        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
            System.out.println("Error : 'Default Shader.glsl'\n\tLinking shader failed.");
            System.out.println(glGetProgramInfoLog(fragmentID, len));
            assert false : "";
        }

        //generate VAO,VBO and EBO Buffer Object and send it to the gpu
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        //create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        //create VBO upload vertex buffer
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        //create the indices and uplaod
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        // add vertex attribute pointers
        int positionsSize = 3;
        int colorsSize = 4;
        int floatSizeBytes = 4;
        int vertexSizeBytes = (positionsSize + colorsSize) * floatSizeBytes;
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorsSize, GL_FLOAT, false, vertexSizeBytes, positionsSize * floatSizeBytes);
        glEnableVertexAttribArray(1);
    }

    @Override
    public void update(float dt) {
        //Bind shader program
        glUseProgram(shaderProgram);
        //bind vao
        glBindVertexArray(vaoID);

        //enable bertex attrib
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        //unbind everything
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);
        glUseProgram(0);
    }

}
