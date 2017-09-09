package com.wuwang.jni;

/**
 * Created by aiya on 2017/9/6.
 */

public class GLExport {

    private long nativeId;

    public GLExport(int width,int height){
        nativeId=_createObject(width, height);
    }

    public int readPixels(byte[] data,int width,int height){
        return _readPixels(nativeId,data,width,height);
    }

    private static native long _createObject(int width,int height);

    private static native int _readPixels(long id,byte[] data,int width,int height);

    private static native void _release(long id);

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        _release(nativeId);
    }


    static {
        System.loadLibrary("dec");
        System.loadLibrary("VideoConvert");
    }
}
