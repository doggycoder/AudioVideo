#ifndef DECODE_H
#define DECODE_H

#include "libavcodec/avcodec.h"

typedef struct
{
	AVCodec *codec;
	AVCodecContext *c;
	AVFrame *frame;
	AVPacket avpkt;
} DecCtx;

DecCtx *ay_open_decoder(enum AVCodecID id);

void ay_close_decoder(DecCtx *dec);

int ay_decode_frame(DecCtx *dec, int *frame_count, int *got_frame);

#endif
