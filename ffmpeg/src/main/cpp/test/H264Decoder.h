//
// Created by aiya on 2017/4/5.
//

#ifndef AUDIOVIDEO_H264DECODER_H
#define AUDIOVIDEO_H264DECODER_H

#include "Codec.h"

class H264Decoder: public Codec {
private:
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


#endif //AUDIOVIDEO_H264DECODER_H
