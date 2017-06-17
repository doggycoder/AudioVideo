//
// Created by aiya on 2017/6/17.
//

#ifndef AUDIOVIDEO_MP4HAACDECODER_H
#define AUDIOVIDEO_MP4HAACDECODER_H

#include "AACDecoder.h"

class Mp4AACDecoder :public AACDecoder{

public:
    int start();
    int input(uint8_t * data);
    int output(uint8_t * data);
    void set(int key,int value);
    int get(int key);
    int stop();
};


#endif //AUDIOVIDEO_MP4HAACDECODER_H
