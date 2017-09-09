package edu.wuwang.codec.mp4processor;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;

import edu.wuwang.codec.R;
import edu.wuwang.codec.filter.GrayFilter;
import edu.wuwang.codec.filter.GroupFilter;
import edu.wuwang.codec.filter.NoFilter;
import edu.wuwang.codec.filter.OesFilter;
import edu.wuwang.codec.mp4processor.halo.Mp4Processor;
import edu.wuwang.codec.mp4processor.halo.Renderer;

/**
 * Created by aiya on 2017/9/8.
 */

public class Mp4ModifyTestActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
//    private Mp4Providers mProviders;
//    private GpuVideoProcessor mProcessor;
//    private Mp4Generator mGenerator;

    private GroupFilter mGF;
    private NoFilter mNoFilter;

    private Mp4Processor mMp4Processor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp4_test);
        mGF=new GroupFilter(getResources());
        mGF.addFilter(new OesFilter(getResources()));
        mGF.addFilter(new GrayFilter(getResources()));
        mNoFilter=new NoFilter(getResources());
//        mProcessor = new GpuVideoProcessor();
//
//        mProcessor.setRenderer(new Renderer() {
//            @Override
//            public void onCreate(int width, int height) {
//                mFilter.create();
//                mFilter.setSize(width, height);
//                Log.e("wuwang", "size-->" + width + "/" + height);
//            }
//
//            @Override
//            public void onDraw() {
//                Log.e("wuwang", "textureId-->" + mProcessor.getInputTextureId());
//                mFilter.setTextureId(mProcessor.getInputTextureId());
//                mFilter.draw();
//            }
//
//            @Override
//            public void onDestroy() {
//
//            }
//        });
//
//        mProviders = new Mp4Providers();
//        mProviders.setGpuVideoProcessor(mProcessor);
        mMp4Processor=new Mp4Processor();
        mSurfaceView = (SurfaceView) findViewById(R.id.mSurface);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//                mProviders.setOutputSurface(holder.getSurface());
//                mProcessor.setOutputSurface(holder.getSurface());
//                mMp4Processor.setOutputSurface(holder.getSurface());
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        mMp4Processor.setRenderer(new Renderer() {
            @Override
            public void onCreate(int width, int height) {
                mGF.create();
                mGF.setSize(width, height);
                mNoFilter.create();
                mNoFilter.setSize(width, height);
                Log.e("wuwang", "size-->" + width + "/" + height);
            }

            @Override
            public void onDraw() {
                Log.e("wuwang", "textureId-->" + mMp4Processor.getVideoSurfaceTextureId());
                mGF.setTextureId(mMp4Processor.getVideoSurfaceTextureId());
                mGF.draw();
                mNoFilter.setTextureId(mGF.getOutputTexture());
                mNoFilter.draw();
            }

            @Override
            public void onDestroy() {

            }
        });

//        mGenerator = new Mp4Generator();
//        mGenerator.setProvider(IProvider.KEY_AUDIO, mProviders.AUDIO_PROVIDER);
//        mGenerator.setProvider(IProvider.KEY_VIDEO_SURFACE, mProviders.VIDEO_SURFACE_PROVIDER);
//        Configuration configuration = new Configuration();
//        configuration.sourcePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.mp4";
//        mGenerator.setConfiguration(configuration);
//        mGenerator.init();
//        mProcessor.setOutputSurface(mGenerator.getInputSurface());

        mMp4Processor.setOutputPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.mp4");
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mBtn0:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //intent.setType(“image/*”);//选择图片
                //intent.setType(“audio/*”); //选择音频
                intent.setType("video/mp4"); //选择视频 （mp4 3gp 是android支持的视频格式）
                //intent.setType(“video/*;image/*”);//同时选择视频和图片
                //intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                break;
            case R.id.mBtn1:
                try {
                    mMp4Processor.prepare();
                    mMp4Processor.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                mGenerator.start();
//                try {
//                    mProviders.start();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String path = getRealFilePath(data.getData());
            if (path != null) {
//                Configuration config = new Configuration();
//                config.sourcePath = path;
//                mProviders.setConfiguration(config);
                mMp4Processor.setInputPath(path);
            }
        }
    }


    public String getRealFilePath(final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            Log.e("wuwang", "scheme is null");
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
            Log.e("wuwang", "SCHEME_FILE");
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            data = GetPathFromUri4kitkat.getPath(getApplicationContext(), uri);
        }
        return data;
    }
}
