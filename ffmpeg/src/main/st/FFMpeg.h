//
// Created by aiya on 2017/3/25.
//

#ifndef AUDIOVIDEO_FFMPEG_H
#define AUDIOVIDEO_FFMPEG_H

#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"

class FFMpeg{
    const static int INPUT_FILE=0xF001;
    const static int OUTPUT_FILE=0xF801;
private:
    AVCodec * mCodec;
    AVFormatContext * mFormatContext;
    AVStream * mStream;
    AVCodecParameters * mParams;
    AVOutputFormat * mOutputFormat;
    AVIOContext * mIoContext;
    AVIOInterruptCB * mIoInterruptCB;
    AVDictionary * mDictionary;
    AVCodecID  mCodecId;
    AVCodecContext * mCodecContext;
    AVPacket * mPacket;

    char * outputFormatName;
    char * outputFileName;
    char * ioUrl;
    char * shortName;
    char * fileName;
    char * mimeType;

    int avOpenFlag;
public:
    int avRegisterAll();

    int avFormatAllocOutputContext2();
    int avIoOpen();
    int avFormatNewStream();
    int avCodecFindEncoder();
    int avCodecOpen();
    int avFormatWriteHeader();
    int avWriteFrame();
    int avWriteTrailer();
    int avCodecClose();
    int avFromatFreeContext();
    int avIoClose();
    void set(int key, char * value);
    void set(int key, int value);
};

#endif //AUDIOVIDEO_FFMPEG_H
