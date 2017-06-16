package edu.wuwang.ffmpeg.demo;

import android.view.View;

import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.wuwang.ffmpeg.FFMpeg;
import edu.wuwang.ffmpeg.filter.YuvFilter;

public class Mp4Decoder extends DemoBase {

    private boolean isCodecStarted=false;
    private YuvFilter mFilter;
    private byte[] data;

    public Mp4Decoder(DemoActivity activity) {
        super(activity);
        mFilter=new YuvFilter(activity.getResources());
    }

    @Override
    protected void onViewCreated() {
        activity.setBtnText("图像解码","声音解码");
    }

    @Override
    protected void onBtn1Click(View view) {
        super.onBtn1Click(view);
        if(!isCodecStarted){
            mpeg.start(FFMpeg.DECODER_MP4);
            activity.outInfo(String.format(Locale.CHINA,"视频宽高：%d,%d",
                    mpeg.get(FFMpeg.KEY_WIDTH),
                    mpeg.get(FFMpeg.KEY_HEIGHT)));
            isCodecStarted=true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isCodecStarted&&activity!=null&&!activity.isDestroyed()){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mGLView.requestRender();
                            }
                        });
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }



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
            int ret=mpeg.output(data);
            if(ret==0){
                mFilter.updateFrame(mpeg.get(FFMpeg.KEY_WIDTH),mpeg.get(FFMpeg.KEY_HEIGHT),data);
                mFilter.draw();
            }else if(ret==FFMpeg.EOF){
                isCodecStarted=false;
            }
        }
    }

}
