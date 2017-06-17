//
// Created by aiya on 2017/4/5.
//

#ifndef AUDIOVIDEO_MP4H264DECODER_H
#define AUDIOVIDEO_MP4H264DECODER_H

#include "Codec.h"

class Mp4H264Decoder: public Codec {
private:
    int width;
    int height;
    size_t yFrameSize;
    size_t uvFrameSize;
    AVPacket * spsPacket;
    const uint8_t startCode[4]={0,0,0,1};
    int ppsAndspsDispose(int streamId);
public:
    int start();
    int input(uint8_t * data);
    int output(uint8_t * data);
    void set(int key,int value);
    int get(int key);
    int stop();
};


#endif //AUDIOVIDEO_MP4DECODER_H
