package tsukuba.emp.mirrorgl.programs;

import android.content.Context;

import tsukuba.emp.mirrorgl.util.ShaderHelper;
import tsukuba.emp.mirrorgl.util.TextResourceReader;

import static android.opengl.GLES20.glUseProgram;

/**
 * Created by utail on 6/9/2015.
 */
public class ShaderProgram {
    // Shader program
    protected final int program;

    protected ShaderProgram(Context context, int vertexShaderResourceId,
                            int fragmentShaderResourceId) {
        // Compile the shaders and link the program.
        program = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResource(
                        context, vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(
                        context, fragmentShaderResourceId));
    }
    public int useProgram() {
        // Set the current OpenGL shader program to this program.
        glUseProgram(program);
        return program;
    }
}
