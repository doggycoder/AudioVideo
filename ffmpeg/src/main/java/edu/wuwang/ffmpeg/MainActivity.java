package edu.wuwang.ffmpeg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import edu.wuwang.ffmpeg.demo.AACDecoderDemo;
import edu.wuwang.ffmpeg.demo.DemoActivity;
import edu.wuwang.ffmpeg.demo.H264Decoder;
import edu.wuwang.ffmpeg.utils.FileCopyCat;
import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File cacheFile=getExternalFilesDir(null);
        if(cacheFile==null){
            cacheFile=getFilesDir();
        }
        FileCopyCat.getInstance().copyFolder(getAssets(),"sd",cacheFile.getAbsolutePath());
        FFMpeg.init();
        FFMpeg.set(FFMpeg.KEY_STR_CACHE_PATH,cacheFile.getAbsolutePath());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mBtnDecodeH264:
                //openActivity(H264DecoderDemo.class);
                openDemo(H264Decoder.class);
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

    public void openDemo(Class clazz){
        Intent intent=new Intent(this, DemoActivity.class);
        intent.putExtra("demo",clazz.getName());
        startActivity(intent);
    }

}
