package edu.wuwang.codec.camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import edu.wuwang.codec.R;
import edu.wuwang.codec.coder.CameraRecorder;
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
    private int type;       //1为拍照，0为录像

    private CameraRecorder mp4Recorder;

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
                            recordFlag=false;
                            time=System.currentTimeMillis();
                            mCapture.postDelayed(captureTouchRunnable,500);
                            break;
                        case MotionEvent.ACTION_UP:
                            recordFlag=false;
                            if(System.currentTimeMillis()-time<500){
                                mCapture.removeCallbacks(captureTouchRunnable);
                                mCameraView.setFrameCallback(720,1280,CameraActivity.this);
                                mCameraView.takePhoto();
                            }
                            break;
                    }
                    return false;
                }
            });
        }
    };

    //录像的Runnable
    private Runnable captureTouchRunnable=new Runnable() {
        @Override
        public void run() {
            recordFlag=true;
            mExecutor.execute(recordRunnable);
        }
    };

    private Runnable recordRunnable=new Runnable() {

        @Override
        public void run() {
            type=0;
            long timeCount=0;
//                    if(mRecorder==null){
//                        mRecorder=new CameraRecorder();
//                        mAudioEncoder=new AudioEncoder();
//                    }
            if(mp4Recorder==null){
                mp4Recorder=new CameraRecorder();
            }
            long time=System.currentTimeMillis();
            String savePath=getPath("video/",time+".mp4");
            mp4Recorder.setSavePath(getPath("video/",time+""),"mp4");
//                    mAudioEncoder.setSavePath(getVideoPath(time+".aac"));
            try {
//                        mRecorder.prepare(360,640);
//                        mRecorder.start();
                mp4Recorder.prepare(384,640);
                mp4Recorder.start();
                mCameraView.setFrameCallback(384,640,CameraActivity.this);
                mCameraView.startRecord();
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

                if(timeCount<2000){
                    mp4Recorder.cancel();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCapture.setProcess(0);
                            Toast.makeText(CameraActivity.this,"录像时间太短了",Toast.LENGTH_SHORT).show();

                        }
                    });
                }else{
                    mp4Recorder.stop();
                    recordComplete(type,savePath);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private String getBaseFolder(){
        String baseFolder=Environment.getExternalStorageDirectory()+"/Codec/";
        File f=new File(baseFolder);
        if(!f.exists()){
            boolean b=f.mkdirs();
            if(!b){
                baseFolder=getExternalFilesDir(null).getAbsolutePath()+"/";
            }
        }
        return baseFolder;
    }

    //获取VideoPath
    private String getPath(String path,String fileName){
        String p= getBaseFolder()+path;
        File f=new File(p);
        if(!f.exists()&&!f.mkdirs()){
            return getBaseFolder()+fileName;
        }
        return p+fileName;
    }

    //图片保存
    public void saveBitmap(Bitmap b){
        long dataTake = System.currentTimeMillis();
        final String jpegName=getPath("photo/", dataTake +".jpg");
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        recordComplete(type,jpegName);
    }

    private void recordComplete(int type,final String path){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCapture.setProcess(0);
                Toast.makeText(CameraActivity.this,"文件保存路径："+path,Toast.LENGTH_SHORT).show();
            }
        });
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
