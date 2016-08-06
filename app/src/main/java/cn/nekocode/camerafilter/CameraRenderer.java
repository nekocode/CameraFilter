package cn.nekocode.camerafilter;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Pair;
import android.view.TextureView;

/**
 * Created by nekocode on 16/8/5.
 */
public class CameraRenderer implements TextureView.SurfaceTextureListener {
    private Camera camera;
    private Activity activity;
    private RenderThread renderThread;

    public CameraRenderer(Activity activity) {
        this.activity = activity;
    }

    public void setCameraFilter(int id) {
        if (renderThread != null)
            renderThread.setCameraFilter(id);
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        camera.stopPreview();
        camera.release();
        renderThread.Stop();

        return true;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // Open camera
        Pair<Camera.CameraInfo, Integer> backCamera = getBackCamera();
        final int backCameraId = backCamera.second;
        camera = Camera.open(backCameraId);

        // Start rendering
        renderThread = new RenderThread(activity, surface, camera);
        renderThread.start();
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