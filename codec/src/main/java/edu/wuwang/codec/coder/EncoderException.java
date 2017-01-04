/*
 *
 * EncoderException.java
 * 
 * Created by Wuwang on 2016/12/31
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package edu.wuwang.codec.coder;

/**
 * Description:
 */
public class EncoderException extends Exception {

    public EncoderException() {
    }

    public EncoderException(String message) {
        super(message);
    }

    public EncoderException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncoderException(Throwable cause) {
        super(cause);
    }

}
