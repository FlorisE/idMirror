package tsukuba.emp.mirrorgl;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

import tsukuba.emp.mirrorgl.util.CameraSurfaceView;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Mirror extends Activity {
    /**
     * The surfaceview for OpenGL rendering
     */
    private GLSurfaceView glSurfaceView;

    private SurfaceTexture mSurfaceTexture;

    private PowerManager.WakeLock mWL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mWL = ((PowerManager)getSystemService ( Context.POWER_SERVICE )).newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        mWL.acquire();

        glSurfaceView = new CameraSurfaceView(this);

        setContentView(glSurfaceView);
    }

    @Override
    protected void onPause () {
        if ( mWL.isHeld() )
            mWL.release();

        glSurfaceView.onPause();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        glSurfaceView.onResume();

        /*if (mCamera == null) {
            startCamera();
        }*/

        mWL.acquire();
    }
}
