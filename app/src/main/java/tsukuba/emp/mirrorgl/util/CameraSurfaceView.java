package tsukuba.emp.mirrorgl.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

import tsukuba.emp.mirrorgl.Mirror;
import tsukuba.emp.mirrorgl.R;
import tsukuba.emp.mirrorgl.SpaceEffectRenderer;

/**
 * Created by utail on 6/23/2015.
 */
public class CameraSurfaceView extends GLSurfaceView {
    private SpaceEffectRenderer mRenderer;

    private Camera mCamera;

    public CameraSurfaceView(Context context, AttributeSet attributeSet) {
        super ( context, attributeSet );

        // Check if the system supports OpenGL ES 2.0.
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager
                .getDeviceConfigurationInfo();

        final boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));

        mRenderer = new SpaceEffectRenderer(this);

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            setEGLContextClientVersion(2);

            // Assign our renderer.
            setRenderer(mRenderer);
        } else {
            //Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
            //        Toast.LENGTH_LONG).show();
            return;
        }

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY );
    }

    @Override
    public void onPause()
    {
        if (mCamera != null) {
            mCamera.stopFaceDetection();
            mCamera.release();
        }
    }

    public void surfaceCreated ( SurfaceHolder holder ) {
        super.surfaceCreated(holder);
    }

    public void surfaceDestroyed ( SurfaceHolder holder ) {
        mRenderer.close();
        super.surfaceDestroyed(holder);
    }

    public void surfaceChanged ( SurfaceHolder holder, int format, int w, int h ) {
        super.surfaceChanged(holder, format, w, h);
    }
}
