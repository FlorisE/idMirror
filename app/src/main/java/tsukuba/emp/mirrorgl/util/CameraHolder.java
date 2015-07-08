package tsukuba.emp.mirrorgl.util;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;
import java.util.List;

import tsukuba.emp.mirrorgl.SpaceEffectRenderer;

public class CameraHolder {
    private Camera mCamera;
    private SpaceEffectRenderer renderer;

    public CameraHolder(SpaceEffectRenderer renderer) {
        this.renderer = renderer;
    }

    public void close() {
        mCamera.stopPreview();
        mCamera = null;
    }

    public void initialize(SurfaceTexture surfaceTexture) {
        int cameras = Camera.getNumberOfCameras();
        int frontFacingCameraId;
        for (frontFacingCameraId = 0; frontFacingCameraId < cameras; frontFacingCameraId++)  {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(frontFacingCameraId, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                break;
            }
        }

        mCamera = Camera.open(frontFacingCameraId);
        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch ( IOException ignored) {
        }
    }

    public void surfaceChanged() {
        mCamera.startPreview();
        mCamera.startFaceDetection();
        mCamera.setFaceDetectionListener(renderer);
    }

    public void setParameters(int width, int height) {
        Camera.Parameters param = mCamera.getParameters();
        List<Camera.Size> psize = param.getSupportedPreviewSizes();
        if ( psize.size() > 0 ) {
            int i;
            for ( i = 0; i < psize.size(); i++ ) {
                if ( psize.get(i).width < width || psize.get(i).height < height )
                    break;
            }
            if ( i > 0 )
                i--;
            param.setPreviewSize(psize.get(i).width, psize.get(i).height);
        }

        if (width < height)
        {
            param.set("orientation", "portrait");
            param.set("rotation", 90);
        }

        mCamera.setParameters(param);
    }
}
