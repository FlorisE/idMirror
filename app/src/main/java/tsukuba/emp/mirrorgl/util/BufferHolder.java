package tsukuba.emp.mirrorgl.util;

import android.graphics.Rect;
import android.hardware.Camera;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import tsukuba.emp.mirrorgl.programs.MirrorGridShaderProgram;

public class BufferHolder {

    private long faceStart = 0;
    private long faceCurrent = 0;

    private List<FloatBuffer> originalVerticeBuffers = new ArrayList<>();
    private List<FloatBuffer> verticeBuffers = new ArrayList<>();
    private List<FloatBuffer> textureBuffers = new ArrayList<>();

    private List<VerticeBufferCell> verticeBufferCells = new ArrayList<>();

    private final int rows = 32;
    private final int columns = 32;

    public BufferHolder() {
        for (int i = 1; i <= rows; i++)
            for (int j = 1; j <= columns; j++) {
                float width = (1f / rows) * 2f;
                float height = (1f / columns) * 2f;

                float blX = 1.0f - ((float) i / columns) * 2f;
                float blY = 1.0f - ((float) j / rows) * 2f;
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

                verticeBufferCells.add(new VerticeBufferCell(i-1, j-1, buffer));

                FloatBuffer texCoords = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                textureBuffers.add(texCoords);
                texCoords.put(buffer2);
                texCoords.position(0);
            }
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public void resetBuffers() {
        if (System.currentTimeMillis() > faceCurrent + 5000) {
            faceStart = 0;
        }

        for (int i = 0; i < verticeBuffers.size(); i++) {
            if (faceStart == 0) {
                for (int j = 0; j < 8; j++) {
                    verticeBuffers.get(i).put(j, originalVerticeBuffers.get(i).get(j));
                }
            }
        }
    }

    public void renderToPrograms(List<MirrorGridShaderProgram> programs, int tex) {
        for (int i = 0; i < verticeBufferCells.size(); i++) {
            MirrorGridShaderProgram hProgram = programs.get(i);
            VerticeBufferCell bufferCell = verticeBufferCells.get(i);
            hProgram.render(bufferCell, tex, faceStart,  textureBuffers.get(rows * (bufferCell.getHorizontalIndex()) + (bufferCell.getVerticalIndex())));
        }
    }

    public void updateBuffers(Camera.Face[] faces) {
        faceCurrent = System.currentTimeMillis();

        if (faceStart == 0)
            faceStart = faceCurrent;

        Rect faceRect = faces[0].rect;

        float originX = faceRect.exactCenterX() / 1000f;
        float originY = faceRect.exactCenterY() / 1000f;
        float radiusHorizontal = faceRect.width() / 2000f;
        float radiusVertical = faceRect.height() / 2000f;

        float left = ((faceRect.left / 1000f) + 1f) / 2;
        float bottom = ((faceRect.top / 1000f) + 1f) / 2;
        float width = faceRect.width() / 2000f;
        float height = faceRect.height() / 2000f;

        int hStart = -1;
        int hEnd = 0;
        int vStart = -1;
        int vEnd = 0;

        for (int i = 0; i < verticeBufferCells.size(); i++) {
            VerticeBufferCell bufferCell = verticeBufferCells.get(i);
            FloatBuffer buffer = bufferCell.getBuffer();

            float bufferWidth = buffer.get(0) - buffer.get(2);
            float bufferHeight = buffer.get(5) - buffer.get(1);

            bufferCell.setDrawn(false);

            if (inEllipse(buffer.get(5) - bufferHeight/2, -1f * (buffer.get(0) - bufferWidth/2), 0f, 0f, 1f, 1f)) {
                int horizontalIndex = bufferCell.getHorizontalIndex();
                int verticalIndex = bufferCell.getVerticalIndex();

                hStart = hStart == -1 ? horizontalIndex : hStart;
                hEnd = horizontalIndex > hEnd ? horizontalIndex : hEnd;

                vStart = vStart == -1 ? verticalIndex : vStart;
                vEnd = verticalIndex > vEnd ? verticalIndex : vEnd;

                bufferCell.setDrawn(true);
            }
        }

        int count = -1;

        int numColsDrawn = hEnd - hStart;
        int numRowsDrawn = vEnd - vStart;

        for (int i = 1; i <= rows; i++) {
            float particleBottom = bottom + ((float) i / rows) * height;
            float particleTop = particleBottom + height / rows;

            for (int j = columns; j > 0; j--) {
                count++;

                float particleLeft = left + ((float) j / columns) * width;
                float particleRight = particleLeft + width / columns;

                FloatBuffer buffer = textureBuffers.get(count);
                buffer.put(new float[]{particleLeft, particleBottom, particleLeft, particleTop, particleRight, particleBottom, particleRight, particleTop});
                buffer.position(0);
            }
        }
    }

    public boolean inEllipse(float pointX, float pointY, float originX, float originY, float xRadius, float yRadius) {
        return ((Math.pow(pointX - originX, 2)/Math.pow(xRadius, 2)) + (Math.pow(pointY - originY, 2)/Math.pow(yRadius, 2)) <= 1);
    }
}
