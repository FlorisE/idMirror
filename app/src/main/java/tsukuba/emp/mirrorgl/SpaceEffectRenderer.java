package tsukuba.emp.mirrorgl;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import tsukuba.emp.mirrorgl.programs.MirrorGridShaderProgram;
import tsukuba.emp.mirrorgl.util.BufferHolder;
import tsukuba.emp.mirrorgl.util.CameraHolder;
import tsukuba.emp.mirrorgl.util.CameraSurfaceView;
import tsukuba.emp.mirrorgl.util.Constants;

import static android.opengl.GLES20.*;

/**
 * Created by utail on 6/9/2015.
 */
public class SpaceEffectRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, Camera.FaceDetectionListener {

    private final BufferHolder mBufferHolder;
    private int[] hTex;

    private List<MirrorGridShaderProgram> programs = new ArrayList<>();

    private CameraHolder mCameraHolder;
    private SurfaceTexture mSTexture;

    private boolean mUpdateST = false;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    public CameraSurfaceView getCameraSurfaceView() {
        return cameraSurfaceView;
    }

    /**
     * The context with which the renderer should interact
     */
    private CameraSurfaceView cameraSurfaceView;

    private boolean fadeStarted = false;
    private boolean fadeOutStarted = false;

    private ImageView imageView;

    public SpaceEffectRenderer(CameraSurfaceView cameraSurfaceView)
    {
        this.cameraSurfaceView = cameraSurfaceView;

        mCameraHolder = new CameraHolder(this);
        mBufferHolder = new BufferHolder(this, mCameraHolder);
    }

    public void close() {
        mUpdateST = false;
        mSTexture.release();
        mCameraHolder.close();
        deleteTex();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        initTex();
        mSTexture = new SurfaceTexture ( hTex[0] );
        mSTexture.setOnFrameAvailableListener(this);

        mCameraHolder.initialize(mSTexture);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);

        initializePrograms();
    }

    private void initializePrograms() {
        for (int i = 1; i <= mBufferHolder.getRows(); i++) {
            for (int j = 1; j <= mBufferHolder.getColumns(); j++) {
                programs.add(new MirrorGridShaderProgram(cameraSurfaceView.getContext(), this));
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);

        if (mCameraHolder != null) {
            mCameraHolder.setParameters(mSTexture, width, height);

            mCameraHolder.surfaceChanged();
        }

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    private void initTex() {
        hTex = new int[1];
        glGenTextures(1, hTex, 0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }

    private void deleteTex() {
        glDeleteTextures(1, hTex, 0);
    }

    public synchronized void onFrameAvailable ( SurfaceTexture st ) {
        mUpdateST = true;
        cameraSurfaceView.requestRender();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear( GL_COLOR_BUFFER_BIT );

        synchronized(this) {
            if ( mUpdateST ) {
                mSTexture.updateTexImage();
                mUpdateST = false;
            }
        }

        mBufferHolder.resetBuffers();

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        mBufferHolder.renderToPrograms(programs, hTex[0], mMVPMatrix);
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if (faces.length > 0) {
            if (!fadeStarted) {
                fadeStarted = true;
                fadeOutStarted = false;

                imageView = (ImageView) ((Activity)cameraSurfaceView.getContext()).findViewById(R.id.fadeview);
                imageView.setBackgroundColor(Color.BLACK);

                Animation animation = new AlphaAnimation(1.0f, 0.0f);
                animation.setDuration(Constants.FADE_TIME);
                animation.setRepeatCount(0);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        imageView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                imageView.startAnimation(animation);
            }
            mBufferHolder.updateBuffers(faces);
        }
    }

    public void fadeOut() {
        ((Activity) cameraSurfaceView.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (imageView != null && !fadeOutStarted) {
                    fadeOutStarted = true;
                    Animation animation = new AlphaAnimation(0.0f, 1.0f);
                    animation.setDuration(Constants.FADE_TIME);
                    animation.setRepeatCount(0);

                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            imageView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });

                    imageView.startAnimation(animation);
                }
            }
        });
    }

    public void resetFade() {
        fadeStarted = false;

        ((Activity) cameraSurfaceView.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (imageView != null) {
                    imageView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}

