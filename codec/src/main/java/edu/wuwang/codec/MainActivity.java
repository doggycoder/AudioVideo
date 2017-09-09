package edu.wuwang.codec;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.wuwang.codec.camera.CameraActivity;
import edu.wuwang.codec.mp4processor.Mp4ModifyTestActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.mBtnCameraRecorder:
                startActivity(new Intent(this, CameraActivity.class));
                break;
            case R.id.mMp4SurfaceTest:
                startActivity(new Intent(this, Mp4ModifyTestActivity.class));
                break;
        }
    }

}
