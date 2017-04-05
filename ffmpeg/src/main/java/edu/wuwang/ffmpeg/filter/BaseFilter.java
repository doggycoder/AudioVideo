/*
 *
 * NoFilter.java
 * 
 * Created by Wuwang on 2016/11/19
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package edu.wuwang.ffmpeg.filter;

import android.content.res.Resources;

/**
 * Description:
 */
public class BaseFilter extends AFilter {

    public BaseFilter(Resources res) {
        super(res);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base.vert", "shader/base.frag");
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
