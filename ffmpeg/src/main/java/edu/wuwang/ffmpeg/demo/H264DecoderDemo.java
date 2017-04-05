package edu.wuwang.ffmpeg.demo;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import edu.wuwang.ffmpeg.FFMpeg;
import edu.wuwang.ffmpeg.R;
import edu.wuwang.ffmpeg.filter.YuvFilter;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by aiya on 2017/4/5.
 */

public class H264DecoderDemo extends AppCompatActivity implements View.OnClickListener {

    private GLSurfaceView mGLView;
    private TextView mTvInfo;
    private FFMpeg mpeg;
    private YuvFilter mFilter;
    private byte[] data;
    private boolean isCodecStarted=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_h264decoder);

        mGLView=(GLSurfaceView) findViewById(R.id.mGLView);
        mTvInfo=(TextView) findViewById(R.id.mTvInfo);
        FFMpeg.init();
        mpeg=new FFMpeg();
        mFilter=new YuvFilter(getResources());
        mGLView.setEGLContextClientVersion(2);
        mGLView.setPreserveEGLContextOnPause(true);
        mGLView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                mFilter.create();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                mFilter.setSize(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                if(isCodecStarted){
                    if(data==null||data.length!=mpeg.get(FFMpeg.KEY_WIDTH)*mpeg.get(FFMpeg.KEY_HEIGHT)){
                        data=new byte[mpeg.get(FFMpeg.KEY_WIDTH)*mpeg.get(FFMpeg.KEY_HEIGHT)*3/2];
                    }
                    if(mpeg.output(data)==0){
                        mFilter.updateFrame(mpeg.get(FFMpeg.KEY_WIDTH),mpeg.get(FFMpeg.KEY_HEIGHT),data);
                        mFilter.draw();
                    }
                }
            }
        });
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mGLView!=null){
            mGLView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mGLView!=null){
            mGLView.onPause();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mBtnInfo:
                mTvInfo.setText(FFMpeg.getInfo());
                break;
            case R.id.mBtnStart:
                mpeg.start();
                isCodecStarted=true;
                break;
            case R.id.mBtnDecode:
                mGLView.requestRender();
                break;
        }
    }
}
