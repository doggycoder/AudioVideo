package edu.wuwang.codec.filter;

import android.content.res.Resources;

/**
 * Description:
 */
public class GrayFilter extends AFilter {

    public GrayFilter(Resources mRes) {
        super(mRes);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base_vertex.sh",
            "shader/color/gray_fragment.frag");
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
