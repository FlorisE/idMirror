package tsukuba.emp.mirrorgl.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureProcessor extends Thread {

    private File file;
    private byte[] data;
    private Rect faceRect;

    PictureProcessor(byte[] data, Rect faceRect)
    {
        this.data = data;
        this.faceRect = faceRect;
    }

    @Override
    public void run() {
        resizeAndSave(data, faceRect);

        Settings settings = Settings.getInstance();
        String ftpAddress = settings.getFtpAddress();

        if (!ftpAddress.isEmpty()) {
            try {
                SimpleFTP ftp = new SimpleFTP();

                ftp.connect(settings.getFtpAddress(), settings.getFtpPort());
                ftp.cwd("pictures");
                ftp.bin();
                ftp.stor(file);

                // When uploading we give the file a temporary name so it doesn't get loaded by the
                // projection directly, so here we rename the file so it will get loaded
                ftp.ren(file.getName(), file.getName() + ".jpg");

                // Finished!
                ftp.disconnect();
            } catch (IOException e) {
                if (LoggerConfig.ON) {
                    Log.e("FTP", e.getMessage());
                }
            }
        }
    }

    private void resizeAndSave(byte[] data, Rect faceRect) {
        file = getOutputMediaFile();

        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

        try {
            FileOutputStream fos = new FileOutputStream(file);

            int x = (Math.round((faceRect.left + 1000f) / 2f) * (bmp.getWidth() / 1000) ) - 25;
            int y = (Math.round((faceRect.top + 1000f) / 2f) * (bmp.getHeight() / 1000)) - 25;
            int width = (Math.round(faceRect.width()  * (bmp.getWidth() / 2000f))) + 50;
            int height = (Math.round(faceRect.height() * (bmp.getHeight() / 2000f))) + 50;

            if (width > height) {
                // increase the height to match width
                int difference = width - height;
                y = y - difference / 2;
                height = height + difference;
            } else if (height > width) {
                //  increase the width to match height
                int difference = height - width;
                x = x - difference / 2;
                width = width + difference;
            }

            x = x < 0 ? 0 : x;
            y = y < 0 ? 0 : y;
            width = width > bmp.getWidth() ? bmp.getWidth() : width;
            height = height > bmp.getHeight() ? bmp.getHeight() : height;

            if (x + width > bmp.getWidth()) {
                x = x - (x + width - bmp.getWidth());
            }

            if (y + height > bmp.getHeight()) {
                y = y - (y + height - bmp.getHeight());
            }

            Bitmap newBmp = Bitmap.createBitmap(bmp, x, y, width, height);

            Bitmap rotatedBitmap = ExifUtil.rotateBitmap(file.getAbsolutePath(), newBmp);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    static File getOutputMediaFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());

        // device id
        String deviceId64 = android.provider.Settings.Secure.ANDROID_ID;
        String shortDeviceId = deviceId64.substring(deviceId64.length()-9);

        // Append the last 8 characters of the device id to avoid conflicts
        File mediaFile = new File(File.separator + "sdcard" + File.separator + "idMirror" +
                File.separator + "IMG_" + timeStamp + "_" + shortDeviceId);

        return mediaFile;
    }
}
