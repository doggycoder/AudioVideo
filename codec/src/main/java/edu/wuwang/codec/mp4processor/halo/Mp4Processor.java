package edu.wuwang.codec.mp4processor.halo;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;


/**
 * MP4处理工具，暂时只用于处理图像。
 * 4.4的手机不支持video/mp4v-es格式的视频流，MediaMuxer混合无法stop，5.0以上可以
 */
public class Mp4Processor {

    private final int TIME_OUT=1000;

    private String mInputPath;                  //输入路径
    private String mOutputPath;                 //输出路径

    private MediaCodec mVideoDecoder;           //视频解码器
    private MediaCodec mVideoEncoder;           //视频编码器
    //private MediaCodec mAudioDecoder;           //音频解码器
    //private MediaCodec mAudioEncoder;           //音频编码器
    private MediaExtractor mExtractor;          //音视频分离器
    private MediaMuxer mMuxer;                  //音视频混合器
    private EGLHelper mEGLHelper;               //GL环境创建的帮助类
    private MediaCodec.BufferInfo mVideoDecoderBufferInfo;  //用于存储当前帧的视频解码信息
    //private MediaCodec.BufferInfo mAudioDecoderBufferInfo;  //用于存储当前帧的音频解码信息
    private MediaCodec.BufferInfo mVideoEncoderBufferInfo;  //用于存储当前帧的视频编码信息
    private MediaCodec.BufferInfo mAudioEncoderBufferInfo;  //用于纯粹当前帧的音频编码信息

    private int mAudioEncoderTrack;     //解码音轨
    private int mVideoEncoderTrack;     //解码视轨
    private int mAudioDecoderTrack;     //编码音轨
    private int mVideoDecoderTrack;     //编码视轨

    //private String mAudioMime;
    //private String mVideoMime;

    private int mInputVideoWidth=0;     //输入视频的宽度
    private int mInputVideoHeight=0;    //输入视频的高度

    private int mOutputVideoWidth=0;    //输出视频的宽度
    private int mOutputVideoHeight=0;   //输出视频的高度
    private int mVideoTextureId;        //原始视频图像的纹理
    private SurfaceTexture mVideoSurfaceTexture;    //用于接收原始视频的解码的图像流

    private boolean isRenderToWindowSurface;        //是否渲染到用户设置的WindowBuffer上，用于测试
    private Surface mOutputSurface;                 //视频输出的Surface

    private Thread mDecodeThread;
    private Thread mGLThread;
    private boolean mCodecFlag=false;
    private boolean isVideoExtractorEnd=false;
    private boolean isAudioExtractorEnd=false;
    private Renderer mRenderer=DEFAULT_RENDERER;
    private boolean mGLThreadFlag=false;
    private Semaphore mSem;

    private final Object Extractor_LOCK=new Object();
    private final Object MUX_LOCK=new Object();

    private CompleteListener mCompleteListener;

    public Mp4Processor(){
        mEGLHelper=new EGLHelper();
        mVideoDecoderBufferInfo=new MediaCodec.BufferInfo();
        //mAudioDecoderBufferInfo=new MediaCodec.BufferInfo();
        mVideoEncoderBufferInfo=new MediaCodec.BufferInfo();
        mAudioEncoderBufferInfo=new MediaCodec.BufferInfo();
    }


    /**
     * 设置用于处理的MP4文件
     * @param path 文件路径
     */
    public void setInputPath(String path){
        this.mInputPath=path;
    }

    /**
     * 设置处理后的mp4存储的位置
     * @param path 文件路径
     */
    public void setOutputPath(String path){
        this.mOutputPath=path;
    }

    /**
     * 设置直接渲染到指定的Surface上
     * @param surface 渲染的位置
     * {@hide}
     */
    public void setOutputSurface(Surface surface){
        this.mOutputSurface=surface;
        this.isRenderToWindowSurface=surface!=null;
    }

    /**
     * 设置用户处理接口
     * @param renderer 处理接口
     */
    public void setRenderer(Renderer renderer){
        mRenderer=renderer==null?DEFAULT_RENDERER:renderer;
    }

    public int getVideoSurfaceTextureId(){
        return mVideoTextureId;
    }

    public SurfaceTexture getVideoSurfaceTexture(){
        return mVideoSurfaceTexture;
    }

    /**
     * 设置输出Mp4的图像大小，默认为输出大小
     * @param width 视频图像宽度
     * @param height 视频图像高度
     */
    public void setOutputSize(int width,int height){
        this.mOutputVideoWidth=width;
        this.mOutputVideoHeight=height;
    }

    public void setOnCompleteListener(CompleteListener listener){
        this.mCompleteListener=listener;
    }

    public boolean prepare() throws IOException {
        //todo 获取视频旋转信息，并做出相应处理
        MediaMetadataRetriever mMetRet=new MediaMetadataRetriever();
        mMetRet.setDataSource(mInputPath);
        mExtractor=new MediaExtractor();
        mExtractor.setDataSource(mInputPath);
        int count=mExtractor.getTrackCount();
        //解析Mp4
        for (int i=0;i<count;i++){
            MediaFormat format=mExtractor.getTrackFormat(i);
            String mime=format.getString(MediaFormat.KEY_MIME);
            if(mime.startsWith("audio")){
                mAudioDecoderTrack=i;
                //todo 暂时不对音频处理，后续需要对音频处理时再修改这个
                /*mAudioDecoder=MediaCodec.createDecoderByType(mime);
                mAudioDecoder.configure(format,null,null,0);
                if(!isRenderToWindowSurface){
                    Log.e("wuwang", format.toString());
                    MediaFormat audioFormat=MediaFormat.createAudioFormat(mime,
                            format.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                            format.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                    audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                            format.getInteger(MediaFormat.KEY_AAC_PROFILE));
                    audioFormat.setInteger(MediaFormat.KEY_BIT_RATE,
                            Integer.valueOf(mMetRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)));
                    mAudioEncoder=MediaCodec.createEncoderByType(mime);
                    mAudioEncoder.configure(audioFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
                }*/
            }else if(mime.startsWith("video")){

                mVideoDecoderTrack=i;
                mInputVideoWidth=format.getInteger(MediaFormat.KEY_WIDTH);
                mInputVideoHeight=format.getInteger(MediaFormat.KEY_HEIGHT);
                mVideoDecoder=MediaCodec.createDecoderByType(mime);
                mVideoTextureId=mEGLHelper.createTextureID();
                mVideoSurfaceTexture=new SurfaceTexture(mVideoTextureId);
                mVideoSurfaceTexture.setOnFrameAvailableListener(mFrameAvaListener);
                mVideoDecoder.configure(format,new Surface(mVideoSurfaceTexture),null,0);
                if(!isRenderToWindowSurface){
                    if(mOutputVideoWidth==0||mOutputVideoHeight==0){
                        mOutputVideoWidth=mInputVideoWidth;
                        mOutputVideoHeight=mInputVideoHeight;
                    }
                    MediaFormat videoFormat=MediaFormat.createVideoFormat(mime,mOutputVideoWidth,mOutputVideoHeight);
                    videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                    videoFormat.setInteger(MediaFormat.KEY_BIT_RATE,mOutputVideoHeight*mOutputVideoWidth*5);
                    videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 24);
                    videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
                    mVideoEncoder=MediaCodec.createEncoderByType(mime);
                    mVideoEncoder.configure(videoFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
                    mOutputSurface=mVideoEncoder.createInputSurface();
                    Bundle bundle=new Bundle();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        bundle.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE,mOutputVideoHeight*mOutputVideoWidth*5);
                        mVideoEncoder.setParameters(bundle);
                    }
                }
            }
        }
        if(!isRenderToWindowSurface){
            //如果用户没有设置渲染到指定Surface，就需要导出视频，暂时不对音频做处理
            mMuxer=new MediaMuxer(mOutputPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            MediaFormat format=mExtractor.getTrackFormat(mAudioDecoderTrack);
            mAudioEncoderTrack=mMuxer.addTrack(format);
        }
        return false;
    }

    public boolean start(){
        isVideoExtractorEnd=false;
        isVideoExtractorEnd=false;
        mGLThreadFlag=true;
        mVideoDecoder.start();
        //mAudioDecoder.start();
        if(!isRenderToWindowSurface){
            //mAudioEncoder.start();
            mVideoEncoder.start();
        }

        mGLThread=new Thread(new Runnable() {
            @Override
            public void run() {
                glRunnable();
            }
        });
        mGLThread.start();

        mCodecFlag=true;
        mDecodeThread=new Thread(new Runnable() {
            @Override
            public void run() {
                //视频处理
                while (mCodecFlag&&!videoDecodeStep());
                mGLThreadFlag=false;
                try {
                    mGLThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //将原视频中的音频复制到新视频中
                ByteBuffer buffer=ByteBuffer.allocate(1024*32);
                while (mCodecFlag&&!audioDecodeStep(buffer));
                buffer.clear();
                mMuxer.stop();
                if(mCompleteListener!=null&&mCodecFlag){
                    mCompleteListener.onComplete(mOutputPath);
                }
                mCodecFlag=false;
            }
        });
        mDecodeThread.start();
        return false;
    }

    public void waitProcessFinish() throws InterruptedException {
        if(mDecodeThread!=null&&mDecodeThread.isAlive()){
            mDecodeThread.join();
        }
    }

    private boolean audioDecodeStep(ByteBuffer buffer){
        buffer.clear();
        synchronized (Extractor_LOCK){
            mExtractor.selectTrack(mAudioDecoderTrack);
            int length=mExtractor.readSampleData(buffer,0);
            if(length!=-1){
                int flags=mExtractor.getSampleFlags();
                mAudioEncoderBufferInfo.size=length;
                mAudioEncoderBufferInfo.flags=flags;
                mAudioEncoderBufferInfo.presentationTimeUs=mExtractor.getSampleTime();
                mAudioEncoderBufferInfo.offset=0;
                mMuxer.writeSampleData(mAudioEncoderTrack,buffer,mAudioEncoderBufferInfo);
            }
            isAudioExtractorEnd=!mExtractor.advance();
        }
        return isAudioExtractorEnd;
    }

    //视频解码到SurfaceTexture上，以供后续处理。返回值为是否是最后一帧视频
    private boolean videoDecodeStep(){
        int mInputIndex=mVideoDecoder.dequeueInputBuffer(TIME_OUT);
        if(mInputIndex>=0){
            ByteBuffer buffer=getInputBuffer(mVideoDecoder,mInputIndex);
            buffer.clear();
            synchronized (Extractor_LOCK) {
                mExtractor.selectTrack(mVideoDecoderTrack);
                int ret = mExtractor.readSampleData(buffer, 0);
                if (ret != -1) {
                    mVideoDecoder.queueInputBuffer(mInputIndex, 0, ret, mExtractor.getSampleTime(), mExtractor.getSampleFlags());
                }
                isVideoExtractorEnd = !mExtractor.advance();
            }
        }
        while (true){
            int mOutputIndex=mVideoDecoder.dequeueOutputBuffer(mVideoDecoderBufferInfo,TIME_OUT);
            if(mOutputIndex>=0){
                mVideoDecoder.releaseOutputBuffer(mOutputIndex,true);
            }else if(mOutputIndex==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                MediaFormat format=mVideoDecoder.getOutputFormat();
            }else if(mOutputIndex==MediaCodec.INFO_TRY_AGAIN_LATER){
                break;
            }
        }
        return isVideoExtractorEnd;
    }

    private boolean videoEncodeStep(boolean isEnd){
        if(isEnd){
            mVideoEncoder.signalEndOfInputStream();
        }
        while (true){
            int mOutputIndex=mVideoEncoder.dequeueOutputBuffer(mVideoEncoderBufferInfo,TIME_OUT);
            if(mOutputIndex>=0){
                ByteBuffer buffer=getOutputBuffer(mVideoEncoder,mOutputIndex);
                if(mVideoEncoderBufferInfo.size>0){
                    mMuxer.writeSampleData(mVideoEncoderTrack,buffer,mVideoEncoderBufferInfo);
                }
                mVideoEncoder.releaseOutputBuffer(mOutputIndex,false);
            }else if(mOutputIndex==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                MediaFormat format=mVideoEncoder.getOutputFormat();
                mVideoEncoderTrack=mMuxer.addTrack(format);
                mMuxer.start();
                synchronized (MUX_LOCK){
                    MUX_LOCK.notifyAll();
                }
            }else if(mOutputIndex==MediaCodec.INFO_TRY_AGAIN_LATER){
                break;
            }
        }
        return false;
    }

    private void glRunnable(){
        mSem=new Semaphore(0);
        mEGLHelper.setSurface(mOutputSurface);
        boolean ret=mEGLHelper.createGLES(mOutputVideoWidth,mOutputVideoHeight);
        if(!ret)return;
        mRenderer.onCreate(mOutputVideoWidth,mOutputVideoHeight);
        while (mGLThreadFlag){
            try {
                mSem.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(mGLThreadFlag){
                mVideoSurfaceTexture.updateTexImage();
                mRenderer.onDraw();
                mEGLHelper.setPresentationTime(mVideoDecoderBufferInfo.presentationTimeUs*1000);
                if(!isRenderToWindowSurface){
                    videoEncodeStep(false);
                }
                mEGLHelper.swapBuffers();
            }
        }
        if(!isRenderToWindowSurface){
            videoEncodeStep(true);
        }
        mEGLHelper.destroyGLES();
        mRenderer.onDestroy();
    }

    private SurfaceTexture.OnFrameAvailableListener mFrameAvaListener=new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            mSem.release();
        }
    };

    private static final Renderer DEFAULT_RENDERER=new Renderer() {
        @Override
        public void onCreate(int width, int height) {
            Log.e("wuwang","DEFAULT_RENDERER onCreate(width,height):"+width+"/"+height);
        }

        @Override
        public void onDraw() {

        }

        @Override
        public void onDestroy() {
            Log.e("wuwang","DEFAULT_RENDERER onDestroy");
        }
    };

    public boolean stop() throws InterruptedException {
        boolean del=false;
        if(mCodecFlag){
            del=true;
        }
        mCodecFlag=false;
        if(mDecodeThread!=null&&mDecodeThread.isAlive()){
            mDecodeThread.join();
        }
        mGLThreadFlag=false;
        if(mGLThread!=null&&mGLThread.isAlive()){
            mSem.release();
            mGLThread.join();
        }
        if(mVideoDecoder!=null){
            mVideoDecoder.stop();
        }
        if(!isRenderToWindowSurface&&mVideoEncoder!=null){
            mVideoEncoder.stop();
        }
        if(mMuxer!=null){
            mMuxer.stop();
        }
        if(del){
            File file=new File(mOutputPath);
            if(file.exists()){
                file.delete();
            }
        }
        return true;
    }

    public boolean release() throws InterruptedException {
        if(mCodecFlag){
            stop();
        }
        if(mVideoDecoder!=null){
            mVideoDecoder.release();
            mVideoDecoder=null;
        }
        if(mVideoEncoder!=null){
            mVideoEncoder.release();
            mVideoEncoder=null;
        }
        if(mMuxer!=null){
            mMuxer.release();
            mMuxer=null;
        }
        if(mExtractor!=null){
            mExtractor.release();
        }
        return true;
    }

    private ByteBuffer getInputBuffer(MediaCodec codec, int index){
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

    public interface CompleteListener{
        void onComplete(String path);
    }

}
