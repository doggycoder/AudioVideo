package edu.wuwang.ffmpeg.filter;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;
import java.nio.ByteBuffer;

public class YuvFilter extends AFilter {

    private int[] textures=new int[3];
    private int[] mHTexs=new int[3];
    private ByteBuffer y,u,v;

    public YuvFilter(Resources mRes) {
        super(mRes);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base.vert","shader/yuv.frag");
        mHTexs[0]=GLES20.glGetUniformLocation(mProgram,"texY");
        mHTexs[1]=GLES20.glGetUniformLocation(mProgram,"texU");
        mHTexs[2]=GLES20.glGetUniformLocation(mProgram,"texV");
        createTexture();
    }

    public void updateFrame(int width,int height,byte[] data){
        if(y==null){
            y=ByteBuffer.allocate(width*height);
            u=ByteBuffer.allocate(width*height>>2);
            v=ByteBuffer.allocate(width*height>>2);
        }
        y.clear();
        y.put(data,0,width*height);

        u.clear();
        u.put(data,width*height,width*height>>2);

        v.clear();
        v.put(data,width*height+(width*height>>2),width*height>>2);


        y.position(0);
        u.position(0);
        v.position(0);
        Log.e("FFMPEG_LOG","width8height:"+width+"/"+height);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,width,height,0,GLES20.GL_LUMINANCE,
            GLES20.GL_UNSIGNED_BYTE, y);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[1]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,width>>1,height>>1,0,GLES20.GL_LUMINANCE,
            GLES20.GL_UNSIGNED_BYTE,u);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[2]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,width>>1,height>>1,0,GLES20.GL_LUMINANCE,
            GLES20.GL_UNSIGNED_BYTE, v);
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }

    @Override
    protected void onBindTexture() {
        for (int i=0;i<3;i++){
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
