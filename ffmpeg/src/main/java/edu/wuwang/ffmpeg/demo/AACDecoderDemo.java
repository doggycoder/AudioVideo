package edu.wuwang.ffmpeg.demo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_aacdecoder);
        init();
    }

    private void init(){
        audioBufSize = AudioTrack.getMinBufferSize(8000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack=new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            audioBufSize,
            AudioTrack.MODE_STREAM);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mBtnInfo:

                break;
            case R.id.mBtnStart:
                mAudioTrack.play();
                testPlayPcm();
                break;
            case R.id.mBtnDecode:
                break;
        }
    }

    private void testPlayPcm(){
        new Thread(new Runnable() {
            byte[] data1=new byte[audioBufSize*2];
            File file=new File("/mnt/sdcard/test.pcm");
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
