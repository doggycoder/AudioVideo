/*
 * Created by Wuwang on 2017/3/25
 * Copyright © 2017年 深圳哎吖科技. All rights reserved.
 */
package edu.wuwang.ffmpeg;

/**
 * Description:
 */
public class FFMpeg {

    public static final int KEY_WIDTH=0x1001;
    public static final int KEY_HEIGHT=0x1002;
    public static final int KEY_BIT_RATE=0x2001;
    public static final int KEY_SAMPLE_RATE=0x2002;
    public static final int KEY_AUDIO_FORMAT=0x2003;
    public static final int KEY_CHANNEL_COUNT=0x2004;
    public static final int KEY_FRAME_SIZE=0x2005;

    public static final int EOF=-541478725;

    public static native String getInfo();
    public static native void init();

    public native int start();
    public native int input(byte[] data);
    public native int output(byte[] data);
    public native int stop();
    public native void set(int key,int value);
    public native int get(int key);
    public native void release();

    static {
        System.loadLibrary("FFMpeg");
    }

}
