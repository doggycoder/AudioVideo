package edu.wuwang.ffmpeg.demo;

import android.opengl.GLSurfaceView;
import android.view.View;
import edu.wuwang.ffmpeg.FFMpeg;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class DemoBase implements GLSurfaceView.Renderer{

    protected DemoActivity activity;
    protected FFMpeg mpeg;
    protected GLSurfaceView mGLView;

    public DemoBase(DemoActivity activity){
        this.activity=activity;
        mpeg=new FFMpeg();
    }

    protected abstract void onViewCreated();

    protected void onBtn1Click(View view){
    }

    protected void onBtn2Click(View view){

    }

    protected void onBtn3Click(View view){

    }

    protected void onBtn4Click(View view){

    }

    public void configGLView(GLSurfaceView view){
        mGLView=view;
        view.setEGLContextClientVersion(2);
        view.setPreserveEGLContextOnPause(true);
        view.setRenderer(this);
        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

}
