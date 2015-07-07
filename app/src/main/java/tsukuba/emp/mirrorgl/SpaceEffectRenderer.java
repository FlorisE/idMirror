package tsukuba.emp.mirrorgl;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import tsukuba.emp.mirrorgl.objects.Table;
import tsukuba.emp.mirrorgl.util.CameraSurfaceView;

import static android.opengl.GLES20.*;

/**
 * Created by utail on 6/9/2015.
 */
public class SpaceEffectRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, Camera.FaceDetectionListener {

    private final String vss =
        "attribute vec2 vPosition;\n" +
        "attribute vec2 vTexCoord;\n" +
        "varying vec2 texCoord;\n" +
        "void main() {\n" +
        "  texCoord = vTexCoord;\n" +
        "  gl_Position = vec4 ( vPosition.x, vPosition.y, 0.0, 1.0 );\n" +
        "}";

    private final String fss =
        "#extension GL_OES_EGL_image_external : require\n" +
        "precision mediump float;\n" +
        "uniform samplerExternalOES sTexture;\n" +
        "varying vec2 texCoord;\n" +
        "void main() {\n" +
        "    gl_FragColor = texture2D(sTexture,texCoord);\n" +
        "}";

    private int[] hTex;

    private List<FloatBuffer> originalVerticeBuffers = new ArrayList<>();
    private List<FloatBuffer> verticeBuffers = new ArrayList<>();

    /**
     * The coordinates of the face inside a texture based on camera capture
     */
    private FloatBuffer pTexCoord;
    private List<Integer> programs = new ArrayList<>();

    private final int rows = 32;
    private final int columns = 32;

    private Camera mCamera;
    private SurfaceTexture mSTexture;

    private boolean mUpdateST = false;

    /**
     * The context with which the renderer should interact
     */
    private CameraSurfaceView cameraSurfaceView;

    private Rect faceRect;

    private Random rand = new Random();

    private long faceStart = 0;
    private long faceCurrent = 0;

    private float originX = 0;
    private float originY = 0;
    private float radiusHorizontal = 0;
    private float radiusVertical = 0;

    public SpaceEffectRenderer(CameraSurfaceView cameraSurfaceView)
    {
        this.cameraSurfaceView = cameraSurfaceView;

        // texture coordinates
        float[] ttmp = { 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f };

        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= columns; j++) {
                float width = (1f / rows) * 2f;
                float height = (1f / columns) * 2f;

                float blX = 1.0f - ((float) i / columns) * 2f;
                float blY = 1.0f - ((float) j / rows) * 2f;
                float brX = blX + width;
                float brY = blY;
                float trX = brX;
                float trY = brY + height;
                float tlX = blX;
                float tlY = trY;

                // { 1.0f, 0f, 0f, 0f, 1.0f, 1.0f, 0f, 1.0f };

                FloatBuffer buffer = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                FloatBuffer buffer2 = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                originalVerticeBuffers.add(buffer2);
                verticeBuffers.add(buffer);
                buffer.put(new float[]{brX, brY, blX, blY, trX, trY, tlX, tlY});
                buffer2.put(new float[]{ brX, brY, blX, blY, trX, trY, tlX, tlY });
                buffer.position(0);
                buffer2.position(0);
            }
        }

        pTexCoord = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pTexCoord.put(ttmp);
        pTexCoord.position(0);
    }

    public void close()
    {
        mUpdateST = false;
        mSTexture.release();
        mCamera.stopPreview();
        mCamera = null;
        deleteTex();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        initTex();
        mSTexture = new SurfaceTexture ( hTex[0] );
        mSTexture.setOnFrameAvailableListener(this);

        int cameras = Camera.getNumberOfCameras();
        int frontFacingCameraId = 0;
        for (frontFacingCameraId = 0; frontFacingCameraId < cameras; frontFacingCameraId++)  {
            Camera.CameraInfo info = new Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(frontFacingCameraId, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                break;
            }
        }

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);

        mCamera = Camera.open(frontFacingCameraId);
        try {
            mCamera.setPreviewTexture(mSTexture);
        } catch ( IOException ioe ) {
        }

        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= columns; j++) {
                programs.add(loadShader ( vss, fss ));
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
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
        param.set("orientation", "portrait");
        mCamera.setParameters(param);
        mCamera.startPreview();
        mCamera.startFaceDetection();
        mCamera.setFaceDetectionListener(this);
    }

    private void initTex() {
        hTex = new int[1];
        glGenTextures ( 1, hTex, 0 );
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

    private static int loadShader ( String vss, String fss ) {
        int vshader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vshader, vss);
        glCompileShader(vshader);
        int[] compiled = new int[1];
        glGetShaderiv(vshader, GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Shader", "Could not compile vshader");
            Log.v("Shader", "Could not compile vshader:"+glGetShaderInfoLog(vshader));
            glDeleteShader(vshader);
            vshader = 0;
        }

        int fshader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fshader, fss);
        glCompileShader(fshader);
        glGetShaderiv(fshader, GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Shader", "Could not compile fshader");
            Log.v("Shader", "Could not compile fshader:"+glGetShaderInfoLog(fshader));
            glDeleteShader(fshader);
            fshader = 0;
        }

        int program = glCreateProgram();
        glAttachShader(program, vshader);
        glAttachShader(program, fshader);
        glLinkProgram(program);

        return program;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear( GL_COLOR_BUFFER_BIT );

        if (System.currentTimeMillis() > faceCurrent + 5000) {
            faceStart = 0;
        }

        synchronized(this) {
            if ( mUpdateST ) {
                mSTexture.updateTexImage();
                mUpdateST = false;
            }
        }

        boolean addToggle = false;

        for (int i = 0; i < verticeBuffers.size(); i++) {
            if (faceStart == 0) {
                for (int j = 0; j < 8; j++) {
                    verticeBuffers.get(i).put(j, originalVerticeBuffers.get(i).get(j));
                }
            }

            int hProgram = programs.get(i);
            glUseProgram(hProgram);

            int ph = glGetAttribLocation(hProgram, "vPosition");
            int tch = glGetAttribLocation(hProgram, "vTexCoord");
            int th = glGetUniformLocation(hProgram, "sTexture");

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
            glUniform1i(th, 0);

            FloatBuffer buffer = verticeBuffers.get(i);
            // buffer: brX, brY, blX, blY, trX, trY, tlX, tlY
            // pTexCoord: left, bottom, left, bottom + height, left + width, bottom, left + width, bottom + height

            float bufferWidth = buffer.get(0) - buffer.get(2);
            float bufferHeight = buffer.get(5) - buffer.get(1);

            if (inEllipse(buffer.get(5) - bufferHeight/2, -1f * (buffer.get(0) - bufferWidth/2), originX, originY, radiusHorizontal, radiusVertical) && faceStart != 0) {

                addToggle = !addToggle;

                for (int j = 0; j < 8; j++) {
                    if (rand.nextBoolean())
                        buffer.put(j, buffer.get(j) + rand.nextFloat() / 1000);
                    else
                        buffer.put(j, buffer.get(j) - rand.nextFloat() / 1000);
                }


                glVertexAttribPointer(ph, 2, GL_FLOAT, false, 4 * 2, buffer);
                glVertexAttribPointer(tch, 2, GL_FLOAT, false, 4 * 2, pTexCoord);
                glEnableVertexAttribArray(ph);
                glEnableVertexAttribArray(tch);

                glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
            }
            glFlush();
        }
    }

    public boolean inSquare(float pointX, float pointY, float left, float right, float top, float bottom) {
        return pointX >= left && pointX <= right && pointY >= bottom && pointY <= top;
    }

    public boolean inEllipse(float pointX, float pointY, float originX, float originY, float xRadius, float yRadius) {
        return ((Math.pow(pointX - originX, 2)/Math.pow(xRadius, 2)) + (Math.pow(pointY - originY, 2)/Math.pow(yRadius, 2)) <= 1);
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if (faces.length > 0) {
            faceCurrent = System.currentTimeMillis();

            if (faceStart == 0)
                faceStart = faceCurrent;

            faceRect = faces[0].rect;

            originX = faceRect.exactCenterX() / 1000f;
            originY = faceRect.exactCenterY() / 1000f;
            radiusHorizontal = faceRect.width() / 2000f;
            radiusVertical = faceRect.height() / 2000f;

            float left = ((faceRect.left / 1000f) + 1f) / 2;
            float bottom = ((faceRect.top / 1000f) + 1f) / 2;
            float width = faceRect.width() / 2000f;
            float height = faceRect.height() / 2000f;

            float[] ttmp  = { left, bottom, left, bottom + height, left + width, bottom, left + width, bottom + height };

            pTexCoord.put(ttmp);
            pTexCoord.position(0);
        }
    }
}

