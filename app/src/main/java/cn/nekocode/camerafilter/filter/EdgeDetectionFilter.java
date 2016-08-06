package cn.nekocode.camerafilter.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import cn.nekocode.camerafilter.MyGLUtils;
import cn.nekocode.camerafilter.R;

/**
 * Created by nekocode on 16/8/6.
 */
public class EdgeDetectionFilter extends CameraFilter {
    private int program;

    public EdgeDetectionFilter(Context context) {
        super(context);

        // Build shaders
        program = MyGLUtils.buildProgram(context, R.raw.vertext, R.raw.edge_detection);
    }

    @Override
    public void draw(int textureId) {
        // Use shaders
        GLES20.glUseProgram(program);

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
}
