package cn.nekocode.camerafilter.filter;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by nekocode on 16/8/6.
 */
public abstract class CameraFilter {
    Context context;

    static final float squareCoords[] = {
            1.0f, -1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f,
    };
    static final float textureCoords[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };
    FloatBuffer defaultVertexBuffer, defaultTextureCoordBuffer;


    public CameraFilter(Context context) {
        this.context = context;

        // Setup default VertexBuffers
        defaultVertexBuffer = ByteBuffer.allocateDirect(squareCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        defaultVertexBuffer.put(squareCoords);
        defaultVertexBuffer.position(0);

        defaultTextureCoordBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        defaultTextureCoordBuffer.put(textureCoords);
        defaultTextureCoordBuffer.position(0);
    }

    abstract public void draw(int textureId);
}
