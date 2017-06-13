package edu.wuwang.ffmpeg.demo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.view.View;
import edu.wuwang.ffmpeg.FFMpeg;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Locale;

/**
 * Created by aiya on 2017/6/13.
 */

public class AACDecoder extends DemoBase {

    private AudioTrack mAudioTrack;

    private byte[] tempData;
    private float[] tempFloatData;
    private int sampleRate;
    private int bitRate;
    private int channelCount;
    private int audioFormat;
    private int frameSize;
    private int audioBufSize;

    public AACDecoder(DemoActivity activity) {
        super(activity);
    }

    @Override
    protected void onViewCreated() {
        activity.setBtnText("解码播放");
        activity.outInfo("解码应用目录下test.aac文件");
        activity.outInfo("解码文件保存到应用目录下save.pcm");
    }

    @Override
    protected void onBtn1Click(View view) {
        super.onBtn1Click(view);
        mpeg.start(FFMpeg.DECODER_AAC);
        activity.outInfo("解码开始");
        sampleRate=mpeg.get(FFMpeg.KEY_SAMPLE_RATE);
        channelCount=mpeg.get(FFMpeg.KEY_CHANNEL_COUNT);
        audioFormat=mpeg.get(FFMpeg.KEY_AUDIO_FORMAT);
        frameSize=mpeg.get(FFMpeg.KEY_FRAME_SIZE);
        audioBufSize = AudioTrack.getMinBufferSize(sampleRate,
            channelCount==1?AudioFormat.CHANNEL_OUT_MONO:AudioFormat.CHANNEL_OUT_STEREO,
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

}
