//
// Created by aiya on 2017/4/5.
//

#include "Mp4Decoder.h"

int width;
int height;
size_t yFrameSize;
size_t uvFrameSize;

int Mp4Decoder::start() {
    const char * test="/mnt/sdcard/test.264";
    avFormatContext=avformat_alloc_context();
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
    avCodec=avcodec_find_decoder(AV_CODEC_ID_H264);
    avCodecContext=avcodec_alloc_context3(avCodec);
    ret=avcodec_open2(avCodecContext,avCodec,NULL);
    if(ret!=0){
        log(ret,"avcodec_open2");
        return ret;
    }
    avPacket=av_packet_alloc();
    av_init_packet(avPacket);
    avFrame=av_frame_alloc();
    width=avFormatContext->streams[0]->codecpar->width;
    height=avFormatContext->streams[0]->codecpar->height;
    yFrameSize= (size_t) (width * height);
    uvFrameSize= yFrameSize>>2;
    av_log(NULL,AV_LOG_DEBUG,"w,h,yframe,uvframe info:%d,%d,%d,%d",width,height,yFrameSize,uvFrameSize);
    av_log(NULL,AV_LOG_DEBUG," start success");
    return 0;
}

int Mp4Decoder::input(uint8_t *data) {
    return 0;
}

int Mp4Decoder::output(uint8_t *data) {
    int ret=av_read_frame(avFormatContext,avPacket);
    if(ret!=0){
        log(ret,"av_read_frame");
        return ret;
    }
    ret=avcodec_send_packet(avCodecContext,avPacket);
    if(ret!=0){
        log(ret,"avcodec_send_packet");
        return ret;
    }
    ret=avcodec_receive_frame(avCodecContext,avFrame);
    if(ret==0){
        memcpy(data, avFrame->data[0], yFrameSize);
        memcpy(data+yFrameSize, avFrame->data[1], uvFrameSize);
        memcpy(data+yFrameSize+uvFrameSize, avFrame->data[2], uvFrameSize);
    }else{
        log(ret,"avcodec_receive_frame");
    }
    av_packet_unref(avPacket);
    return ret;
}

int Mp4Decoder::stop() {
    avcodec_free_context(&avCodecContext);
    avformat_close_input(&avFormatContext);
    return 0;
}

void Mp4Decoder::set(int key, int value) {

}

int Mp4Decoder::get(int key) {
    switch (key){
        case KEY_WIDTH:
            return width;
        case KEY_HEIGHT:
            return height;
        default:
            break;
    }
    return Codec::get(key);
}