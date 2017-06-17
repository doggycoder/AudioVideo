//
// Created by aiya on 2017/6/17.
//

#include "Mp4AACDecoder.h"

int Mp4AACDecoder::start() {
    const char * test=file("/test.mp4");
    avFormatContext=avformat_alloc_context();
    cacheFile=fopen(file("/save2.pcm"),"w+b");
    int ret=avformat_open_input(&avFormatContext,test,NULL,NULL);
    if(ret!=0){
        log(ret,"avformat_open_input");
        return ret;
    }
    ret=avformat_find_stream_info(avFormatContext,NULL);
    if(ret<0){
        log(ret,"avformat_find_stream_info");
        return ret;
    }
    AVCodecParameters * parameter=NULL;
    for (int i = 0; i <avFormatContext->nb_streams ; i++) {
        if(avFormatContext->streams[i]->codecpar->codec_type==AVMEDIA_TYPE_AUDIO){
            parameter=avFormatContext->streams[i]->codecpar;
            break;
        }
    }
    if(parameter==NULL)return -1;
    avCodec=avcodec_find_decoder(parameter->codec_id);
    avCodecContext=avcodec_alloc_context3(avCodec);
    ret=avcodec_open2(avCodecContext,avCodec,NULL);
    if(ret!=0){
        log(ret,"avcodec_open2");
        return ret;
    }
    bitRate= (long) parameter->bit_rate;
    sampleRate=parameter->sample_rate;
    channelCount=parameter->channels;
    audioFormat=parameter->format;
    frameSize= (size_t) parameter->frame_size;
    bytesPerSample = (size_t) av_get_bytes_per_sample(avCodecContext->sample_fmt);
    avPacket=av_packet_alloc();
    av_init_packet(avPacket);
    avFrame=av_frame_alloc();

    av_log(NULL,AV_LOG_DEBUG," start success,%d",bytesPerSample);
    return 0;
}

int Mp4AACDecoder::input(uint8_t *data) {
    return AACDecoder::input(data);
}

int Mp4AACDecoder::output(uint8_t *data) {
    return AACDecoder::output(data);
}

int Mp4AACDecoder::stop() {
    return AACDecoder::stop();
}

void Mp4AACDecoder::set(int key, int value) {
    AACDecoder::set(key,value);
}

int Mp4AACDecoder::get(int key) {
    return AACDecoder::get(key);
}