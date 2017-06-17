package edu.wuwang.ffmpeg.demo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.view.View;

import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.wuwang.ffmpeg.FFMpeg;
import edu.wuwang.ffmpeg.filter.YuvFilter;

public class Mp4Decoder extends DemoBase {

    private boolean isCodecStarted=false;
    private YuvFilter mFilter;
    private byte[] data;

    private AudioTrack mAudioTrack;
    private byte[] tempData;
    private float[] tempFloatData;
    private int sampleRate;
    private int bitRate;
    private int channelCount;
    private int audioFormat;
    private int frameSize;
    private int audioBufSize;

    public Mp4Decoder(DemoActivity activity) {
        super(activity);
        mFilter=new YuvFilter(activity.getResources());
    }

    @Override
    protected void onViewCreated() {
        activity.setBtnText("图像解码","声音解码");
    }

    @Override
    protected void onBtn1Click(View view) {
        super.onBtn1Click(view);
        if(!isCodecStarted){
            mpeg.start(FFMpeg.DECODER_MP4_H264);
            activity.outInfo(String.format(Locale.CHINA,"视频宽高：%d,%d",
                    mpeg.get(FFMpeg.KEY_WIDTH),
                    mpeg.get(FFMpeg.KEY_HEIGHT)));
            isCodecStarted=true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isCodecStarted&&activity!=null&&!activity.isDestroyed()){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mGLView.requestRender();
                            }
                        });
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    @Override
    protected void onBtn2Click(View view) {
        super.onBtn2Click(view);
        mpeg.start(FFMpeg.DECODER_MP4_AAC);
        activity.outInfo("解码开始");
        sampleRate=mpeg.get(FFMpeg.KEY_SAMPLE_RATE);
        channelCount=mpeg.get(FFMpeg.KEY_CHANNEL_COUNT);
        audioFormat=mpeg.get(FFMpeg.KEY_AUDIO_FORMAT);
        frameSize=mpeg.get(FFMpeg.KEY_FRAME_SIZE);
        audioBufSize = AudioTrack.getMinBufferSize(sampleRate,
            channelCount==1? AudioFormat.CHANNEL_OUT_MONO:AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_FLOAT);
        tempData=new byte[frameSize*channelCount*4];
        activity.outInfo(String.format(Locale.CHINA,
            "音频信息：sampleRate-%d,channelCount-%d,audioFormat-%d,frameSize-%d",
            sampleRate,channelCount,audioFormat,frameSize));

        mAudioTrack=new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
            channelCount==1?AudioFormat.CHANNEL_OUT_MONO:AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_FLOAT,
            audioBufSize,
            AudioTrack.MODE_STREAM);
        mAudioTrack.play();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (activity!=null&&!activity.isDestroyed()){
                    if(mpeg.output(tempData)==FFMpeg.EOF){
                        break;
                    }else{
                        mAudioTrack.write(byte2float(tempData), 0, tempData.length/4,AudioTrack.WRITE_BLOCKING);
                    }
                }
                mAudioTrack.stop();
                mAudioTrack.release();
                mpeg.stop();
                activity.outInfo("解码完成");
            }
        }).start();
    }

    public float[] byte2float(byte[] b) {
        if(tempFloatData==null||tempFloatData.length!=b.length/4){
            tempFloatData=new float[b.length/4];
        }
        for (int i=0;i<b.length/4;i++){
            int l;
            l = b[i*4];
            l &= 0xff;
            l |= ((long) b[i*4+1] << 8);
            l &= 0xffff;
            l |= ((long) b[i*4 + 2] << 16);
            l &= 0xffffff;
            l |= ((long) b[i*4 + 3] << 24);
            tempFloatData[i]=Float.intBitsToFloat(l);
        }
        return tempFloatData;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFilter.create();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mFilter.setSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if(isCodecStarted){
            if(data==null||data.length!=mpeg.get(FFMpeg.KEY_WIDTH)*mpeg.get(FFMpeg.KEY_HEIGHT)*3/2){
                data=new byte[mpeg.get(FFMpeg.KEY_WIDTH)*mpeg.get(FFMpeg.KEY_HEIGHT)*3/2];
            }
            int ret=mpeg.output(data);
            if(ret==0){
                mFilter.updateFrame(mpeg.get(FFMpeg.KEY_WIDTH),mpeg.get(FFMpeg.KEY_HEIGHT),data);
                mFilter.draw();
            }else if(ret==FFMpeg.EOF){
                isCodecStarted=false;
            }
        }
    }

}
