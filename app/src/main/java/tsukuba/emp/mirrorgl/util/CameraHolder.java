package tsukuba.emp.mirrorgl.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
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
        File pictureFile = getOutputMediaFile();
        camera.startPreview();

        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);



        //Matrix m = new Matrix();
        //m.setScale(bmp.getWidth(), bmp.getHeight());
        //m.preRotate(180, bmp.getWidth() / 2, bmp.getHeight() / 2);

        //Bitmap rotatedBitMap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);

        if (pictureFile == null) {
            //no path to picture, return
            safeToTakePicture = true;
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            //ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //

            Bitmap rotatedBitmap = ExifUtil.rotateBitmap(pictureFile.getAbsolutePath(), bmp);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();              //<-------- show exception
        } catch (IOException e) {
            e.printStackTrace();              //<-------- show exception
        }

        Thread processorThread = new PictureProcessor(pictureFile);
        processorThread.start();

        camera.startPreview();
        safeToTakePicture = true;
        camera.startFaceDetection();
        camera.setFaceDetectionListener(renderer);
    }

    public void tryTakePicture() {
        if (safeToTakePicture) {
            safeToTakePicture = false;
            mCamera.takePicture(null, null, this);
        }
    }

    static File getOutputMediaFile() {

        /* yyyy-MM-dd'T'HH:mm:ss.SSSZ */
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());

        // file name
        File mediaFile = new File(File.separator + "sdcard" + File.separator + "idMirror" +
                File.separator + "IMG_" + timeStamp);

        return mediaFile;

    }
}
