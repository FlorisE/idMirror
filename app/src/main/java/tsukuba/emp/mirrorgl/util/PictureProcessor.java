package tsukuba.emp.mirrorgl.util;

import org.jibble.simpleftp.SimpleFTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
            ftp.connect("ftp.floriserich.nl", 21, "floriser", "W27;vxVB+l7K");

            // Set binary mode.
            ftp.bin();

            // Change to a new working directory on the FTP server.
            ftp.cwd("web");

            // Upload some files.
            ftp.stor(file);

            // Quit from the FTP server.
            ftp.disconnect();
        }
        catch (IOException e) {
            // Jibble.
        }
    }
}
