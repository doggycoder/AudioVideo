//
// Created by aiya on 2017/6/17.
//

#include "Mp4Decoder.h"

int Mp4Decoder::start() {
    mp4Codec->setCachePath(cachePath);
    return mp4Codec->start();
}

int Mp4Decoder::output(uint8_t *data) {
    return mp4Codec->output(data);
}

int Mp4Decoder::input(uint8_t *data) {
    return mp4Codec->input(data);
}

int Mp4Decoder::stop() {
    return mp4Codec->stop();
}

int Mp4Decoder::get(int key) {
    return mp4Codec->get(key);
}

void Mp4Decoder::set(int key, int value) {
    if(key==Codec::KEY_FLAG){
        this->type=value;
        if(type==0){
            mp4Codec=new Mp4H264Decoder();
        }else{
            mp4Codec=new Mp4AACDecoder();
        }
    }else{
        if(mp4Codec){
            mp4Codec->set(key,value);
        }
    }
}
