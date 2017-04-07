package edu.wuwang.ffmpeg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import edu.wuwang.ffmpeg.demo.AACDecoderDemo;
import edu.wuwang.ffmpeg.demo.H264DecoderDemo;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FFMpeg.init();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mBtnDecodeH264:
                openActivity(H264DecoderDemo.class);
                break;
            case R.id.mBtnDecodeAAC:
                openActivity(AACDecoderDemo.class);
                break;
            case R.id.mBtnDecodeMP4:
                break;
        }
    }

    public void openActivity(Class clazz){
        startActivity(new Intent(this,clazz));
    }
}
