package edu.wuwang.ffmpeg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInfo= (TextView) findViewById(R.id.mInfo);
        FFMpeg ffMpeg=new FFMpeg();
        mInfo.setText(ffMpeg.getConfiguration());
        ffMpeg.init();
    }
}
