package cn.nekocode.camerafilter.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import cn.nekocode.camerafilter.MyGLUtils;
import cn.nekocode.camerafilter.R;

/**
 * Created by nekocode on 16/8/6.
 */
public class RefractionFilter extends CameraFilter {
    private int program;
    private int texture2Id;

    public RefractionFilter(Context context) {
        super(context);

        // Build shaders
        program = MyGLUtils.buildProgram(context, R.raw.vertext, R.raw.refraction);

        // Load the texture will need for the shader
        texture2Id = MyGLUtils.loadTexture(context, R.raw.tex11);
    }

    @Override
    public void draw(int textureId, int gwidth, int gheight) {
        // Use shaders
        GLES20.glUseProgram(program);

        int iResolution = GLES20.glGetUniformLocation(program, "iResolution");
        // FIXME: Because we roate the texture, so we need to exchange the width and height
        final float res[] = {(float) gheight, (float) gwidth, 1.0f};
        GLES20.glUniform3fv(iResolution, 1, res, 0);

        float time = ((float) (System.currentTimeMillis() - START_TIME)) / 1000.0f;
        int iGlobalTime = GLES20.glGetUniformLocation(program, "iGlobalTime");
        GLES20.glUniform1f(iGlobalTime, time);

        int vPositionLocation = GLES20.glGetAttribLocation(program, "vPosition");
        int vTexCoordLocation = GLES20.glGetAttribLocation(program, "vTexCoord");
        int sTextureLocation = GLES20.glGetUniformLocation(program, "sTexture");
        int sTexture2Location = GLES20.glGetUniformLocation(program, "sTexture2");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(sTextureLocation, 0); // First layer texture

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2Id);
        GLES20.glUniform1i(sTexture2Location, 1); // Second layer texture

        GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, defaultVertexBuffer);
        GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, defaultTextureCoordBuffer);
        GLES20.glEnableVertexAttribArray(vPositionLocation);
        GLES20.glEnableVertexAttribArray(vTexCoordLocation);

        // Draw the texture
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
