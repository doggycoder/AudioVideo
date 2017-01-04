package edu.wuwang.codec.coder;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

/**
 * Description:
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class YooRecorder{

    private MediaMuxer mMuxer;  //多路复用器，用于音视频混合
    private String path;        //文件保存的路径
    private String postfix;     //文件后缀

    private String audioMime = "audio/mp4a-latm";   //音频编码的Mime
    private AudioRecord mRecoder;   //录音器
    private MediaCodec mAudioEnc;   //编码器，用于音频编码
    private int audioRate=128000;   //音频编码的密钥比特率
    private int sampleRate=48000;   //音频采样率
    private int channelCount=2;     //音频编码通道数
    private int channelConfig= AudioFormat.CHANNEL_IN_STEREO;   //音频录制通道,默认为立体声
    private int audioFormat=AudioFormat.ENCODING_PCM_16BIT; //音频录制格式，默认为PCM16Bit

//    private byte[] buffer;
    private boolean isRecording;
    private int bufferSize;

    private Thread mAudioThread;

    private MediaCodec mVideoEnc;
    private String videoMime="video/avc";
    private int videoRate=512000;
    private int frameRate=24;
    private int frameInterval=1;

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
    private int mTrackCount=0;

    private long nanoTime;

    public YooRecorder(){
        fpsTime=1000/frameRate;
    }

    public void setSavePath(String path,String postfix){
        this.path=path;
        this.postfix=postfix;
    }

    public int prepare(int width,int height) throws EncoderException, IOException {
        //准备Audio
        MediaFormat format=MediaFormat.createAudioFormat(audioMime,sampleRate,channelCount);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, audioRate);
        mAudioEnc=MediaCodec.createEncoderByType(audioMime);
        mAudioEnc.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)*2;
//        buffer=new byte[bufferSize];
        mRecoder=new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,
            audioFormat,bufferSize);

        //准备Video
//        mHeadInfo=null;
        this.width=width;
        this.height=height;
        MediaFormat videoFormat=MediaFormat.createVideoFormat(videoMime,width,height);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE,videoRate);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,frameRate);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,frameInterval);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities
            .COLOR_FormatYUV420Planar);
        mVideoEnc=MediaCodec.createEncoderByType(videoMime);
        mVideoEnc.configure(videoFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);

        try {
            mMuxer=new MediaMuxer(path+"."+postfix, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            throw new EncoderException(e);
        }
        return 0;
    }

    public int start() throws EncoderException, InterruptedException {
        //记录起始时间
        nanoTime=System.nanoTime();

        //Audio Start
        mAudioEnc.start();
        mRecoder.startRecording();
        if(mAudioThread!=null&&mAudioThread.isAlive()){
            isRecording=false;
            mAudioThread.join();
        }
        isRecording=true;
        mAudioThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
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

        //Video Start
        mVideoEnc.start();
        if(mVideoThread!=null&&mVideoThread.isAlive()){
            mStartFlag=false;
            mVideoThread.join();
        }
        mStartFlag=true;
        mVideoThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
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
        return 0;
    }

    public void stop(){
        try {
            //Audio Stop
            isRecording=false;
            mAudioThread.join();
            mRecoder.stop();
            mAudioEnc.stop();
            mAudioEnc.release();

            //Video Stop
            mStartFlag=false;
            mVideoThread.join();
            mVideoEnc.stop();
            mVideoEnc.release();

            //Muxer Stop
            mMuxer.stop();
            mMuxer.release();
            mVideoTrack=-1;
            mAudioTrack=-1;
            mTrackCount=0;
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
            int length=mRecoder.read(buffer,bufferSize);
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
                Log.e("wuwang","iiiiiiiiiiiiiiiiiiiiii-->"+mInfo.size+"/"+mInfo.offset+"/"+mInfo
                    .presentationTimeUs);
                if(mTrackCount==3&&mInfo.size>0){
                    mMuxer.writeSampleData(mAudioTrack,buffer,mInfo);
                }
                mAudioEnc.releaseOutputBuffer(outIndex,false);
                if((mInfo.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0){
                    return true;
                }
            }else if(outIndex ==MediaCodec.INFO_TRY_AGAIN_LATER){

            }else if(outIndex==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                mAudioTrack=mMuxer.addTrack(mAudioEnc.getOutputFormat());
                Log.e("wuwang","audio track-->"+mAudioTrack);
                mTrackCount++;
                if(mTrackCount==2){
                    mMuxer.start();
                    mTrackCount=3;
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
                rgbaToYuv(data,width,height,yuv);
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
                if(mTrackCount==3&&mInfo.size>0){
                    mMuxer.writeSampleData(mVideoTrack,outBuf,mInfo);
                }
                mVideoEnc.releaseOutputBuffer(outIndex,false);
                outIndex=mVideoEnc.dequeueOutputBuffer(mInfo,0);
                Log.e("wuwang","outIndex-->"+outIndex);
                if((mInfo.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0){
                    return true;
                }
            }else if(outIndex==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                mVideoTrack=mMuxer.addTrack(mVideoEnc.getOutputFormat());
                Log.e("wuwang","video track-->"+mVideoTrack);
                mTrackCount++;
                if(mTrackCount==2){
                    mMuxer.start();
                    mTrackCount=3;
                }
            }
        }while (outIndex>=0);
        return false;
    }

    byte[] yuv;
    private void rgbaToYuv(byte[] rgba,int width,int height,byte[] yuv){
        final int frameSize = width * height;

        int yIndex = 0;
        int uIndex = frameSize;
        int vIndex = frameSize + frameSize/4;

        int R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                index = j * width + i;
                if(rgba[index*4]>127||rgba[index*4]<-128){
                    Log.e("color","-->"+rgba[index*4]);
                }
                R = rgba[index*4]&0xFF;
                G = rgba[index*4+1]&0xFF;
                B = rgba[index*4+2]&0xFF;

                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                yuv[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv[uIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                    yuv[vIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                }
            }
        }
    }

}
