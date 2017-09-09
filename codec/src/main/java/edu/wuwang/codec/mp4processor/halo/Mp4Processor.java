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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;


//todo 4.4的手机不支持video/mp4v-es格式的视频流，MediaMuxer混合无法stop，5.0以上可以
public class Mp4Processor {

    private final int TIME_OUT=1000;

    private String mInputPath;
    private String mOutputPath;

    private MediaCodec mVideoDecoder;
    private MediaCodec mVideoEncoder;
    private MediaCodec mAudioDecoder;
    private MediaCodec mAudioEncoder;
    private MediaExtractor mExtractor;
    private MediaMuxer mMuxer;
    private EGLHelper mEGLHelper;
    private MediaCodec.BufferInfo mVideoDecoderBufferInfo;
    private MediaCodec.BufferInfo mAudioDecoderBufferInfo;
    private MediaCodec.BufferInfo mVideoEncoderBufferInfo;
    private MediaCodec.BufferInfo mAudioEncoderBufferInfo;

    private int mAudioEncoderTrack;
    private int mVideoEncoderTrack;
    private int mAudioDecoderTrack;
    private int mVideoDecoderTrack;

    private String mAudioMime;
    private String mVideoMime;

    private int mInputVideoWidth=0;
    private int mInputVideoHeight=0;

    private int mOutputVideoWidth=0;
    private int mOutputVideoHeight=0;
    private int mVideoTextureId;
    private SurfaceTexture mVideoSurfaceTexture;

    private boolean isRenderToWindowSurface;
    private Surface mOutputSurface;

    private Thread mVideoThread;
    private Thread mAudioThread;
    private Thread mGLThread;
    private boolean mCodecFlag=false;
    private boolean isVideoExtractorEnd=false;
    private boolean isAudioExtractorEnd=false;
    private Renderer mRenderer=DEFAULT_RENDERER;
    private long glTime=0;
    private boolean mGLThreadFlag=false;
    private Semaphore mSem;
    private int mMuxerOutputType=MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;

    private final Object Extractor_LOCK=new Object();
    private final Object MUX_LOCK=new Object();

    public Mp4Processor(){
        mEGLHelper=new EGLHelper();
        mVideoDecoderBufferInfo=new MediaCodec.BufferInfo();
        mAudioDecoderBufferInfo=new MediaCodec.BufferInfo();
        mVideoEncoderBufferInfo=new MediaCodec.BufferInfo();
        mAudioEncoderBufferInfo=new MediaCodec.BufferInfo();
    }


    public void setInputPath(String path){
        this.mInputPath=path;

    }

    public void setOutputPath(String path){
        this.mOutputPath=path;
    }

    public void setOutputSurface(Surface surface){
        this.mOutputSurface=surface;
        this.isRenderToWindowSurface=surface!=null;
    }

    public void setRenderer(Renderer renderer){
        mRenderer=renderer==null?DEFAULT_RENDERER:renderer;
    }

    public int getVideoSurfaceTextureId(){
        return mVideoTextureId;
    }

    public SurfaceTexture getVideoSurfaceTexture(){
        return mVideoSurfaceTexture;
    }

    public void setOutputSize(int width,int height){
        this.mOutputVideoWidth=width;
        this.mOutputVideoHeight=height;
    }

    public boolean prepare() throws IOException {
        MediaMetadataRetriever mMetRet=new MediaMetadataRetriever();
        mMetRet.setDataSource(mInputPath);
        mExtractor=new MediaExtractor();
        mExtractor.setDataSource(mInputPath);
        int count=mExtractor.getTrackCount();
        for (int i=0;i<count;i++){
            MediaFormat format=mExtractor.getTrackFormat(i);
            String mime=format.getString(MediaFormat.KEY_MIME);
            if(mime.startsWith("audio")){
                mAudioDecoderTrack=i;
                mAudioDecoder=MediaCodec.createDecoderByType(mime);
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
                }
            }else if(mime.startsWith("video")){
                Log.e("wuwang", format.toString());
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
            mMuxer=new MediaMuxer(mOutputPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            MediaFormat format=mExtractor.getTrackFormat(mAudioDecoderTrack);
            mAudioEncoderTrack=mMuxer.addTrack(format);
        }
        return false;
    }

    public boolean start(){
        isVideoExtractorEnd=false;
        isVideoExtractorEnd=false;
        mCodecFlag=true;
        mGLThreadFlag=true;
        mVideoDecoder.start();
        mAudioDecoder.start();
        if(!isRenderToWindowSurface){
            mAudioEncoder.start();
            mVideoEncoder.start();
        }

        mGLThread=new Thread(new Runnable() {
            @Override
            public void run() {
                glRunnable();
            }
        });
        mGLThread.start();

//        mAudioThread=new Thread(new Runnable() {
//            @Override
//            public void run() {
//                synchronized (MUX_LOCK){
//                    try {
//                        MUX_LOCK.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                while (mCodecFlag){
//                    if(audioDecodeStep()){
//                        break;
//                    }
//                }
//            }
//        });
        mVideoThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (mCodecFlag){
                    if(videoDecodeStep()){
                        break;
                    }
                }
                mGLThreadFlag=false;
                try {
                    mGLThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (!audioDecodeStep());
                Log.e("wuwang","audio finish ------------------------ ");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mMuxer.stop();
                Log.e("wuwang","-----------------video thread end-----------------");
            }
        });
//        mAudioThread.start();
        mVideoThread.start();
        return false;
    }

    ByteBuffer buffer=ByteBuffer.allocate(1024*32);

    private boolean audioDecodeStep(){
        buffer.clear();
        synchronized (Extractor_LOCK){
            Log.e("wuwang","audio-----------");
            mExtractor.selectTrack(mAudioDecoderTrack);
            int length=mExtractor.readSampleData(buffer,0);
            if(length!=-1){
                int flags=mExtractor.getSampleFlags();
                mAudioEncoderBufferInfo.size=length;
                mAudioEncoderBufferInfo.flags=flags;
                mAudioEncoderBufferInfo.presentationTimeUs=mExtractor.getSampleTime();
                mAudioEncoderBufferInfo.offset=0;
                mMuxer.writeSampleData(mAudioEncoderTrack,buffer,mAudioEncoderBufferInfo);
//                    mAudioEncoder.queueInputBuffer(mInputIndex,0,length,mExtractor.getSampleTime(),mExtractor.getSampleFlags());
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
        //todo 这里只是临时策略，后面还需要修改，判断渲染的的确就是最后一帧了
        if(isEnd){
            mVideoEncoder.signalEndOfInputStream();
        }
        while (true){
            int mOutputIndex=mVideoEncoder.dequeueOutputBuffer(mVideoEncoderBufferInfo,TIME_OUT);
            Log.i("wuwang","encoder :" + mOutputIndex);
            if(mOutputIndex>=0){
                ByteBuffer buffer=getOutputBuffer(mVideoEncoder,mOutputIndex);

                Log.d("wuwang","write sample "+mVideoEncoderBufferInfo.size+"/"+mVideoEncoderBufferInfo.presentationTimeUs+"/"
                +mVideoEncoderBufferInfo.flags);
                if(mVideoEncoderBufferInfo.size>0){
                    mMuxer.writeSampleData(mVideoEncoderTrack,buffer,mVideoEncoderBufferInfo);
                }
                mVideoEncoder.releaseOutputBuffer(mOutputIndex,false);
            }else if(mOutputIndex==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                Log.e("wuwang","output ---  >"+mVideoEncoder.getOutputFormat());
                MediaFormat format=mVideoEncoder.getOutputFormat();
                mVideoEncoderTrack=mMuxer.addTrack(format);
                mMuxer.start();
                synchronized (MUX_LOCK){
                    MUX_LOCK.notifyAll();
                }
                Log.e("wuwang","start muxer ************************");
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
            mVideoSurfaceTexture.updateTexImage();
            mRenderer.onDraw();
            mEGLHelper.setPresentationTime(mVideoDecoderBufferInfo.presentationTimeUs*1000);
            if(!isRenderToWindowSurface){
                videoEncodeStep(false);
            }
            mEGLHelper.swapBuffers();
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
            Log.d("wuwang","onFrameAvailable - >"+mSem.availablePermits());
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

    public boolean stop(){
        return false;
    }

    public boolean release(){
        return false;
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

}
