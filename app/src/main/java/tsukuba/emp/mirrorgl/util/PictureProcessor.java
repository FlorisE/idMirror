package tsukuba.emp.mirrorgl.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;

import org.jibble.simpleftp.SimpleFTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.opengl.GLES20.glGetShaderInfoLog;

/**
 * Created by Floris on 26-7-2015.
 */
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
        resizeAndUpload(data, faceRect);

        try {
            SimpleFTP ftp = new SimpleFTP();

            Settings settings = Settings.getInstance();

            // Connect to an FTP server on port 21.
            ftp.connect(settings.getFtpAddress(), settings.getFtpPort());

            // Set binary mode.
            ftp.bin();

            // Upload some files.
            ftp.stor(file);

            // Quit from the FTP server.
            ftp.disconnect();
        }
        catch (IOException e) {
            if (LoggerConfig.ON) {
                // Print the shader info log to the Android log output.
                Log.e("FTP", e.getMessage());
            }
        }
    }

    private void resizeAndUpload(byte[] data, Rect faceRect) {
        file = getOutputMediaFile();

        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            //ByteArrayOutputStream stream = new ByteArrayOutputStream();

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
            e.printStackTrace();              //<-------- show exception
        } catch (IOException e) {
            e.printStackTrace();              //<-------- show exception
        }
    }



    static File getOutputMediaFile() {

        /* yyyy-MM-dd'T'HH:mm:ss.SSSZ */
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());

        // file name
        File mediaFile = new File(File.separator + "sdcard" + File.separator + "idMirror" +
                File.separator + "IMG_" + timeStamp + "_C");

        return mediaFile;
    }
}
