package cn.nekocode.camerafilter;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * Created by nekocode on 16/8/5.
 */
public class RenderThread extends Thread {
    private static final int EGL_OPENGL_ES2_BIT = 4;
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final String TAG = "RenderThread";

    private Activity activity;
    private SurfaceTexture cameraSurfaceTexture;
    private int cameraTextureId;

    private SurfaceTexture surfaceTexture;
    private EGLDisplay eglDisplay;
    private EGLSurface eglSurface;
    private EGLContext eglContext;
    private int program;
    private EGL10 egl10;
    private GL11 gl11;

    private static final float squareCoords[] = {
            1.0f, -1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f,
    };
    private static final float textureCoords[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };

    private FloatBuffer vertexBuffer, textureCoordBuffer;

    public RenderThread(Activity activity, SurfaceTexture surfaceTexture, Camera camera) {
        this.activity = activity;
        this.surfaceTexture = surfaceTexture;

        setupVertexBuffer();

        // Create texture for camera preview
        cameraTextureId = createTextureID();
        cameraSurfaceTexture = new SurfaceTexture(cameraTextureId);

        try {
            camera.setPreviewTexture(cameraSurfaceTexture);
            camera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }
    }

    public void release() {
        cameraSurfaceTexture.release();
        GLES20.glDeleteTextures(1, new int[]{cameraTextureId}, 0);
        this.interrupt();
    }

    private int createTextureID() {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    private void setupVertexBuffer() {
        vertexBuffer = ByteBuffer.allocateDirect(squareCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        textureCoordBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureCoordBuffer.put(textureCoords);
        textureCoordBuffer.position(0);
    }

    @Override
    public void run() {
        initGL();

        while (!this.isInterrupted()) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            // Update the camera preview texture
            synchronized (this) {
                cameraSurfaceTexture.updateTexImage();
            }

            // Setup shaders
            GLES20.glUseProgram(program);

            int vPosition = GLES20.glGetAttribLocation(program, "vPosition");
            int vTexCoord = GLES20.glGetAttribLocation(program, "vTexCoord");
            int sTexture = GLES20.glGetUniformLocation(program, "sTexture");

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureId);
            GLES20.glUniform1i(sTexture, 0); // First layer texture

            GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 4 * 2, vertexBuffer);
            GLES20.glVertexAttribPointer(vTexCoord, 2, GLES20.GL_FLOAT, false, 4 * 2, textureCoordBuffer);
            GLES20.glEnableVertexAttribArray(vPosition);
            GLES20.glEnableVertexAttribArray(vTexCoord);

            // Draw the texture
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLES20.glFlush();
            egl10.eglSwapBuffers(eglDisplay, eglSurface);

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    private int buildProgram(String vertexSource, String fragmentSource) {
        final int vertexShader = buildShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        final int fragmentShader = buildShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == 0) {
            return 0;
        }

        final int program = GLES20.glCreateProgram();
        if (program == 0) {
            return 0;
        }

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        return program;
    }

    private int buildShader(int type, String shaderSource) {
        final int shader = GLES20.glCreateShader(type);
        if (shader == 0) {
            return 0;
        }

        GLES20.glShaderSource(shader, shaderSource);
        GLES20.glCompileShader(shader);

        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    private void initGL() {
        egl10 = (EGL10) EGLContext.getEGL();

        eglDisplay = egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed " + GLUtils.getEGLErrorString(egl10.eglGetError()));
        }

        int[] version = new int[2];
        if (!egl10.eglInitialize(eglDisplay, version)) {
            throw new RuntimeException("eglInitialize failed " + GLUtils.getEGLErrorString(egl10.eglGetError()));
        }

        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] configSpec = {
                EGL10.EGL_RENDERABLE_TYPE,
                EGL_OPENGL_ES2_BIT,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE
        };

        EGLConfig eglConfig = null;
        if (!egl10.eglChooseConfig(eglDisplay, configSpec, configs, 1, configsCount)) {
            throw new IllegalArgumentException("eglChooseConfig failed " + GLUtils.getEGLErrorString(egl10.eglGetError()));
        } else if (configsCount[0] > 0) {
            eglConfig = configs[0];
        }
        if (eglConfig == null) {
            throw new RuntimeException("eglConfig not initialized");
        }

        int[] attrib_list = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
        eglContext = egl10.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
        eglSurface = egl10.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, null);

        if (eglSurface == null || eglSurface == EGL10.EGL_NO_SURFACE) {
            int error = egl10.eglGetError();
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                Log.e(TAG, "eglCreateWindowSurface returned EGL10.EGL_BAD_NATIVE_WINDOW");
                return;
            }
            throw new RuntimeException("eglCreateWindowSurface failed " + GLUtils.getEGLErrorString(error));
        }

        if (!egl10.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw new RuntimeException("eglMakeCurrent failed " + GLUtils.getEGLErrorString(egl10.eglGetError()));
        }

        gl11 = (GL11) eglContext.getGL();
        program = buildProgram(getStringFromRaw(R.raw.vertext), getStringFromRaw(R.raw.fragment));
    }

    private String getStringFromRaw(int id) {
        String str;
        try {
            Resources r = activity.getResources();
            InputStream is = r.openRawResource(id);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int i = is.read();
            while (i != -1) {
                baos.write(i);
                i = is.read();
            }

            str = baos.toString();
            is.close();
        } catch (IOException e) {
            str = "";
        }

        return str;
    }
}