//
// Created by aiya on 2017/3/25.
//

#include "FFMpeg.h"


int FFMpeg::avRegisterAll() {
    av_register_all();
    return 0;
}

int FFMpeg::avFormatAllocOutputContext2() {
    return avformat_alloc_output_context2(&this->mFormatContext,this->mOutputFormat,this->outputFormatName,this->outputFileName);
}

int FFMpeg::avIoOpen() {
    return avio_open2(&this->mIoContext,this->ioUrl,this->avOpenFlag,this->mIoInterruptCB,&this->mDictionary);
}

int FFMpeg::avFormatNewStream() {
    mStream=avformat_new_stream(this->mFormatContext,this->mCodec);
    return 0;
}

int FFMpeg::avCodecFindEncoder() {
    mCodec=avcodec_find_encoder(this->mCodecId);
    return 0;
}

int FFMpeg::avCodecOpen() {
    return avcodec_open2(this->mCodecContext,this->mCodec,&this->mDictionary);
}

int FFMpeg::avFormatWriteHeader() {
    return avformat_write_header(this->mFormatContext,&this->mDictionary);
}

int FFMpeg::avWriteFrame() {
    return av_write_frame(this->mFormatContext,this->mPacket);
}

int FFMpeg::avWriteTrailer() {
    return av_write_trailer(this->mFormatContext);
}

int FFMpeg::avCodecClose() {
    return avcodec_close(this->mCodecContext);
}

int FFMpeg::avFromatFreeContext() {
    avformat_free_context(this->mFormatContext);
    return 0;
}

int FFMpeg::avIoClose() {
    return avio_close(this->mIoContext);
}

void FFMpeg::set(int key, char *value) {

}

void FFMpeg::set(int key, int value) {
}



