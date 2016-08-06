package cn.nekocode.camerafilter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.TextureView;

import java.io.IOException;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import cn.nekocode.camerafilter.filter.*;

/**
 * Created by nekocode on 16/8/5.
 */
public class CameraRenderer extends Thread implements TextureView.SurfaceTextureListener {
    private static final String TAG = "CameraRenderer";
    private static final int EGL_OPENGL_ES2_BIT = 4;
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final int DRAW_INTERVAL = 1000 / 30;

    private Context context;
    private SurfaceTexture surfaceTexture;
    private int gwidth, gheight;

    private EGLDisplay eglDisplay;
    private EGLSurface eglSurface;
    private EGLContext eglContext;
    private EGL10 egl10;

    private Camera camera;
    private SurfaceTexture cameraSurfaceTexture;
    private int cameraTextureId;
    private CameraFilter cameraFilter;
    private SparseArray<CameraFilter> cameraFilterMap = new SparseArray<>();

    private boolean exit = false;

    public CameraRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        GLES20.glViewport(0, 0, gwidth = width, gheight = height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        camera.stopPreview();
        camera.release();
        exit = true;

        return true;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        surfaceTexture = surface;

        // Open camera
        Pair<Camera.CameraInfo, Integer> backCamera = getBackCamera();
        final int backCameraId = backCamera.second;
        camera = Camera.open(backCameraId);

        // Start rendering
        start();
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
        cameraFilterMap.append(R.id.filter4, new TrianglesMosaicFilter(context));
        cameraFilterMap.append(R.id.filter5, new LegofiedFilter(context));
        cameraFilterMap.append(R.id.filter6, new TileMosaicFilter(context));
        cameraFilter = cameraFilterMap.get(R.id.filter0);

        try {
            camera.setPreviewTexture(cameraSurfaceTexture);
            camera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }

        while (!exit) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            // Update the camera preview texture
            synchronized (this) {
                cameraSurfaceTexture.updateTexImage();
            }

            // Draw camera preview
            cameraFilter.draw(cameraTextureId, gwidth, gheight);

            // Flush
            GLES20.glFlush();
            egl10.eglSwapBuffers(eglDisplay, eglSurface);

            try {
                Thread.sleep(DRAW_INTERVAL);
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        cameraSurfaceTexture.release();
        GLES20.glDeleteTextures(1, new int[]{cameraTextureId}, 0);
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
    }

    private Pair<Camera.CameraInfo, Integer> getBackCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        final int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; ++i) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return new Pair<>(cameraInfo, i);
            }
        }
        return null;
    }
}