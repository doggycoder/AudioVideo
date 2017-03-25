#ifndef FORMAT_H
#define FORMAT_H

#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"

AVFormatContext *ay_open_video(const char *filename);

int ay_read_frame(AVFormatContext *ifmt_ctx, AVPacket *pkt);

void ay_close_video(AVFormatContext **ctx);


#endif