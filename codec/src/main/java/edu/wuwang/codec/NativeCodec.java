package edu.wuwang.codec;

/**
 * Created by aiya on 2017/8/29.
 */

public class NativeCodec {

    public static final int KEY_WIDTH=0x0011;
    public static final int KEY_HEIGHT=0x0012;
    public static final int KEY_FORMAT=0x0013;
    public static final int KEY_LINE_SIZE=0x0014;
    
    public static native long _createObject();
    public static native int _openVideo(long id,String path);
    public static native int _decode(long id,byte[] data);
    public static native int _closeVideo(long id);
    public static native int _release(long id);
    public static native int _getIntValue(long id,int key);
    public static native int _getIntValue(long id,int key,int[] value);

    private long id;

    public NativeCodec(){
        id=_createObject();
    }

    public int openVideo(String path){
        return _openVideo(id,path);
    }

    public int decode(byte[] data){
        return _decode(id,data);
    }

    public int closeVideo(){
        return _closeVideo(id);
    }

    public int release(){
        return _release(id);
    }

    public int getWidth(){
        return _getIntValue(id,KEY_WIDTH);
    }

    public int getHeight(){
        return _getIntValue(id,KEY_HEIGHT);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        _release(id);
    }

    static{
        System.loadLibrary("VideoConvert");
    }
}
