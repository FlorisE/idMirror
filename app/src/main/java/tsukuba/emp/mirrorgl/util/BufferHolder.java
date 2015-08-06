package tsukuba.emp.mirrorgl.util;

import android.graphics.Rect;
import android.hardware.Camera;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tsukuba.emp.mirrorgl.SpaceEffectRenderer;
import tsukuba.emp.mirrorgl.programs.MirrorGridShaderProgram;

import static android.opengl.GLES20.glViewport;

public class BufferHolder {

    private long faceStart = 0;
    private long faceCurrent = 0;

    private List<FloatBuffer> originalVerticeBuffers = new ArrayList<>();
    private List<FloatBuffer> verticeBuffers = new ArrayList<>();
    private List<FloatBuffer> textureBuffers = new ArrayList<>();

    private List<VerticeBufferCell> verticeBufferCells = new ArrayList<>();

    private Rect faceRect = null;
    private CameraHolder mCameraHolder = null;
    private boolean picTaken = false;
    private SpaceEffectRenderer renderer;

    public BufferHolder(SpaceEffectRenderer renderer, CameraHolder cameraHolder) {
        this.renderer = renderer;
        this.mCameraHolder = cameraHolder;

        for (int i = 1; i <= Constants.BUFFER_NN; i++)
            for (int j = 1; j <= Constants.BUFFER_NN; j++) {
                float width = (1f / Constants.BUFFER_NN) * 2f;
                float height = (1f / Constants.BUFFER_NN) * 2f;

                float blX = 1.0f - ((float) i / Constants.BUFFER_NN) * 2f;
                float blY = 1.0f - ((float) j / Constants.BUFFER_NN) * 2f;
                float brX = blX + width;
                float trY = blY + height;

                // { 1.0f, 0f, 0f, 0f, 1.0f, 1.0f, 0f, 1.0f };

                FloatBuffer buffer = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                FloatBuffer buffer2 = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                originalVerticeBuffers.add(buffer2);
                verticeBuffers.add(buffer);
                buffer.put(new float[]{brX, blY, blX, blY, brX, trY, blX, trY});
                buffer2.put(new float[]{brX, blY, blX, blY, brX, trY, blX, trY});
                buffer.position(0);
                buffer2.position(0);

                verticeBufferCells.add(new VerticeBufferCell(i - 1, j - 1, buffer));

                FloatBuffer texCoords = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                textureBuffers.add(texCoords);
                texCoords.put(buffer2);
                texCoords.position(0);
            }
    }

    public int getRows() {
        return Constants.BUFFER_NN;
    }

    public int getColumns() {
        return Constants.BUFFER_NN;
    }

    public void resetBuffers() {
        if (System.currentTimeMillis() > faceCurrent + 2000) {
            faceStart = 0;
            picTaken = false;
            renderer.resetFade();
        }

        for (int i = 0; i < verticeBuffers.size(); i++) {
            if (faceStart == 0) {
                for (int j = 0; j < 8; j++) {
                    verticeBuffers.get(i).put(j, originalVerticeBuffers.get(i).get(j));
                }
            }
        }
    }

    public void renderToPrograms(List<MirrorGridShaderProgram> programs, int tex, float[] mMVPMatrix) {
        for (int i = 0; i < verticeBufferCells.size(); i++) {
            MirrorGridShaderProgram hProgram = programs.get(i);
            VerticeBufferCell bufferCell = verticeBufferCells.get(i);
            hProgram.render(bufferCell, tex, faceStart, textureBuffers.get(Constants.BUFFER_NN * (bufferCell.getHorizontalIndex()) + (bufferCell.getVerticalIndex())));
        }
    }

    public void updateBuffers(Camera.Face[] faces) {
        faceCurrent = System.currentTimeMillis();

        if (faceStart == 0)
            faceStart = faceCurrent;

        if (faceCurrent > faceStart + 5000 && !picTaken) {
            mCameraHolder.tryTakePicture();
            picTaken = true;
        }

        faceRect = faces[0].rect;

        float left = ((faceRect.left / 1000f) + 1f) / 2;
        float bottom = ((faceRect.top / 1000f) + 1f) / 2;
        float width = faceRect.width() / 2000f;
        float height = faceRect.height() / 2000f;

        for (int i = 0; i < verticeBufferCells.size(); i++) {
            VerticeBufferCell bufferCell = verticeBufferCells.get(i);

            bufferCell.setDrawn(true);
        }

        int count = -1;

        stretchDetectedFaceToScreen(left, bottom, width, height, count);
    }

    private void stretchDetectedFaceToScreen(float left, float bottom, float width, float height, int count) {
        for (int i = 1; i <= Constants.BUFFER_NN; i++) {
            float particleBottom = bottom + ((float) i / Constants.BUFFER_NN) * height;
            float particleTop = particleBottom + height / Constants.BUFFER_NN;

            for (int j = Constants.BUFFER_NN; j > 0; j--) {
                count++;

                float particleLeft = left + ((float) j / Constants.BUFFER_NN) * width;
                float particleRight = particleLeft + width / Constants.BUFFER_NN;

                FloatBuffer buffer = textureBuffers.get(count);
                buffer.put(new float[]{particleLeft, particleBottom, particleLeft, particleTop, particleRight, particleBottom, particleRight, particleTop});
                buffer.position(0);
            }
        }
    }
}