//
// Created by aiya on 2017/4/5.
//

//
// Created by aiya on 2017/4/5.
//

#include "AACDecoder.h"

FILE * file=NULL;

int AACDecoder::start() {
    const char * test="/mnt/sdcard/test.aac";
    avFormatContext=avformat_alloc_context();
    file=fopen("/mnt/sdcard/save.pcm","w+b");
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
    avCodec=avcodec_find_decoder(AV_CODEC_ID_AAC);
    avCodecContext=avcodec_alloc_context3(avCodec);
    ret=avcodec_open2(avCodecContext,avCodec,NULL);
    if(ret!=0){
        log(ret,"avcodec_open2");
        return ret;
    }
    AVCodecParameters * param=avFormatContext->streams[0]->codecpar;
    bitRate= (long) param->bit_rate;
    sampleRate=param->sample_rate;
    channelCount=param->channels;
    audioFormat=param->format;
    frameSize= (size_t) param->frame_size;
    bytesPerSample = (size_t) av_get_bytes_per_sample(avCodecContext->sample_fmt);
    avPacket=av_packet_alloc();
    av_init_packet(avPacket);
    avFrame=av_frame_alloc();

    av_log(NULL,AV_LOG_DEBUG," start success,%d",bytesPerSample);
    return 0;
}

int AACDecoder::input(uint8_t *data) {
    return 0;
}

int AACDecoder::output(uint8_t *data) {
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
    bytesPerSample = (size_t) av_get_bytes_per_sample(avCodecContext->sample_fmt);
    if(ret==0){
        //PCM采样数据的排列方式，一般是交错排列输出，AVFrame中存储PCM数据各个通道是分开存储的
        //所以多通道的时候，需要根据PCM的格式和通道数，排列好后存储
        if(channelCount>1){
            //多通道的
            for (int i = 0; i < frameSize; i++) {
                for (int j=0;j< channelCount;j++){
                    memcpy(data+(i*channelCount+j)*bytesPerSample, avFrame->data[j]+i*bytesPerSample,bytesPerSample);
                    fwrite(avFrame->data[j]+i*bytesPerSample,1,bytesPerSample,file);
                }
            }
            av_log(NULL,AV_LOG_DEBUG,"avcodec_receive_frame ok,%d,%d",bytesPerSample*frameSize*2,avFrame->nb_samples);
        }else{
            //单通道的，
            memcpy(data,avFrame->data[0],frameSize*channelCount*bytesPerSample);
            fwrite(avFrame->data[0],1,frameSize*bytesPerSample*channelCount,file);
            av_log(NULL,AV_LOG_DEBUG,"avcodec_receive_frame ok,%d",frameSize*channelCount*bytesPerSample);
        }
    }else{
        log(ret,"avcodec_receive_frame");
    }
    av_packet_unref(avPacket);
    return ret;
}

int AACDecoder::stop() {
    fclose(file);
    avcodec_free_context(&avCodecContext);
    avformat_close_input(&avFormatContext);
    return 0;
}

void AACDecoder::set(int key, int value) {

}

int AACDecoder::get(int key) {
    switch (key){
        case KEY_BIT_RATE:
            return bitRate;
        case KEY_SAMPLE_RATE:
            return sampleRate;
        case KEY_CHANNEL_COUNT:
            return channelCount;
        case KEY_AUDIO_FORMAT:
            return audioFormat;
        case KEY_FRAME_SIZE:
            return frameSize;
        default:
            break;
    }
    return Codec::get(key);
}