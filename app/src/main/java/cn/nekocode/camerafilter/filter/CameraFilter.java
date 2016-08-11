/*
 * Copyright 2016 nekocode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.nekocode.camerafilter.filter;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by nekocode on 16/8/6.
 */
public abstract class CameraFilter {
    final long START_TIME = System.currentTimeMillis();

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
    static FloatBuffer defaultVertexBuffer, defaultRoatedTextureCoordBuffer;

    public CameraFilter() {
        // Setup default VertexBuffers
        if (defaultVertexBuffer == null) {
            defaultVertexBuffer = ByteBuffer.allocateDirect(squareCoords.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            defaultVertexBuffer.put(squareCoords);
            defaultVertexBuffer.position(0);
        }

        if (defaultRoatedTextureCoordBuffer == null) {
            defaultRoatedTextureCoordBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            defaultRoatedTextureCoordBuffer.put(textureCoords);
            defaultRoatedTextureCoordBuffer.position(0);
        }
    }

    void defaultDraw(int program, int textureId, int gwidth, int gheight) {
        // Use shaders
        GLES20.glUseProgram(program);

        int iResolution = GLES20.glGetUniformLocation(program, "iResolution");
        // FIXME: Because we roated the texture by 90 degresss, so we need to exchange the width and height
        final float res[] = {(float) gheight, (float) gwidth, 1.0f};
        GLES20.glUniform3fv(iResolution, 1, res, 0);

        float time = ((float) (System.currentTimeMillis() - START_TIME)) / 1000.0f;
        int iGlobalTime = GLES20.glGetUniformLocation(program, "iGlobalTime");
        GLES20.glUniform1f(iGlobalTime, time);

        int vPositionLocation = GLES20.glGetAttribLocation(program, "vPosition");
        int vTexCoordLocation = GLES20.glGetAttribLocation(program, "vTexCoord");
        int sTextureLocation = GLES20.glGetUniformLocation(program, "sTexture");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(sTextureLocation, 0); // First layer texture

        GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, defaultVertexBuffer);
        GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, defaultRoatedTextureCoordBuffer);
        GLES20.glEnableVertexAttribArray(vPositionLocation);
        GLES20.glEnableVertexAttribArray(vTexCoordLocation);

        // Draw the texture
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    abstract public void draw(int textureId, int textureWidth, int textureHeight);
}
