//
// Created by aiya on 2017/4/5.
//
#include "Mp4Decoder.h"

int Mp4Decoder::start() {
    const char * test=file("/test.mp4");
    avFormatContext=avformat_alloc_context();
    int ret=avformat_open_input(&avFormatContext,test,NULL,NULL);
    if(ret!=0){
        log(ret,"avformat_open_input");
        return ret;
    }
    ret=avformat_find_stream_info(avFormatContext,NULL);
    AVCodecParameters * parameter=avFormatContext->streams[0]->codecpar;
    if(ret<0){
        log(ret,"avformat_find_stream_info");
        return ret;
    }
    avCodec= avcodec_find_decoder(parameter->codec_id);
    avCodecContext=avcodec_alloc_context3(avCodec);
    ret=avcodec_open2(avCodecContext,avCodec,NULL);
    if(ret!=0){
        log(ret,"avcodec_open2");
        return ret;
    }
    spsPacket=av_packet_alloc();
    av_init_packet(spsPacket);

    avPacket=av_packet_alloc();
    av_init_packet(avPacket);
    avFrame=av_frame_alloc();
    //todo 宽度如果不为16的倍数，需要补成16的倍数，否则画面会出问题
    width=parameter->width;
    height=parameter->height;
    yFrameSize= (size_t) (width * height);
    uvFrameSize= yFrameSize>>2;
    av_log(NULL,AV_LOG_DEBUG,"w,h,yframe,uvframe info:%d,%d,%d,%d",width,height,yFrameSize,uvFrameSize);
    av_log(NULL,AV_LOG_DEBUG," start success");
    ppsAndspsDispose(0);
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
    size_t size=0;
    size_t step=0;
    for (;step<avPacket->size;){
        size=(avPacket->data[step]<<24)|(avPacket->data[step+1]<<16)|(avPacket->data[step+2]<<8)|avPacket->data[step+3];
        memcpy(avPacket->data+step,startCode,4);
        step=step+size+4;
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

int Mp4Decoder::ppsAndspsDispose(int streamId)
{
    int ret=0;
    size_t size= (size_t) avFormatContext->streams[0]->codecpar->extradata_size;
    uint8_t * a=NULL;
    a= (uint8_t *) malloc(size);
    memcpy(a,avFormatContext->streams[0]->codecpar->extradata,size);
    for (int i = 0; i < size ; i++) {
        av_log(NULL,AV_LOG_DEBUG,"pps : data:%x",a[i]);
    }
    uint spsLen=a[6]<<8|a[7];
    uint ppsLen=a[9+spsLen]<<8|a[10+spsLen];
    spsPacket->size=spsLen+ppsLen+8;
    spsPacket->data= (uint8_t *) malloc((size_t) avPacket->size);
    memcpy(spsPacket->data,startCode , 4);
    memcpy(spsPacket->data+4,a+8 ,spsLen);
    memcpy(spsPacket->data+4+spsLen,startCode ,4);
    memcpy(spsPacket->data+8+spsLen,a+11+spsLen ,ppsLen);
    ret=avcodec_send_packet(avCodecContext,spsPacket);
    if(ret!=0){
        log(ret,"avcodec_send_packet");
        return ret;
    }
    ret=avcodec_receive_frame(avCodecContext,avFrame);
    if(ret==0){

    }else{
        log(ret,"avcodec_receive_frame");
    }
    av_packet_unref(spsPacket);
    return 0;
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