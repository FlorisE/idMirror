package tsukuba.emp.mirrorgl.util;

import android.app.Activity;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.WindowManager;

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
    private Activity mirror;

    public BufferHolder(SpaceEffectRenderer renderer, CameraHolder cameraHolder) {
        this.renderer = renderer;
        this.mCameraHolder = cameraHolder;
        mirror = (Activity) renderer.getCameraSurfaceView().getContext();

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

                VerticeBufferCell bufferCell = new VerticeBufferCell(i - 1, j - 1, buffer);
                float bufferWidth = buffer.get(0) - buffer.get(2);
                float bufferHeight = buffer.get(5) - buffer.get(1);

                if (inEllipse(buffer.get(5) - bufferHeight/2, -1f * (buffer.get(0) - bufferWidth/2), 0f, 0f, 1f, 1f)) {
                    bufferCell.setDrawn(true);
                }

                verticeBufferCells.add(bufferCell);

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

    private void setBrightness(final float brightness) {
        mirror.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WindowManager.LayoutParams layout = mirror.getWindow().getAttributes();
                layout.screenBrightness = brightness;
                mirror.getWindow().setAttributes(layout);
            }
        });
    }

    public void resetBuffers() {
        if (System.currentTimeMillis() > faceCurrent + 2000) {
            faceStart = 0;
            picTaken = false;
            renderer.resetFade();
            setBrightness(0F);
        }

        if (faceStart == 0) {
            for (int i = 0; i < verticeBuffers.size(); i++) {
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

        if (faceStart == 0) {
            faceStart = faceCurrent;
            setBrightness(1F);
        }

        if (faceCurrent > faceStart + Constants.PICTURE_TIME && !picTaken) {
            mCameraHolder.tryTakePicture(faceRect);
            picTaken = true;
        }

        faceRect = faces[0].rect;

        float left = ((faceRect.left / 1000f) + 1f) / 2;
        float bottom = ((faceRect.top / 1000f) + 1f) / 2;
        float width = faceRect.width() / 2000f;
        float height = faceRect.height() / 2000f;

        /*for (int i = 0; i < verticeBufferCells.size(); i++) {
            VerticeBufferCell bufferCell = verticeBufferCells.get(i);
            FloatBuffer buffer = bufferCell.getBuffer();

            float bufferWidth = buffer.get(0) - buffer.get(2);
            float bufferHeight = buffer.get(5) - buffer.get(1);

            if (inEllipse(buffer.get(5) - bufferHeight/2, -1f * (buffer.get(0) - bufferWidth/2), 0f, 0f, 1f, 1f)) {
                bufferCell.setDrawn(true);
            }
        }*/

        int count = -1;

        stretchDetectedFaceToScreen(left, bottom, width, height, count);
    }

    private void stretchDetectedFaceToScreen(float left, float bottom, float width, float height, int count) {
        for (int i = 1; i <= Constants.BUFFER_NN; i++) {
            float particleBottom = bottom + ((float) i / Constants.BUFFER_NN) * height;
            float particleTop = particleBottom + height / Constants.BUFFER_NN;

            for (int j = Constants.BUFFER_NN; j > 0; j--) {
                count++;
                FloatBuffer buffer = textureBuffers.get(count);
                float particleLeft = left + ((float) j / Constants.BUFFER_NN) * width;
                float particleRight = particleLeft + width / Constants.BUFFER_NN;

                buffer.put(new float[]{particleLeft, particleBottom, particleLeft, particleTop, particleRight, particleBottom, particleRight, particleTop});
                buffer.position(0);
            }
        }
    }

    public boolean inEllipse(float pointX, float pointY, float originX, float originY, float xRadius, float yRadius) {
        return ((Math.pow(pointX - originX, 2)/Math.pow(xRadius, 2)) + (Math.pow(pointY - originY, 2)/Math.pow(yRadius, 2)) <= 1);
    }
}
