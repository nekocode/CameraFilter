package cn.nekocode.camerafilter.filter;

import android.content.Context;

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
    public void draw(int textureId, int gwidht, int gheight) {
        defaultDraw(program, textureId, gwidht, gheight);
    }
}
