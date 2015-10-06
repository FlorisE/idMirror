package tsukuba.emp.mirrorgl;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import tsukuba.emp.mirrorgl.util.CameraSurfaceView;
import tsukuba.emp.mirrorgl.util.ProjectionServerListener;
import tsukuba.emp.mirrorgl.util.Settings;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Mirror extends Activity implements View.OnTouchListener {
    /**
     * The surfaceview for OpenGL rendering
     */
    private GLSurfaceView glSurfaceView;

    private SurfaceTexture mSurfaceTexture;

    private PowerManager.WakeLock mWL;

    private boolean actionBarShown = false;

    private ActionBar actionBar = null;

    private long lastTouch = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mWL = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        mWL.acquire();

        setContentView(R.layout.activity_mirror);

        glSurfaceView = (CameraSurfaceView) findViewById(R.id.cameraSurfaceView);

        actionBar = getActionBar();
        actionBar.hide();

        glSurfaceView.
                setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        glSurfaceView.setOnTouchListener(this);
    }

    @Override
    protected void onPause() {
        if (mWL.isHeld())
            mWL.release();

        glSurfaceView.onPause();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        glSurfaceView.onResume();

        mWL.acquire();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mirror_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.set_ftp_addr:
                showSetFtpAddressDialog();
                return true;
            default:
                return false;
        }
    }

    private void showSetFtpAddressDialog() {
        Settings settings = Settings.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Set FTP details");

        final EditText ftpAddress = new EditText(this);
        ftpAddress.setInputType(InputType.TYPE_CLASS_TEXT);
        ftpAddress.setText(settings.getFtpAddress());

        final EditText ftpPort = new EditText(this);
        ftpPort.setInputType(InputType.TYPE_CLASS_NUMBER);
        ftpPort.setText(Integer.toString(settings.getFtpPort()));

        builder.setView(ftpAddress);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Settings settings = Settings.getInstance();
                settings.setFtpAddress(ftpAddress.getText().toString());
                settings.setFtpPort(Integer.parseInt(ftpPort.getText().toString()));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        long touch = System.currentTimeMillis();
        if (touch > lastTouch + 1000) {
            if (!actionBarShown)
                actionBar.show();
            else
                actionBar.hide();

            actionBarShown = !actionBarShown;
            lastTouch = touch;
        }
        return true;
    }
}
