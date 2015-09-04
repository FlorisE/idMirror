package tsukuba.emp.mirrorgl.util;

/**
 * Created by Floris on 31-8-2015.
 */
public class Settings {
    private static Settings instance = null;

    protected Settings() {
        // avoid initialization
    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    private String ftpAddress = "192.168.10.2";
    private int ftpPort = 21;

    public String getFtpAddress() {
        return ftpAddress;
    }

    public void setFtpAddress(String ftpAddress) {
        this.ftpAddress = ftpAddress;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(int port) {
        this.ftpPort = port;
    }

}
