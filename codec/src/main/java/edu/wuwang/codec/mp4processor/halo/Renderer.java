package edu.wuwang.codec.mp4processor.halo;

/**
 * Created by aiya on 2017/9/2.
 */

public interface Renderer {

    void onCreate(int width,int height);
    void onDraw();
    void onDestroy();

}
