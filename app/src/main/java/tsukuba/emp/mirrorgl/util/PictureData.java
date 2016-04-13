package tsukuba.emp.mirrorgl.util;

import android.graphics.Rect;

public class PictureData {
    private byte[] data;
    private Rect faceRect;

    public PictureData(byte[] data, Rect faceRect) {
        this.data = data;
        this.faceRect = faceRect;
    }

    public byte[] getData() {
        return data;
    }

    public Rect getFaceRect() {
        return faceRect;
    }
}
