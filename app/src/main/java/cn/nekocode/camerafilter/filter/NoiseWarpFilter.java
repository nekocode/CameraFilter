package cn.nekocode.camerafilter.filter;

import android.content.Context;

import cn.nekocode.camerafilter.MyGLUtils;
import cn.nekocode.camerafilter.R;

/**
 * Created by nekocode on 16/8/6.
 */
public class NoiseWarpFilter extends CameraFilter {
    private int program;

    public NoiseWarpFilter(Context context) {
        super(context);

        // Build shaders
        program = MyGLUtils.buildProgram(context, R.raw.vertext, R.raw.noise_warp);
    }

    @Override
    public void draw(int textureId, int gwidth, int gheight) {
        defaultDraw(program, textureId, gwidth, gheight);
    }
}
