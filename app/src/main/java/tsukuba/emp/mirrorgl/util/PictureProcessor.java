package tsukuba.emp.mirrorgl.util;

import android.util.Log;

import org.jibble.simpleftp.SimpleFTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static android.opengl.GLES20.glGetShaderInfoLog;

/**
 * Created by Floris on 26-7-2015.
 */
public class PictureProcessor extends Thread {

    private File file;

    PictureProcessor(File file)
    {
        this.file = file;
    }

    @Override
    public void run() {
        try {
            SimpleFTP ftp = new SimpleFTP();

            // Connect to an FTP server on port 21.
            ftp.connect("192.168.10.4", 21);

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
}
