/*
 *
 * CameraActivity.java
 * 
 * Created by Wuwang on 2016/11/14
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package edu.wuwang.codec.camera;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import edu.wuwang.codec.R;
import edu.wuwang.codec.coder.AudioEncoder;
import edu.wuwang.codec.coder.CameraRecorder;
import edu.wuwang.codec.coder.EncoderException;
import edu.wuwang.codec.coder.YooRecorder;
import edu.wuwang.codec.utils.PermissionUtils;

/**
 * Description:
 */
public class CameraActivity extends AppCompatActivity implements FrameCallback {

    private CameraView mCameraView;
    private CircularProgressView mCapture;
    private long time;
    private ExecutorService mExecutor;
    private long maxTime=20000;
    private long timeStep=50;
    private boolean recordFlag=false;

//    private CameraRecorder mRecorder;
//    private AudioEncoder mAudioEncoder;
    private YooRecorder mp4Recorder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtils.askPermission(this,new String[]{Manifest.permission.CAMERA,Manifest
            .permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},10,initViewRunnable);
    }

    private Runnable initViewRunnable=new Runnable() {
        @Override
        public void run() {
            mExecutor= Executors.newSingleThreadExecutor();
            setContentView(R.layout.activity_camera);
            mCameraView= (CameraView)findViewById(R.id.mCameraView);
            mCameraView.setFrameCallback(384,640,CameraActivity.this);
            mCapture= (CircularProgressView)findViewById(R.id.mCapture);
            mCapture.setTotal((int)maxTime);
            mCapture.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            recordFlag=true;
                            time=System.currentTimeMillis();
                            mCapture.postDelayed(captureTouchRunnable,1000);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_HOVER_EXIT:
                            if(System.currentTimeMillis()-time<1000){
                                mCapture.removeCallbacks(captureTouchRunnable);
                            }else {

                            }
                            recordFlag=false;
                            break;
                    }
                    return false;
                }
            });
        }
    };

    private Runnable captureTouchRunnable=new Runnable() {
        @Override
        public void run() {
            mExecutor.execute(new Runnable() {

                @Override
                public void run() {
                    long timeCount=0;
//                    if(mRecorder==null){
//                        mRecorder=new CameraRecorder();
//                        mAudioEncoder=new AudioEncoder();
//                    }
                    if(mp4Recorder==null){
                        mp4Recorder=new YooRecorder();
                    }
                    long time=System.currentTimeMillis();
                    String temp=getVideoPath(time+".mp4");
//                    mRecorder.setSavePath(temp);
                    mp4Recorder.setSavePath(getVideoPath(time+""),"mp4");
//                    mAudioEncoder.setSavePath(getVideoPath(time+".aac"));
                    try {
//                        mRecorder.prepare(360,640);
//                        mRecorder.start();
                        mp4Recorder.prepare(384,640);
                        mp4Recorder.start();
//                        mAudioEncoder.prepare();
//                        mAudioEncoder.start();
                        mCameraView.startRecord();
                    } catch (IOException | EncoderException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    while (timeCount<=maxTime&&recordFlag){
                        long start=System.currentTimeMillis();
                        mCapture.setProcess((int)timeCount);
                        long end=System.currentTimeMillis();
                        try {
                            Thread.sleep(timeStep-(end-start));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        timeCount+=timeStep;
                    }
                    mCameraView.stopRecord();
//                    mRecorder.stop();
                    mp4Recorder.stop();
//                    mAudioEncoder.stop();
                    Log.e("wuwang","录制->"+temp);
                }
            });
        }
    };

    private String getVideoPath(String path){
        String p= Environment.getExternalStorageDirectory()+"/AiyaCamera/video/";
        File f=new File(p);
        if((!f.exists()||!f.isDirectory())&&f.mkdirs()){
            android.util.Log.e("wuwang","mkdirs->"+p);
        }
        return p+path;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode == 10, grantResults, initViewRunnable,
            new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CameraActivity.this, "没有获得必要的权限", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("切换摄像头").setTitle("切换摄像头").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String name=item.getTitle().toString();
        if(name.equals("切换摄像头")){
            mCameraView.switchCamera();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFrame(byte[] bytes, long time) {
        mp4Recorder.feedData(bytes,time);
    }
}
