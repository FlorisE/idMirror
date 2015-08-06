package tsukuba.emp.mirrorgl.programs;

import android.content.Context;
import android.opengl.GLES11Ext;

import java.nio.FloatBuffer;
import java.util.Random;

import tsukuba.emp.mirrorgl.R;
import tsukuba.emp.mirrorgl.SpaceEffectRenderer;
import tsukuba.emp.mirrorgl.util.Constants;
import tsukuba.emp.mirrorgl.util.VerticeBufferCell;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glFlush;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glVertexAttribPointer;

public class MirrorGridShaderProgram extends ShaderProgram {
    // Uniform constants
    public static final String V_POSITION = "vPosition";
    public static final String V_TEX_COORDINATE = "vTexCoord";
    public static final String S_TEXTURE = "sTexture";

    private int direction = -1;

    private Random rand = new Random(System.currentTimeMillis());

    private SpaceEffectRenderer renderer = null;

    public MirrorGridShaderProgram(Context context, SpaceEffectRenderer renderer) {
        super(context, R.raw.mirror_grid_vertex_shader, R.raw.mirror_grid_fragment_shader);
        this.renderer = renderer;
    }

    public void render(VerticeBufferCell bufferCell, int tex, long faceStart, FloatBuffer textureBuffer) {
        useProgram();

        int ph = glGetAttribLocation(program, MirrorGridShaderProgram.V_POSITION);
        int tch = glGetAttribLocation(program, MirrorGridShaderProgram.V_TEX_COORDINATE);
        int th = glGetUniformLocation(program, MirrorGridShaderProgram.S_TEXTURE);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex);
        glUniform1i(th, 0);

        FloatBuffer buffer = bufferCell.getBuffer();
        // buffer: brX, brY, blX, blY, trX, trY, tlX, tlY
        // pTexCoord: left, bottom, left, bottom + height, left + width, bottom, left + width, bottom + height

        long faceTime = System.currentTimeMillis() - faceStart;

        if (bufferCell.getDrawn() && faceStart != 0) {
            for (int j = 0; j < 8; j++) {
                if (rand.nextBoolean()) {
                    buffer.put(j, (buffer.get(j) + 0.0000002f * faceTime));
                } else {
                    buffer.put(j, (buffer.get(j) - 0.0000002f * faceTime));
                }
            }
            if (faceTime > Constants.FADE_TIME + 2000f) {
                renderer.fadeOut();
            }

            glVertexAttribPointer(ph, 2, GL_FLOAT, false, 4 * 2, buffer);
            glVertexAttribPointer(tch, 2, GL_FLOAT, false, 4 * 2, textureBuffer);
            glEnableVertexAttribArray(ph);
            glEnableVertexAttribArray(tch);

            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        }
        glFlush();
    }
}
