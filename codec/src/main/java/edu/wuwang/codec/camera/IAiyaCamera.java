package edu.wuwang.codec.camera;

import android.graphics.Point;
import android.graphics.SurfaceTexture;

/**
 * Description:
 */
public interface IAiyaCamera {

    void open(int cameraId);

    void setPreviewTexture(SurfaceTexture texture);

    void setConfig(Config config);

    void setOnPreviewFrameCallback(PreviewFrameCallback callback);

    void preview();

    Point getPreviewSize();

    Point getPictureSize();

    boolean close();

    class Config{
        float rate=1.778f; //宽高比
        int minPreviewWidth;
        int minPictureWidth;
    }

    interface PreviewFrameCallback{
        void onPreviewFrame(byte[] bytes, int width, int height);
    }

}
