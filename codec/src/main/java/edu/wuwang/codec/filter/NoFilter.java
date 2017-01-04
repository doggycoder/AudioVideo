package edu.wuwang.codec.filter;

import android.content.res.Resources;

/**
 * Description:
 */
public class NoFilter extends AFilter {

    public NoFilter(Resources res) {
        super(res);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base_vertex.sh",
            "shader/base_fragment.sh");
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
