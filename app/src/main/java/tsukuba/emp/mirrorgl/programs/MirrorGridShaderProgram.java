package tsukuba.emp.mirrorgl.programs;

import android.content.Context;
import android.opengl.GLES11Ext;

import java.nio.FloatBuffer;
import java.util.Random;

import tsukuba.emp.mirrorgl.R;
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

    public static final int DIRECTION_NORTHEAST = 0;
    public static final int DIRECTION_SOUTHEAST = 1;
    public static final int DIRECTION_SOUTHWEST = 2;
    public static final int DIRECTION_NORTHWEST = 3;

    private int direction = -1;

    private Random rand = new Random();

    public MirrorGridShaderProgram(Context context) {
        super(context, R.raw.mirror_grid_vertex_shader, R.raw.mirror_grid_fragment_shader);
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
            int midPoint = Constants.BUFFER_NN / 2;
            boolean horizontal = bufferCell.getHorizontalIndex() < midPoint; // false = left side, true = right side
            boolean vertical = bufferCell.getVerticalIndex() > midPoint; // false = top side, true = bottom side

            if (horizontal && vertical)
                direction = DIRECTION_SOUTHEAST;
            if (horizontal && !vertical)
                direction = DIRECTION_NORTHEAST;
            if (!horizontal && vertical)
                direction = DIRECTION_SOUTHWEST;
            if (!horizontal && !vertical)
                direction = DIRECTION_NORTHWEST;

            int horizontalMinMid = bufferCell.getHorizontalIndex() - midPoint;
            int verticalMinMid = bufferCell.getVerticalIndex() - midPoint;
            int midMinHorizontal = midPoint - bufferCell.getHorizontalIndex();
            int midMinVertical = midPoint - bufferCell.getVerticalIndex();

            boolean back = faceTime % 8000 > 4000;

            for (int j = 0; j < 8; j++) {
                switch (direction) {
                    case DIRECTION_NORTHEAST:
                        if (j % 2 == 0) // x
                            move(buffer, false, j, midMinHorizontal, back);
                            //buffer.put(j, buffer.get(j) + (rand.nextFloat()/1000f) * (Constants.MORPHING_DELAY * midMinHorizontal));
                        else
                            move(buffer, false, j, midMinVertical, back);
                            //buffer.put(j, buffer.get(j) + (rand.nextFloat()/1000f) * (Constants.MORPHING_DELAY * midMinVertical));
                        break;
                    case DIRECTION_NORTHWEST:
                        if (j % 2 == 0) // x
                            move(buffer, true, j, horizontalMinMid, back);
                            //buffer.put(j, buffer.get(j) - (rand.nextFloat()/1000f) * (Constants.MORPHING_DELAY * horizontalMinMid));
                        else // y
                            move(buffer, false, j, midMinVertical, back);
                            //buffer.put(j, buffer.get(j) + (rand.nextFloat()/1000f) * (Constants.MORPHING_DELAY * midMinVertical));
                        break;
                    case DIRECTION_SOUTHEAST:
                        if (j % 2 == 0) // x
                            move(buffer, false, j, midMinHorizontal, back);
                            //buffer.put(j, buffer.get(j) + (rand.nextFloat()/1000f) * (Constants.MORPHING_DELAY * midMinHorizontal));
                        else // y
                            move(buffer, true, j, verticalMinMid, back);
                            //buffer.put(j, buffer.get(j) - (rand.nextFloat()/1000f) * (Constants.MORPHING_DELAY * verticalMinMid));
                        break;
                    case DIRECTION_SOUTHWEST:
                        if (j % 2 == 0) // x
                            move(buffer, true, j, horizontalMinMid, back);
                            //buffer.put(j, buffer.get(j) - (rand.nextFloat()/1000f) * (Constants.MORPHING_DELAY * horizontalMinMid));
                        else
                            move(buffer, true, j, verticalMinMid, back);
                            //buffer.put(j, buffer.get(j) - (rand.nextFloat()/1000f) * (Constants.MORPHING_DELAY * verticalMinMid));
                        break;
                }


                //if (rand.nextFloat() > 0.25f)
                //    buffer.put(j, buffer.get(j) + rand.nextFloat() / Constants.MORPHING_DELAY);
                //else
                //    buffer.put(j, buffer.get(j) - rand.nextFloat() / Constants.MORPHING_DELAY);
            }

            glVertexAttribPointer(ph, 2, GL_FLOAT, false, 4 * 2, buffer);
            glVertexAttribPointer(tch, 2, GL_FLOAT, false, 4 * 2, textureBuffer);
            glEnableVertexAttribArray(ph);
            glEnableVertexAttribArray(tch);

            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        }
        glFlush();
    }

    void move(FloatBuffer buffer, boolean axis, int index, int morphIndex) {
        move(buffer, axis, index, morphIndex, false);
    }

    void move(FloatBuffer buffer, boolean axis, int index, int morphIndex, boolean back) {
        if (!axis && !back)
            buffer.put(index, buffer.get(index) + (rand.nextFloat() / 1500f) * (Constants.MORPHING_DELAY * morphIndex));
        else if (!axis && back)
            buffer.put(index, buffer.get(index) - (rand.nextFloat() / 1500f) * (Constants.MORPHING_DELAY * morphIndex));
        else if (axis && !back)
            buffer.put(index, buffer.get(index) - (rand.nextFloat() / 1500f) * (Constants.MORPHING_DELAY * morphIndex));
        else
            buffer.put(index, buffer.get(index) + (rand.nextFloat() / 1500f) * (Constants.MORPHING_DELAY * morphIndex));
    }
}
