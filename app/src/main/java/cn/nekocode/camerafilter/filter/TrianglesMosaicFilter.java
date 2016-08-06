package cn.nekocode.camerafilter.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import cn.nekocode.camerafilter.MyGLUtils;
import cn.nekocode.camerafilter.R;

/**
 * Created by nekocode on 16/8/6.
 */
public class TrianglesMosaicFilter extends CameraFilter {
    private int program;

    public TrianglesMosaicFilter(Context context) {
        super(context);

        // Build shaders
        program = MyGLUtils.buildProgram(context, R.raw.vertext, R.raw.triangles_mosaic);
    }

    @Override
    public void draw(int textureId, int gwidht, int gheight) {
        defaultDraw(program, textureId, gwidht, gheight);
    }
}
