package edu.wuwang.codec.mp4processor;

import android.util.Log;

import java.util.concurrent.Semaphore;

import edu.wuwang.codec.mp4processor.halo.EGLHelper;

public class GLEnvironment {

    private static final String TAG="GLEnvironment";

    private Renderer mRenderer=DEFAULT_RENDERER;
    private EGLHelper mEGLHelper;
    private int width=720;
    private int height=1280;

    private Thread mGLThread;
    private boolean mGLFlag=false;

    private Semaphore mSemaphore;

    public GLEnvironment(){
        mEGLHelper=new EGLHelper();
        mSemaphore=new Semaphore(0);
    }

    public void setSurfaceSize(int width,int height){
        this.width=width;
        this.height=height;
    }

    public void setRenderer(Renderer renderer){
        if(renderer!=null){
            this.mRenderer=renderer;
        }else{
            mRenderer=DEFAULT_RENDERER;
        }
    }

    public void setSurface(Object surface){
        mEGLHelper.setSurface(surface);
    }

    public boolean createEnvironment(){
        if(width<=0||height<=0){
            return false;
        }
        mSemaphore.release();
        mGLFlag=true;
        mGLThread=new Thread(mGLRunnable);
        mGLThread.start();
        return true;
    }

    public void destroyEnvironment() throws InterruptedException {
        mGLFlag=false;
        mSemaphore.release();
        mGLThread.join();
    }

    public void requestRenderer(){
        mSemaphore.release();
    }

    private Runnable mGLRunnable=new Runnable() {
        @Override
        public void run() {
            Log.e("wuwang","env thread in");
            if(mEGLHelper.createGLES(width,height)){
                mRenderer.onCreate();
                mRenderer.onSurfaceChanged(width,height);
                while (mGLFlag){
                    mRenderer.onDraw();
                    mEGLHelper.swapBuffers();
                    try {
                        mSemaphore.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mRenderer.onDestroy();
            }
        }
    };

    private static Renderer DEFAULT_RENDERER=new Renderer() {
        @Override
        public void onCreate() {
            Log.e(TAG,"default renderer:onCreate");
        }

        @Override
        public void onSurfaceChanged(int width,int height) {
            Log.e(TAG,"default renderer:onSurfaceChanged("+width+","+height+")");
        }

        @Override
        public void onDraw() {
            Log.e(TAG,"default renderer:onDraw");
        }

        @Override
        public void onDestroy() {
            Log.e(TAG,"default renderer:onDestroy");
        }
    };

    public interface Renderer{
        void onCreate();
        void onSurfaceChanged(int width,int height);
        void onDraw();
        void onDestroy();
    }

}
