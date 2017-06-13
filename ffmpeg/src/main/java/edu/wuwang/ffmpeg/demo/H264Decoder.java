package edu.wuwang.ffmpeg.demo;

import android.view.View;
import android.widget.Toast;
import edu.wuwang.ffmpeg.FFMpeg;
import edu.wuwang.ffmpeg.filter.YuvFilter;
import java.util.Locale;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by aiya on 2017/6/13.
 */

public class H264Decoder extends DemoBase {

    private boolean isCodecStarted=false;
    private YuvFilter mFilter;
    private byte[] data;

    public H264Decoder(DemoActivity activity) {
        super(activity);
        mFilter=new YuvFilter(activity.getResources());
    }

    @Override
    protected void onViewCreated() {
        activity.setBtnText("Info","初始化","解码一帧");
        activity.outInfo("点击Info可获取FFMpeg信息");
        activity.outInfo("点击初始化获取test.h264信息，并准备解码");
        activity.outInfo("不断点击解码一帧以进行解码");
    }

    @Override
    protected void onBtn1Click(View view) {
        super.onBtn1Click(view);
        activity.clearInfo();
        activity.outInfo(FFMpeg.getInfo());
    }

    @Override
    protected void onBtn2Click(View view) {
        super.onBtn2Click(view);
        mpeg.start(FFMpeg.DECODER_H264);
        activity.outInfo(String.format(Locale.CHINA,"视频宽高：%d,%d",
            mpeg.get(FFMpeg.KEY_WIDTH),
            mpeg.get(FFMpeg.KEY_HEIGHT)));
        isCodecStarted=true;
    }

    @Override
    protected void onBtn3Click(View view) {
        super.onBtn3Click(view);
        if(!isCodecStarted){
            Toast.makeText(activity,"解码器未初始化",Toast.LENGTH_SHORT).show();
        }
        mGLView.requestRender();
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
            if(mpeg.output(data)==0){
                mFilter.updateFrame(mpeg.get(FFMpeg.KEY_WIDTH),mpeg.get(FFMpeg.KEY_HEIGHT),data);
                mFilter.draw();
            }
        }
    }
}
