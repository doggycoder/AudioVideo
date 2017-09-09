package edu.wuwang.codec.mp4processor.halo;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

public class EGLHelper {

    private EGLSurface mEGLSurface;
    private EGLContext mEGLContext;
    private EGLDisplay mEGLDisplay;
    private EGLConfig mEGLConfig;

    private EGLContext mShareEGLContext=EGL14.EGL_NO_CONTEXT;

    private boolean isDebug=true;

    private int mEglSurfaceType=EGL14.EGL_WINDOW_BIT;

    private Object mSurface;

    /**
     * @param type one of {@link EGL14#EGL_WINDOW_BIT}、{@link EGL14#EGL_PBUFFER_BIT}、{@link EGL14#EGL_PIXMAP_BIT}
     */
    public void setEGLSurfaceType(int type){
        this.mEglSurfaceType=type;
    }

    public void setSurface(Object surface){
        this.mSurface=surface;
    }

    /**
     * create the environment for OpenGLES
     * @param eglWidth width
     * @param eglHeight height
     */
    public boolean createGLES(int eglWidth, int eglHeight){
        int[] attributes = new int[] {
                EGL14.EGL_SURFACE_TYPE, mEglSurfaceType,      //渲染类型
                EGL14.EGL_RED_SIZE, 8,  //指定RGB中的R大小（bits）
                EGL14.EGL_GREEN_SIZE, 8, //指定G大小
                EGL14.EGL_BLUE_SIZE, 8,  //指定B大小
                EGL14.EGL_ALPHA_SIZE, 8, //指定Alpha大小，以上四项实际上指定了像素格式
                EGL14.EGL_DEPTH_SIZE, 16, //指定深度缓存(Z Buffer)大小
                EGL14.EGL_RENDERABLE_TYPE, 4, //指定渲染api类别, 如上一小节描述，这里或者是硬编码的4(EGL14.EGL_OPENGL_ES2_BIT)
                EGL14.EGL_NONE };  //总是以EGL14.EGL_NONE结尾

        int glAttrs[] = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,  //0x3098是EGL14.EGL_CONTEXT_CLIENT_VERSION，但是4.2以前没有EGL14
                EGL14.EGL_NONE
        };

        int bufferAttrs[]={
                EGL14.EGL_WIDTH,eglWidth,
                EGL14.EGL_HEIGHT,eglHeight,
                EGL14.EGL_NONE
        };

        //获取默认显示设备，一般为设备主屏幕
        mEGLDisplay= EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        //获取版本号，[0]为版本号，[1]为子版本号
        int[] versions=new int[2];
        EGL14.eglInitialize(mEGLDisplay,versions,0,versions,1);
        log(EGL14.eglQueryString(mEGLDisplay, EGL14.EGL_VENDOR));
        log(EGL14.eglQueryString(mEGLDisplay, EGL14.EGL_VERSION));
        log(EGL14.eglQueryString(mEGLDisplay, EGL14.EGL_EXTENSIONS));

        //获取EGL可用配置
        EGLConfig[] configs = new EGLConfig[1];
        int[] configNum = new int[1];
        EGL14.eglChooseConfig(mEGLDisplay, attributes,0, configs,0, 1, configNum,0);
        if(configs[0]==null){
            log("eglChooseConfig Error:"+ EGL14.eglGetError());
            return false;
        }
        mEGLConfig = configs[0];

        //创建EGLContext
        mEGLContext= EGL14.eglCreateContext(mEGLDisplay,mEGLConfig,mShareEGLContext, glAttrs,0);
        if(mEGLContext==EGL14.EGL_NO_CONTEXT){
            return false;
        }
        //获取创建后台绘制的Surface
        switch (mEglSurfaceType){
            case EGL14.EGL_WINDOW_BIT:
                mEGLSurface=EGL14.eglCreateWindowSurface(mEGLDisplay,mEGLConfig,mSurface,new int[]{EGL14.EGL_NONE},0);
                break;
            case EGL14.EGL_PIXMAP_BIT:
                break;
            case EGL14.EGL_PBUFFER_BIT:
                mEGLSurface=EGL14.eglCreatePbufferSurface(mEGLDisplay,mEGLConfig,bufferAttrs,0);
                break;
        }
        if(mEGLSurface==EGL14.EGL_NO_SURFACE){
            log("eglCreateSurface Error:"+EGL14.eglGetError());

            return false;
        }

        if(!EGL14.eglMakeCurrent(mEGLDisplay,mEGLSurface,mEGLSurface,mEGLContext)){
            log("eglMakeCurrent Error:"+EGL14.eglQueryString(mEGLDisplay,EGL14.eglGetError()));
            return false;
        }
        log("gl environment create success");
        return true;
    }

    public void setShareEGLContext(EGLContext context){
        this.mShareEGLContext=context;
    }

    public EGLContext getEGLContext(){
        return mEGLContext;
    }

    public boolean makeCurrent(){
        return EGL14.eglMakeCurrent(mEGLDisplay,mEGLSurface,mEGLSurface,mEGLContext);
    }

    public boolean destroyGLES(){
        EGL14.eglMakeCurrent(mEGLDisplay,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroySurface(mEGLDisplay,mEGLSurface);
        EGL14.eglDestroyContext(mEGLDisplay,mEGLContext);
        EGL14.eglTerminate(mEGLDisplay);
        log("gl destroy gles");
        return true;
    }

    public void setPresentationTime(long time){
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay,mEGLSurface,time);
    }

    public boolean swapBuffers(){
        return EGL14.eglSwapBuffers(mEGLDisplay,mEGLSurface);
    }


    //创建视频数据流的OES TEXTURE
    public int createTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    private void log(String log){
        if(isDebug){
            Log.e("EGLHelper",log);
        }
    }

}
