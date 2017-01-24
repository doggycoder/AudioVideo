/*
 *
 * YooRecorder.java
 * 
 * Created by Wuwang on 2016/12/31
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package edu.wuwang.codec.coder;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.wuwang.jni.DataConvert;

/**
 * Description:
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CameraRecorder {

    public static final String TAG="RECORD";
    private final Object LOCK=new Object();

    private MediaMuxer mMuxer;  //多路复用器，用于音视频混合
    private String path;        //文件保存的路径
    private String postfix;     //文件后缀

    private String audioMime = "audio/mp4a-latm";   //音频编码的Mime
    private AudioRecord mRecorder;   //录音器
    private MediaCodec mAudioEnc;   //编码器，用于音频编码
    private int audioRate=128000;   //音频编码的密钥比特率
    private int sampleRate=48000;   //音频采样率
    private int channelCount=2;     //音频编码通道数
    private int channelConfig= AudioFormat.CHANNEL_IN_STEREO;   //音频录制通道,默认为立体声
    private int audioFormat=AudioFormat.ENCODING_PCM_16BIT; //音频录制格式，默认为PCM16Bit

//    private byte[] buffer;
    private boolean isRecording;
    private int bufferSize;

    private int convertType;

    private Thread mAudioThread;

    private MediaCodec mVideoEnc;
    private String videoMime="video/avc";   //视频编码格式
    private int videoRate=2048000;       //视频编码波特率
    private int frameRate=24;           //视频编码帧率
    private int frameInterval=1;        //视频编码关键帧，1秒一关键帧

    private int fpsTime;

    private Thread mVideoThread;
    private boolean mStartFlag=false;
    private int width;
    private int height;
//    private byte[] mHeadInfo=null;

    private byte[] nowFeedData;
//    private long nowTimeStep;
    private boolean hasNewData=false;

    private int mAudioTrack=-1;
    private int mVideoTrack=-1;
    private boolean isStop=true;

    private long nanoTime;

    private boolean cancelFlag=false;
    private boolean isAlign=false;

    public CameraRecorder(){
        fpsTime=1000/frameRate;
    }

    public void setSavePath(String path,String postfix){
        this.path=path;
        this.postfix=postfix;
    }

    public int prepare(int width,int height) throws IOException {
        //准备Audio
        MediaFormat format=MediaFormat.createAudioFormat(audioMime,sampleRate,channelCount);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, audioRate);
        mAudioEnc=MediaCodec.createEncoderByType(audioMime);
        mAudioEnc.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)*2;
//        buffer=new byte[bufferSize];
        mRecorder=new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,
            audioFormat,bufferSize);

        //准备Video
//        mHeadInfo=null;
        this.width=width;
        this.height=height;
        MediaFormat videoFormat=MediaFormat.createVideoFormat(videoMime,width,height);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE,videoRate);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,frameRate);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,frameInterval);

        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,checkColorFormat(videoMime));
        mVideoEnc=MediaCodec.createEncoderByType(videoMime);
        mVideoEnc.configure(videoFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        Bundle bundle=new Bundle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bundle.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE,videoRate);
            mVideoEnc.setParameters(bundle);
        }

        mMuxer=new MediaMuxer(path+"."+postfix, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        return 0;
    }

    public int start() throws InterruptedException {
        //记录起始时间
        nanoTime=System.nanoTime();
        synchronized (LOCK){
            //Audio Start
            if(mAudioThread!=null&&mAudioThread.isAlive()){
                isRecording=false;
                mAudioThread.join();
            }
            if(mVideoThread!=null&&mVideoThread.isAlive()){
                mStartFlag=false;
                mVideoThread.join();
            }

            mAudioEnc.start();
            mRecorder.startRecording();
            isRecording=true;
            mAudioThread=new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!cancelFlag){
                        try {
                            if(audioStep()){
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            mAudioThread.start();


            mVideoEnc.start();
            mStartFlag=true;
            mVideoThread=new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!cancelFlag){
                        long time=System.currentTimeMillis();
                        if(nowFeedData!=null){
                            try {
                                if(videoStep(nowFeedData)){
                                    break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        long lt=System.currentTimeMillis()-time;
                        if(fpsTime>lt){
                            try {
                                Thread.sleep(fpsTime-lt);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            mVideoThread.start();
        }
        return 0;
    }

    public void cancel(){
        cancelFlag=true;
        stop();
        cancelFlag=false;
        File file=new File(path);
        if(file.exists()){
            file.delete();
        }
    }

    public void stop(){
        try {
            synchronized (LOCK){
                isRecording=false;
                mAudioThread.join();
                mStartFlag=false;
                mVideoThread.join();
                //Audio Stop
                mRecorder.stop();
                mAudioEnc.stop();
                mAudioEnc.release();

                //Video Stop
                mVideoEnc.stop();
                mVideoEnc.release();

                //Muxer Stop
                mVideoTrack=-1;
                mAudioTrack=-1;
                mMuxer.stop();
                mMuxer.release();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 由外部喂入一帧数据
     * @param data RGBA数据
     * @param timeStep camera附带时间戳
     */
    public void feedData(final byte[] data, final long timeStep){
        hasNewData=true;
        nowFeedData=data;
//        nowTimeStep=timeStep;
    }

    private ByteBuffer getInputBuffer(MediaCodec codec,int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getInputBuffer(index);
        }else{
            return codec.getInputBuffers()[index];
        }
    }

    private ByteBuffer getOutputBuffer(MediaCodec codec,int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getOutputBuffer(index);
        }else{
            return codec.getOutputBuffers()[index];
        }
    }

    //TODO Add End Flag
    private boolean audioStep() throws IOException{
        int index=mAudioEnc.dequeueInputBuffer(-1);
        if(index>=0){
            final ByteBuffer buffer=getInputBuffer(mAudioEnc,index);
            buffer.clear();
            int length=mRecorder.read(buffer,bufferSize);
            if(length>0){
                mAudioEnc.queueInputBuffer(index,0,length,(System.nanoTime()-nanoTime)/1000,isRecording?0:MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            }else{
                Log.e("wuwang","length-->"+length);
            }
        }
        MediaCodec.BufferInfo mInfo=new MediaCodec.BufferInfo();
        int outIndex;
        do{
            outIndex=mAudioEnc.dequeueOutputBuffer(mInfo,0);
            Log.e("wuwang","audio flag---->"+mInfo.flags+"/"+outIndex);
            if(outIndex>=0){
                ByteBuffer buffer=getOutputBuffer(mAudioEnc,outIndex);
                buffer.position(mInfo.offset);
//                byte[] temp=new byte[mInfo.size+7];
//                buffer.get(temp,7,mInfo.size);
//                addADTStoPacket(temp,temp.length);
                if(mAudioTrack>=0&&mVideoTrack>=0&&mInfo.size>0&&mInfo.presentationTimeUs>0){
                    try {
                        mMuxer.writeSampleData(mAudioTrack,buffer,mInfo);
                    }catch (Exception e){
                        Log.e(TAG,"audio error:size="+mInfo.size+"/offset="
                            +mInfo.offset+"/timeUs="+mInfo.presentationTimeUs);
                        e.printStackTrace();
                    }
                }
                mAudioEnc.releaseOutputBuffer(outIndex,false);
                if((mInfo.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0){
                    Log.e(TAG,"audio end");
                    return true;
                }
            }else if(outIndex ==MediaCodec.INFO_TRY_AGAIN_LATER){

            }else if(outIndex==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                mAudioTrack=mMuxer.addTrack(mAudioEnc.getOutputFormat());
                Log.e(TAG,"add audio track-->"+mAudioTrack);
                if(mAudioTrack>=0&&mVideoTrack>=0){
                    mMuxer.start();
                }
            }
        }while (outIndex>=0);
        return false;
    }

    /**
     * 给编码出的aac裸流添加adts头字段
     * @param packet 要空出前7个字节，否则会搞乱数据
     * @param packetLen
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //CPE
        packet[0] = (byte)0xFF;
        packet[1] = (byte)0xF9;
        packet[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2));
        packet[3] = (byte)(((chanCfg&3)<<6) + (packetLen>>11));
        packet[4] = (byte)((packetLen&0x7FF) >> 3);
        packet[5] = (byte)(((packetLen&7)<<5) + 0x1F);
        packet[6] = (byte)0xFC;
    }

    //TODO 定时调用，如果没有新数据，就用上一个数据
    private boolean videoStep(byte[] data) throws IOException {
        int index=mVideoEnc.dequeueInputBuffer(-1);
        if(index>=0){
            if(hasNewData){
                if(yuv==null){
                    yuv=new byte[width*height*3/2];
                }
                DataConvert.rgbaToYuv(data,width,height,yuv,convertType);
            }
            ByteBuffer buffer=getInputBuffer(mVideoEnc,index);
            buffer.clear();
            buffer.put(yuv);
            mVideoEnc.queueInputBuffer(index,0,yuv.length,(System.nanoTime()-nanoTime)/1000,mStartFlag?0:MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        }
        MediaCodec.BufferInfo mInfo=new MediaCodec.BufferInfo();
        int outIndex=mVideoEnc.dequeueOutputBuffer(mInfo,0);
        do {
            if(outIndex>=0){
                ByteBuffer outBuf=getOutputBuffer(mVideoEnc,outIndex);
                if(mAudioTrack>=0&&mVideoTrack>=0&&mInfo.size>0&&mInfo.presentationTimeUs>0){
                    try {
                        mMuxer.writeSampleData(mVideoTrack,outBuf,mInfo);
                    }catch (Exception e){
                        Log.e(TAG,"video error:size="+mInfo.size+"/offset="
                            +mInfo.offset+"/timeUs="+mInfo.presentationTimeUs);
                        //e.printStackTrace();
                        Log.e(TAG,"-->"+e.getMessage());
                    }
                }
                mVideoEnc.releaseOutputBuffer(outIndex,false);
                outIndex=mVideoEnc.dequeueOutputBuffer(mInfo,0);
                if((mInfo.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0){
                    Log.e(TAG,"video end");
                    return true;
                }
            }else if(outIndex==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                mVideoTrack=mMuxer.addTrack(mVideoEnc.getOutputFormat());
                Log.e(TAG,"add video track-->"+mVideoTrack);
                if(mAudioTrack>=0&&mVideoTrack>=0){
                    mMuxer.start();
                }
            }
        }while (outIndex>=0);
        return false;
    }

    byte[] yuv;

    private int checkColorFormat(String mime){
        if(Build.MODEL.equals("HUAWEI P6-C00")){
            convertType=DataConvert.BGRA_YUV420SP;
            return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        }
        for (int i = 0; i< MediaCodecList.getCodecCount(); i++){
            MediaCodecInfo info= MediaCodecList.getCodecInfoAt(i);
            if(info.isEncoder()){
                String[] types=info.getSupportedTypes();
                for (String type:types){
                    if(type.equals(mime)){
                        Log.e("YUV","type-->"+type);
                        MediaCodecInfo.CodecCapabilities c=info.getCapabilitiesForType(type);
                        Log.e("YUV","color-->"+ Arrays.toString(c.colorFormats));
                        for (int j=0;j<c.colorFormats.length;j++){
                            if (c.colorFormats[j]==MediaCodecInfo.CodecCapabilities
                                .COLOR_FormatYUV420Planar){
                                convertType=DataConvert.RGBA_YUV420P;
                                return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
                            }else if(c.colorFormats[j]==MediaCodecInfo.CodecCapabilities
                                .COLOR_FormatYUV420SemiPlanar){
                                convertType=DataConvert.RGBA_YUV420SP;
                                return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
                            }
                        }
                    }
                }
            }
        }
        convertType=DataConvert.RGBA_YUV420SP;
        return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
    }
}
