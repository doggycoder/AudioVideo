//
// Created by aiya on 2017/4/20.
//

#include "H264Encoder.h"

static uint8_t * datas;

int H264Encoder::start() {
    int ret=avformat_alloc_output_context2(&avFormatContext,NULL,"h264",NULL);
    if(ret!=0){
        log(ret,"avformat_alloc_output_context2");
        return ret;
    }
    ret=avio_open2(&avFormatContext->pb,cacheFileName,AVIO_FLAG_READ_WRITE,NULL,NULL);
    if(ret!=0){
        log(ret,"avio_open");
        return ret;
    }
    avStream=avformat_new_stream(avFormatContext,NULL);
    avCodec=avcodec_find_encoder(avFormatContext->oformat->video_codec);
    avCodecContext=avcodec_alloc_context3(avCodec);
    avCodecContext->width=386;
    avCodecContext->height=640;
    avCodecContext->bit_rate=44100;
    avCodecContext->qmin=10;
    avCodecContext->qmax=25;
    avCodecContext->time_base.den=1;
    avCodecContext->time_base.num=25;
    avCodecContext->pix_fmt=AV_PIX_FMT_YUV420P;
    avcodec_open2(avCodecContext,avCodec,NULL);
    ret=avformat_write_header(avFormatContext,NULL);
    if(ret!=0){
        log(ret,"avformat_write_header");
        return ret;
    }
    avPacket=av_packet_alloc();
    av_init_packet(avPacket);
    avFrame=av_frame_alloc();
    return 0;
}

int H264Encoder::input(uint8_t *data) {
    return 0;
}

int H264Encoder::output(uint8_t *data) {
    avFrame->data[0]=datas;
    int ret=avcodec_send_frame(avCodecContext,avFrame);
    if(ret!=0){
        log(ret,"avcodec_send_frame");
    }
    ret=avcodec_receive_packet(avCodecContext,avPacket);
    if(ret!=0){
        log(ret,"avcodec_receive_packet");
    }
    av_frame_unref(avFrame);
    return 0;
}

int H264Encoder::stop() {
    avcodec_free_context(&avCodecContext);
    avformat_close_input(&avFormatContext);
    return 0;
}

void H264Encoder::set(int key, int value) {

}

int H264Encoder::get(int key) {
    return 0;
}