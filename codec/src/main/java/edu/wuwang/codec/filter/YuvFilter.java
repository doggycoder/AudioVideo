package edu.wuwang.codec.filter;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;

public class YuvFilter extends AFilter {

    private int[] textures=new int[3];
    private int[] mHTexs=new int[3];
    private ByteBuffer y,u,v;

    private boolean is420P=true;

    public YuvFilter(Resources mRes) {
        super(mRes);
    }

    public void setDataTypeYUV420P(boolean is420P){
        this.is420P=is420P;
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base.vert",is420P?"shader/yuv.frag":"shader/yuv420sp.frag");
        mHTexs[0]=GLES20.glGetUniformLocation(mProgram,"texY");
        if(is420P){
            mHTexs[1]=GLES20.glGetUniformLocation(mProgram,"texU");
            mHTexs[2]=GLES20.glGetUniformLocation(mProgram,"texV");
        }else{
            mHTexs[1]=GLES20.glGetUniformLocation(mProgram,"texUV");
        }
        createTexture();
    }

    public void updateFrame(int width,int height,byte[] data){
        if(y==null){
            y=ByteBuffer.allocate(width*height);
            if(is420P){
                u=ByteBuffer.allocate(width*height>>2);
                v=ByteBuffer.allocate(width*height>>2);
            }else{
                u=ByteBuffer.allocate(width*height>>1);
            }
        }
        y.clear();
        y.put(data,0,width*height);
        y.position(0);
        if(is420P){
            u.clear();
            u.put(data,width*height,width*height>>2);
            v.clear();
            v.put(data,width*height+(width*height>>2),width*height>>2);
            u.position(0);
            v.position(0);
        }else{
            u.clear();
            u.put(data,width*height,width*height>>1);
            u.position(0);
        }

        Log.e("FFMPEG_LOG","width8height:"+width+"/"+height);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,width,height,0,GLES20.GL_LUMINANCE,
            GLES20.GL_UNSIGNED_BYTE, y);
        if(is420P){
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[1]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,width>>1,height>>1,0,GLES20.GL_LUMINANCE,
                    GLES20.GL_UNSIGNED_BYTE,u);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[2]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,width>>1,height>>1,0,GLES20.GL_LUMINANCE,
                    GLES20.GL_UNSIGNED_BYTE, v);
        }else{
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[1]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE_ALPHA,width>>1,height>>1,0,GLES20.GL_LUMINANCE_ALPHA,
                    GLES20.GL_UNSIGNED_BYTE,u);
        }

    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }

    @Override
    protected void onBindTexture() {
        for (int i=0;i<(is420P?3:2);i++){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0+getTextureType()+i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[i]);
            GLES20.glUniform1i(mHTexs[i],getTextureType()+i);
        }
    }

    private void createTexture(){
        GLES20.glGenTextures(3,textures,0);
        for (int i=0;i<3;i++){
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[i]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
        }
    }
}
