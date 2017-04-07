//
// Created by aiya on 2017/4/5.
//

#ifndef AUDIOVIDEO_AACDECODER_H
#define AUDIOVIDEO_AACDECODER_H

#include "Codec.h"

class AACDecoder: public Codec {
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


#endif //AUDIOVIDEO_AACDECODER_H
