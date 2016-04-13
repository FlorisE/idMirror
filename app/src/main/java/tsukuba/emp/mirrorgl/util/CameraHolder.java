package tsukuba.emp.mirrorgl.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import tsukuba.emp.mirrorgl.SpaceEffectRenderer;

public class CameraHolder implements Camera.PictureCallback {
    private Camera mCamera;
    private SpaceEffectRenderer renderer;

    private boolean safeToTakePicture = false;

    private Rect faceRect = null;

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
        try {
            mCamera.startPreview();
            safeToTakePicture = true;
            mCamera.startFaceDetection();
            mCamera.setFaceDetectionListener(renderer);
        } catch (Exception e) {

        }
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

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        camera.startPreview();


        Thread processorThread = new PictureProcessor(data, faceRect);
        processorThread.start();

        camera.startPreview();
        safeToTakePicture = true;
        camera.startFaceDetection();
        camera.setFaceDetectionListener(renderer);
    }

    public void tryTakePicture(Rect faceRect) {
        if (safeToTakePicture) {
            safeToTakePicture = false;
            this.faceRect = faceRect;
            mCamera.takePicture(null, null, this);
        }
    }
}
