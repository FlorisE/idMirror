package tsukuba.emp.mirrorgl.objects;


import java.util.Random;

import tsukuba.emp.mirrorgl.data.VertexArray;
import tsukuba.emp.mirrorgl.programs.TextureShaderProgram;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static tsukuba.emp.mirrorgl.util.Constants.*;


/**
 * Created by utail on 6/9/2015.
 */
public class Table {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private static final float[] VERTEX_DATA = {
        // Order of coordinates: X, Y, S, T
        // Triangle Fan
        0f, 0f, 0.5f, 0.5f,
        -0.5f, -0.8f, 0f, 0.9f,
        0.5f, -0.8f, 1f, 0.9f,
        0.5f, 0.8f, 1f, 0.1f,
        -0.5f, 0.8f, 0f, 0.1f,
        -0.5f, -0.8f, 0f, 0.9f
    };

    private final VertexArray vertexArray;

    public Table() {
        float particleWidth = 0.06f;
        float particleHeight = 0.03f;
        Random rand = new Random();
        float randX = ((rand.nextFloat() - 0.5f) / 2f);
        float randY = ((rand.nextFloat() - 0.5f) / 1.5f);
        float originX = randX + particleWidth / 2;
        float originY = randY + particleHeight / 2;

        float[] vertexData = {
                // Order of coordinates: X, Y, S, T
                // Triangle Fan
                originX, originY, 0.5f, 0.5f,
                randX, randY, 0f, 0.9f,
                randX + particleWidth, randY, 1f, 0.9f,
                randX + particleWidth, randY + particleHeight, 1f, 0.1f,
                randX, randY + particleHeight, 0f, 0.1f,
                randX, randY, 0f, 0.9f
        };

        vertexArray = new VertexArray(vertexData);
    }

    public void bindData(TextureShaderProgram textureProgram) {
        vertexArray.setVertexAttribPointer(
                0,
                textureProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);
        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    }
}
