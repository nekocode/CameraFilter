package cn.nekocode.camerafilter.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by nekocode on 16/8/6.
 */
public abstract class CameraFilter {
    private static long START_TIME = System.currentTimeMillis();
    Context context;

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
    private FloatBuffer defaultVertexBuffer, defaultTextureCoordBuffer;

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

    void defaultDraw(int program, int textureId, int gwidth, int gheight) {
        // Use shaders
        GLES20.glUseProgram(program);

        int iResolution = GLES20.glGetUniformLocation(program, "iResolution");
        final float res[] = {(float) gwidth, (float) gheight, 1.0f};
        GLES20.glUniform3fv(iResolution, 1, res, 0);

        float time = ((float) (System.currentTimeMillis() - START_TIME)) / 1000.0f;
        int iGlobalTime = GLES20.glGetUniformLocation(program, "iGlobalTime");
        GLES20.glUniform1f(iGlobalTime, time);

        int vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        int vTexCoord = GLES20.glGetAttribLocation(program, "vTexCoord");
        int sTexture = GLES20.glGetUniformLocation(program, "sTexture");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(sTexture, 0); // First layer texture

        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 4 * 2, defaultVertexBuffer);
        GLES20.glVertexAttribPointer(vTexCoord, 2, GLES20.GL_FLOAT, false, 4 * 2, defaultTextureCoordBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glEnableVertexAttribArray(vTexCoord);

        // Draw the texture
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    abstract public void draw(int textureId, int textureWidth, int textureHeight);
}
