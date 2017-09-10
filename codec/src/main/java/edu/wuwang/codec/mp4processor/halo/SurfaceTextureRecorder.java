package edu.wuwang.codec.mp4processor.halo;

import android.graphics.SurfaceTexture;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.view.Surface;

import java.io.IOException;

public class SurfaceTextureRecorder {

    private AudioRecord mAudioRecorder;
    private MediaCodec mVideoEncoder;
    private MediaCodec mAudioEncoder;
    private MediaMuxer mMuxer;
    private EGLHelper mEGLHelper;

    private Thread mGLThread;
    private Thread mAudioThread;
    private Thread mVideoThread;
    private Configuration mConfig;
    private Surface mOutputSurface;

    private SurfaceTexture mInputSurfaceTexture;
    private int mOutputWidth=0;
    private int mOutputHeight=0;
    private String mOutputPath;

    public void setInputSurfaceTexture(SurfaceTexture surface){
        this.mInputSurfaceTexture=surface;
    }

    public void setOutputpath(String path){
        this.mOutputPath=path;
    }

    public void setOutputSize(int width,int height){
        this.mConfig=new Configuration(width,height);
    }

    public void setConfiguration(Configuration config){
        this.mConfig=config;
    }

    public void setRendererSurface(Surface surface){
        this.mOutputSurface=surface;
    }

    public boolean prepare() throws IOException {
        MediaFormat audioFormat=mConfig.getAudioFormat();
        mAudioEncoder=MediaCodec.createEncoderByType(audioFormat.getString(MediaFormat.KEY_MIME));
        mAudioEncoder.configure(audioFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        MediaFormat videoFormat=mConfig.getVideoFormat();
        mVideoEncoder=MediaCodec.createEncoderByType(videoFormat.getString(MediaFormat.KEY_MIME));
        mVideoEncoder.configure(videoFormat,mOutputSurface,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        return false;
    }

    public boolean start(){
        mAudioEncoder.start();
        mVideoEncoder.start();
        return false;
    }

    public boolean stop(){
        return true;
    }

    public boolean release(){
        return true;
    }

    public static class Configuration{

        private MediaFormat mAudioFormat;
        private MediaFormat mVideoFormat;

        public Configuration(MediaFormat audio,MediaFormat video){
            this.mAudioFormat=audio;
            this.mVideoFormat=video;
        }

        public Configuration(int width,int height){
            mAudioFormat=MediaFormat.createAudioFormat("audio/aac",48000,2);
            mAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE,128000);
            mAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,MediaCodecInfo.CodecProfileLevel.AACObjectLC);

            mVideoFormat=MediaFormat.createVideoFormat("video/avc",width,height);
            mVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,24);
            mVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);
            mVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE,width*height*5);
            mVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        }

        public MediaFormat getAudioFormat(){
            return mAudioFormat;
        }

        public MediaFormat getVideoFormat(){
            return mVideoFormat;
        }

    }

}
