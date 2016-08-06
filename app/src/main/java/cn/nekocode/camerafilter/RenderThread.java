package cn.nekocode.camerafilter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.util.Log;
import android.util.SparseArray;

import java.io.IOException;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL11;

import cn.nekocode.camerafilter.filter.*;

/**
 * Created by nekocode on 16/8/5.
 */
public class RenderThread extends Thread {
    private static final String TAG = "RenderThread";
    private static final int EGL_OPENGL_ES2_BIT = 4;
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final int DRAW_INTERVAL = 1000 / 30;

    private Context context;
    private SurfaceTexture surfaceTexture;
    private Camera camera;

    private EGLDisplay eglDisplay;
    private EGLSurface eglSurface;
    private EGLContext eglContext;
    private EGL10 egl10;
    private GL11 gl11;

    private SurfaceTexture cameraSurfaceTexture;
    private int cameraTextureId;
    private CameraFilter cameraFilter;
    private SparseArray<CameraFilter> cameraFilterMap = new SparseArray<>();

    public RenderThread(Context context, SurfaceTexture surfaceTexture, Camera camera) {
        this.context = context;
        this.surfaceTexture = surfaceTexture;
        this.camera = camera;
    }

    public void setCameraFilter(int id) {
        cameraFilter = cameraFilterMap.get(id);
    }

    @Override
    public void run() {
        initGL(surfaceTexture);

        // Create texture for camera preview
        cameraTextureId = MyGLUtils.createTextureID();
        cameraSurfaceTexture = new SurfaceTexture(cameraTextureId);

        cameraFilterMap.append(R.id.filter0, new OriginalFilter(context));
        cameraFilterMap.append(R.id.filter1, new EdgeDetectionFilter(context));
        cameraFilterMap.append(R.id.filter2, new PixelizeFilter(context));
        cameraFilterMap.append(R.id.filter3, new EMInterferenceFilter(context));
        cameraFilter = cameraFilterMap.get(R.id.filter0);

        try {
            camera.setPreviewTexture(cameraSurfaceTexture);
            camera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }

        while (!this.isInterrupted()) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            // Update the camera preview texture
            synchronized (this) {
                cameraSurfaceTexture.updateTexImage();
            }

            // Draw camera preview
            cameraFilter.draw(cameraTextureId);

            // Flush
            GLES20.glFlush();
            egl10.eglSwapBuffers(eglDisplay, eglSurface);

            try {
                Thread.sleep(DRAW_INTERVAL);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    public void Stop() {
        cameraSurfaceTexture.release();
        GLES20.glDeleteTextures(1, new int[]{cameraTextureId}, 0);
        this.interrupt();
    }

    private void initGL(SurfaceTexture texture) {
        egl10 = (EGL10) EGLContext.getEGL();

        eglDisplay = egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed " + android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }

        int[] version = new int[2];
        if (!egl10.eglInitialize(eglDisplay, version)) {
            throw new RuntimeException("eglInitialize failed " + android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
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
            throw new IllegalArgumentException("eglChooseConfig failed " + android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        } else if (configsCount[0] > 0) {
            eglConfig = configs[0];
        }
        if (eglConfig == null) {
            throw new RuntimeException("eglConfig not initialized");
        }

        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
        eglContext = egl10.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
        eglSurface = egl10.eglCreateWindowSurface(eglDisplay, eglConfig, texture, null);

        if (eglSurface == null || eglSurface == EGL10.EGL_NO_SURFACE) {
            int error = egl10.eglGetError();
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                Log.e(TAG, "eglCreateWindowSurface returned EGL10.EGL_BAD_NATIVE_WINDOW");
                return;
            }
            throw new RuntimeException("eglCreateWindowSurface failed " + android.opengl.GLUtils.getEGLErrorString(error));
        }

        if (!egl10.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw new RuntimeException("eglMakeCurrent failed " + android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }

        gl11 = (GL11) eglContext.getGL();
    }
}