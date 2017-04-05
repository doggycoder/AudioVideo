//
// Created by aiya on 2017/4/5.
//

#ifndef AUDIOVIDEO_MP4DECODER_H
#define AUDIOVIDEO_MP4DECODER_H

#include "Codec.h"

class Mp4Decoder: public Codec {

public:
    int start();
    int input(uint8_t * data);
    int output(uint8_t * data);
    void set(int key,int value);
    int get(int key);
    int stop();
};


#endif //AUDIOVIDEO_MP4DECODER_H
