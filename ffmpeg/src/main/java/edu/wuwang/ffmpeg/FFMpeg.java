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

    public static final int KEY_STR_CACHE_PATH=0x8001;

    public static final int EOF=-541478725;

    public static final int DECODER_H264=0xf001;
    public static final int DECODER_AAC=0xf002;
    public static final int DECODER_MP4=0xf003;
    public static final int DECODER_MP4_AAC=0xf004;
    public static final int DECODER_MP4_H264=0xf005;

    private static String cachePath="";

    public static native String getInfo();
    public static native void init();

    public native int start(int type);
    public native int input(byte[] data);
    public native int output(byte[] data);
    public native int stop();
    private static native void setInt(int key,int value);
    private static native void setStr(int key,String value);
    public native int get(int key);
    public native void release();

    public static void set(int key,int value){
        setInt(key, value);
    }

    public static void set(int key,String value){
        if(key==KEY_STR_CACHE_PATH){
            cachePath=value;
        }
        setStr(key, value);
    }

    public static String getCachePath(){
        return cachePath;
    }

    static {
        System.loadLibrary("FFMpeg");
    }

}
