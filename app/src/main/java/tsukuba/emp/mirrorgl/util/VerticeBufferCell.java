package tsukuba.emp.mirrorgl.util;

import java.nio.FloatBuffer;

/**
 * Created by utail on 7/8/2015.
 */
public class VerticeBufferCell {
    int horizontalIndex;
    int verticalIndex;
    FloatBuffer buffer;
    boolean drawn = false;

    public VerticeBufferCell(int horizontalIndex, int verticalIndex, FloatBuffer buffer) {
        this.horizontalIndex = horizontalIndex;
        this.verticalIndex = verticalIndex;
        this.buffer = buffer;
    }

    public int getHorizontalIndex() {
        return horizontalIndex;
    }

    public int getVerticalIndex() {
        return verticalIndex;
    }

    public FloatBuffer getBuffer() {
        return buffer;
    }

    public boolean getDrawn() {
        return drawn;
    }

    public void setDrawn(boolean drawn) {
        this.drawn = drawn;
    }
}
