package cn.nekocode.camerafilter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;

public class MainActivity extends AppCompatActivity {
    private TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textureView = new TextureView(this);
        textureView.setSurfaceTextureListener(new CameraSurfaceTexutreListener(this));

        setContentView(textureView);
    }
}
