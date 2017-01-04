/*
 *
 * FrameCallback.java
 * 
 * Created by Wuwang on 2016/12/30
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package edu.wuwang.codec.camera;

/**
 * Description:
 */
public interface FrameCallback {

    void onFrame(byte[] bytes,long time);

}
