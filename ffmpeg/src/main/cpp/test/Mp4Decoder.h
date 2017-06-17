//
// Created by aiya on 2017/6/17.
//

#ifndef AUDIOVIDEO_MP4DECODER_H
#define AUDIOVIDEO_MP4DECODER_H

#include "Codec.h"
#include "Mp4AACDecoder.h"
#include "Mp4H264Decoder.h"

class Mp4Decoder :public Codec{
private:
    int type;
    Codec * mp4Codec;

public:
    int start();
    void set(int key,int value);
    int get(int key);
    int input(uint8_t * data);
    int output(uint8_t * data);
    int stop();

};


#endif //AUDIOVIDEO_MP4DECODER_H
