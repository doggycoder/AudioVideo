//
// Created by aiya on 2017/4/5.
//

#ifndef AUDIOVIDEO_CODEC_H
#define AUDIOVIDEO_CODEC_H

extern "C"{
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
}

class Codec {

public:
    static const int KEY_WIDTH=0x1001;
    static const int KEY_HEIGHT=0x1002;
    static const int KEY_BIT_RATE=0x2001;
    static const int KEY_SAMPLE_RATE=0x2002;
    static const int KEY_AUDIO_FORMAT=0x2003;
    static const int KEY_CHANNEL_COUNT=0x2004;
    static const int KEY_FRAME_SIZE=0x2005;

protected:
    AVFormatContext * avFormatContext;
    AVCodec * avCodec;
    AVCodecContext * avCodecContext;
    AVPacket * avPacket;
    AVFrame * avFrame;

public:
    static char * getInfo(int key);
    static void init();
    static void release();
    static void log(int ret, const char * func);

    virtual int start()=0;
    virtual void set(int key,int value);
    virtual int get(int key);
    virtual int input(uint8_t * data)=0;
    virtual int output(uint8_t * data)=0;
    virtual int stop()=0;
};


#endif //AUDIOVIDEO_CODEC_H
