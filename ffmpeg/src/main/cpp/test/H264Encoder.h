//
// Created by aiya on 2017/4/20.
//

#ifndef AUDIOVIDEO_H264ENCODER_H
#define AUDIOVIDEO_H264ENCODER_H

#include "Codec.h"

class H264Encoder:public Codec {
private:
    AVOutputFormat * avOutputFormat;
    AVStream * avStream;
    char * cacheFileName= (char *) "/mnt/sdcard/cache.h264";
    int width;
    int height;
    size_t yFrameSize;
    size_t uvFrameSize;
public:
    int start();
    int input(uint8_t * data);
    int output(uint8_t * data);
    void set(int key,int value);
    int get(int key);
    int stop();
};


#endif //AUDIOVIDEO_H264ENCODER_H
