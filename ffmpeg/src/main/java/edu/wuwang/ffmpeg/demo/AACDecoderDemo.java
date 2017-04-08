package edu.wuwang.ffmpeg.demo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import edu.wuwang.ffmpeg.FFMpeg;
import edu.wuwang.ffmpeg.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by aiya on 2017/4/6.
 */

public class AACDecoderDemo extends AppCompatActivity implements View.OnClickListener {

    private AudioTrack mAudioTrack;
    private int audioBufSize;
    private boolean isDestoryed;
    private FFMpeg mpeg;

    private int sampleRate;
    private int bitRate;
    private int channelCount;
    private int audioFormat;
    private int frameSize;

    private byte[] tempData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_aacdecoder);
        mpeg=new FFMpeg();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mBtnInfo:
                audioBufSize = AudioTrack.getMinBufferSize(44100,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT);
                mAudioTrack=new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    audioBufSize,
                    AudioTrack.MODE_STREAM);
                mAudioTrack.play();
                testPlayPcm();
                break;
            case R.id.mBtnStart:
                mpeg.start();
                sampleRate=mpeg.get(FFMpeg.KEY_SAMPLE_RATE);
                channelCount=mpeg.get(FFMpeg.KEY_CHANNEL_COUNT);
                audioFormat=mpeg.get(FFMpeg.KEY_AUDIO_FORMAT);
                frameSize=mpeg.get(FFMpeg.KEY_FRAME_SIZE)*2;

                audioBufSize = AudioTrack.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_FLOAT);
                tempData=new byte[audioBufSize];
                mAudioTrack=new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_FLOAT,
                    audioBufSize,
                    AudioTrack.MODE_STREAM);
                mAudioTrack.play();
                Log.d("FFMPEG_LOG_","->"+sampleRate+"/"+channelCount+"/"+audioFormat+"/"+frameSize+"/"+audioBufSize);
                break;
            case R.id.mBtnDecode:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!isDestoryed){
                            if(mpeg.output(tempData)!=FFMpeg.EOF){
                                mAudioTrack.write(tempData, 0, tempData.length);
                            }else {
                                break;
                            }

                        }
                    }
                }).start();
                break;
        }
    }

    private void testPlayPcm(){
        new Thread(new Runnable() {
            byte[] data1=new byte[audioBufSize*2];
            File file=new File("/mnt/sdcard/save.pcm");
            int off1=0;
            FileInputStream fileInputStream;

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    fileInputStream=new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                while(!isDestoryed){
                    try {
                        int len=fileInputStream.read(data1,0,audioBufSize*2);
                        if(len>0){
                            off1+=len;
                            mAudioTrack.write(data1, 0, audioBufSize*2);
                            if(file.length()<=off1){
                                break;
                            }
                        }else{
                            break;
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }

                mAudioTrack.stop();
            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestoryed=true;
    }
}
