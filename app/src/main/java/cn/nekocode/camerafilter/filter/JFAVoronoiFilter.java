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

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import cn.nekocode.camerafilter.MyGLUtils;
import cn.nekocode.camerafilter.R;

/**
 * Created by nekocode on 16/8/6.
 */
public class JFAVoronoiFilter extends CameraFilter {
    private int programImg;
    private int programA;
    private int programB;
    private int programC;

    private RenderBuffer bufA;
    private RenderBuffer bufB;
    private RenderBuffer bufC;

    private static final float nonRoatedTexCoords[] = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };
    private FloatBuffer nonRoatedTexCoordBuffer;

    private int iFrame = 0;
    private int inputWidth, inputHeight;

    public JFAVoronoiFilter(Context context, int inputWidth, int inputHeight) {
        super();
        this.inputWidth = inputWidth/6;
        this.inputHeight = inputHeight/6;

        // Build shaders
        programImg = MyGLUtils.buildProgram(context, R.raw.vertext, R.raw.voronoi);
        programA = MyGLUtils.buildProgram(context, R.raw.vertext, R.raw.voronoi_buf_a);
        programB = MyGLUtils.buildProgram(context, R.raw.vertext, R.raw.voronoi_buf_b);
        programC = MyGLUtils.buildProgram(context, R.raw.vertext, R.raw.voronoi_buf_c);

        // Create new textures for buffering
        bufA = new RenderBuffer(this.inputWidth, this.inputHeight, GLES20.GL_TEXTURE1);
        bufB = new RenderBuffer(this.inputWidth, this.inputHeight, GLES20.GL_TEXTURE2);
        bufC = new RenderBuffer(this.inputWidth, this.inputHeight, GLES20.GL_TEXTURE3);

        // Create non-roated texture coord buffer
        nonRoatedTexCoordBuffer = ByteBuffer.allocateDirect(nonRoatedTexCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        nonRoatedTexCoordBuffer.put(nonRoatedTexCoords);
        nonRoatedTexCoordBuffer.position(0);
    }

    public void resetFrame() {
        iFrame = 0;
    }

    private void initShader(int program, int gwidth, int gheight, FloatBuffer textureCoordBuffer) {
        int iResolution = GLES20.glGetUniformLocation(program, "iResolution");
        final float res[] = {(float) gwidth, (float) gheight, 1.0f};
        GLES20.glUniform3fv(iResolution, 1, res, 0);

        int iResolution2 = GLES20.glGetUniformLocation(program, "iResolution2");
        final float res2[] = {(float) inputWidth, (float) inputHeight, 1.0f};
        GLES20.glUniform3fv(iResolution2, 1, res2, 0);

        float time = ((float) (System.currentTimeMillis() - START_TIME)) / 1000.0f;
        int iGlobalTime = GLES20.glGetUniformLocation(program, "iGlobalTime");
        GLES20.glUniform1f(iGlobalTime, time);

        int vPositionLocation = GLES20.glGetAttribLocation(program, "vPosition");
        int vTexCoordLocation = GLES20.glGetAttribLocation(program, "vTexCoord");

        GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, defaultVertexBuffer);
        GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, textureCoordBuffer);
        GLES20.glEnableVertexAttribArray(vPositionLocation);
        GLES20.glEnableVertexAttribArray(vTexCoordLocation);
    }

    @Override
    public void draw(int textureId, int gwidth, int gheight) {
        // Use shaders
        GLES20.glUseProgram(programA);

        int sCameraLocation = GLES20.glGetUniformLocation(programA, "sCamera");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(sCameraLocation, 0);

        int sBufALocation = GLES20.glGetUniformLocation(programA, "sBufA");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bufA.getTexId());
        GLES20.glUniform1i(sBufALocation, 1);

        initShader(programA, inputWidth, inputHeight, nonRoatedTexCoordBuffer);

        // Draw to buffer A
        bufA.bind();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        bufA.unbind();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // =============

        GLES20.glUseProgram(programB);

        int iFrameLocation = GLES20.glGetUniformLocation(programB, "iFrame");
        GLES20.glUniform1i(iFrameLocation, iFrame);

        int sBufBLocation = GLES20.glGetUniformLocation(programB, "sBufB");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bufB.getTexId());
        GLES20.glUniform1i(sBufBLocation, 2);

        sBufALocation = GLES20.glGetUniformLocation(programB, "sBufA");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bufA.getTexId());
        GLES20.glUniform1i(sBufALocation, 1);

        initShader(programB, inputWidth, inputHeight, nonRoatedTexCoordBuffer);

        // Draw to buffer B
        bufB.bind();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        bufB.unbind();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // =============

        GLES20.glUseProgram(programC);

        iFrameLocation = GLES20.glGetUniformLocation(programC, "iFrame");
        GLES20.glUniform1i(iFrameLocation, iFrame);

        int sBufCLocation = GLES20.glGetUniformLocation(programC, "sBufC");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bufC.getTexId());
        GLES20.glUniform1i(sBufCLocation, 3);

        sBufBLocation = GLES20.glGetUniformLocation(programC, "sBufB");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bufB.getTexId());
        GLES20.glUniform1i(sBufBLocation, 2);

        initShader(programC, inputWidth, inputHeight, nonRoatedTexCoordBuffer);

        // Draw to buffer C
        bufC.bind();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        bufC.unbind();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


        // =============

        GLES20.glViewport(0, 0, gwidth, gheight);
        GLES20.glUseProgram(programImg);

        sBufCLocation = GLES20.glGetUniformLocation(programImg, "sBufC");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bufC.getTexId());
        GLES20.glUniform1i(sBufCLocation, 0);

        sBufALocation = GLES20.glGetUniformLocation(programImg, "sBufA");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bufA.getTexId());
        GLES20.glUniform1i(sBufALocation, 1);

        // Because we roated the texture by 90 degresss, so we need to exchange the width and height
        initShader(programImg, gheight, gwidth, defaultRoatedTextureCoordBuffer);

        // Draw the texture
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        iFrame++;
    }

    private static class RenderBuffer {
        private int texId = 0;
        private int textureUnit = 0;
        private int renderBufferId = 0;
        private int frameBufferId = 0;

        private int width, height;

        public RenderBuffer(int width, int height, int activiteTextureUnit) {
            this.width = width;
            this.height = height;
            this.textureUnit = activiteTextureUnit;
            int[] genbuf = new int[1];

            // Generate render traget texture id
            GLES20.glGenTextures(1, genbuf, 0);
            texId = genbuf[0];

            // Generate and bind 2d texture
            GLES20.glActiveTexture(activiteTextureUnit);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
            IntBuffer texBuffer =
                    ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, texBuffer);

            // Generate frame buffer
            GLES20.glGenFramebuffers(1, genbuf, 0);
            frameBufferId = genbuf[0];

            // Bind frame buffer
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);

            // Generate render buffer
            GLES20.glGenRenderbuffers(1, genbuf, 0);
            renderBufferId = genbuf[0];

            // Bind render buffer
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferId);
            GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);

            // Set text parameters
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

            unbind();
        }

        public int getTexId() {
            return texId;
        }

        public void bind() {
            GLES20.glViewport(0, 0, width, height);

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, texId, 0);
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER, renderBufferId);
        }

        void unbind() {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
    }
}
