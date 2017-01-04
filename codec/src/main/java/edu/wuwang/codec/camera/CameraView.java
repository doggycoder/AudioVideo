package edu.wuwang.codec.camera;

import java.nio.ByteBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import edu.wuwang.codec.filter.AFilter;

/**
 * Description:
 */
public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private AiyaCamera mCamera;

    private CameraDrawer mDrawer;

    private boolean isSetParm=false;
    private int dataWidth=0,dataHeight=0;

    private int cameraId=1;
    private FrameCallback mFrameCallback;
//    private AiyaCameraEffect mEffect;

    public CameraView(Context context) {
        this(context,null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
//        mEffect=AiyaCameraEffect.getInstance();
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        setPreserveEGLContextOnPause(true);
        setCameraDistance(100);
        mDrawer=new CameraDrawer(getResources());
        mCamera=new AiyaCamera();
        IAiyaCamera.Config mConfig=new IAiyaCamera.Config();
        mConfig.minPreviewWidth=720;
        mConfig.minPictureWidth=720;
        mConfig.rate=1.778f;
        mCamera.setConfig(mConfig);
    }

    public void setParams(int type,int ... params){
        mDrawer.setParams(type,params);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isSetParm){
            open(cameraId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera.close();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mDrawer.onSurfaceCreated(gl, config);
        if(!isSetParm){
            open(cameraId);
            stickerInit();
        }
        mDrawer.setPreviewSize(dataWidth,dataHeight);
    }

    private void open(final int cameraId){
        mCamera.close();
        mCamera.open(cameraId);
        mDrawer.setCameraId(cameraId);
        final Point previewSize=mCamera.getPreviewSize();
        dataWidth=previewSize.x;
        dataHeight=previewSize.y;
        for (int i=0;i<3;i++){
            mCamera.addBuffer(new byte[dataWidth*dataHeight*4]);
        }
        mDrawer.setCamera(mCamera.getCamera());
        mCamera.setOnPreviewFrameCallbackWithBuffer(new AiyaCamera.PreviewFrameCallback() {

            @Override
            public void onPreviewFrame(byte[] bytes, int width, int height) {
                //TODO 增加限制
                if(isSetParm&&mDrawer!=null){
                    mDrawer.update(bytes);
                    requestRender();
                }else{
                    mCamera.addBuffer(bytes);
                }
            }
        });
        mCamera.setPreviewTexture(mDrawer.getTexture());
        mCamera.preview();
    }

    public void switchCamera(){
        cameraId=cameraId==0?1:0;
        open(cameraId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mDrawer.onSurfaceChanged(gl,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if(isSetParm){
            mDrawer.onDrawFrame(gl);
        }
    }

    public void onDestroy(){
        setPreserveEGLContextOnPause(false);
        onPause();
    }

    public void setFrameCallback(int width,int height,FrameCallback frameCallback){
        this.mFrameCallback=frameCallback;
        mDrawer.setFrameCallback(width,height,frameCallback);
    }

    public void setFairLevel(int level){
//        mEffect.set(AiyaCameraEffect.SET_BEAUTY_LEVEL,level);
    }

    public void setEffect(String effect){
//        mEffect.setEffect(effect);
    }

    public void startRecord(){
        mDrawer.setKeepCallback(true);
    }

    public void stopRecord(){
        mDrawer.setKeepCallback(false);
    }

    public void takePhoto(){
        mDrawer.setOneShotCallback(true);
    }

    /**
     * 增加自定义滤镜
     * @param filter   自定义滤镜
     * @param isBeforeSticker 是否增加在贴纸之前
     */
    public void addFilter(AFilter filter, boolean isBeforeSticker){
        mDrawer.addFilter(filter,isBeforeSticker);
    }

    private void stickerInit(){
        if(!isSetParm&&dataWidth>0&&dataHeight>0) {
            isSetParm = true;
//            mEffect.set(AiyaCameraEffect.SET_IN_WIDTH,dataWidth);
//            mEffect.set(AiyaCameraEffect.SET_IN_HEIGHT,dataHeight);
//            mEffect.setProcessCallback(mcallback);
//            mEffect.setTrackCallback(mTrackCallback);
        }
    }

    private ByteBuffer[] mBuffer=new ByteBuffer[3];
    private int i=0;

//    private ProcessCallback mcallback = new ProcessCallback() {
//
//        @Override
//        public void onFinished() {
//
//        }
//    };
//
//    private float[] infos=new float[20];
//
//    private TrackCallback mTrackCallback=new TrackCallback() {
//        @Override
//        public void onTrack(int trackCode,float[] info) {
//            EData.data.setTrackCode(trackCode);
//        }
//
//    };

}
