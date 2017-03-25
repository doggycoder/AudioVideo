/*
 * Created by Wuwang on 2017/3/25
 * Copyright © 2017年 深圳哎吖科技. All rights reserved.
 */
package edu.wuwang.ffmpeg;

/**
 * Description:
 */
public class FFMpeg {

    public native String getConfiguration();

    public native void init();

    public native void setOutput(String path);

    public native void start();

    public native void writeFrame(byte[] frame);

    public native void set(int key,int value);

    public native void release();

    static {
        System.loadLibrary("FFMpeg");
    }

}
