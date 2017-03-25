#ifndef ENCODE_H
#define ENCODE_H

#include <libavutil/opt.h>
#include <libavcodec/avcodec.h>

typedef struct
{
	AVCodec *codec;
	AVCodecContext *c;
	AVFrame *frame;
	AVPacket pkt;
} EncCtx;


EncCtx *open_encoder(int codec_id, int pix_fmt, int bit_rate, int width, int height);

void close_encoder(EncCtx *ec);

void video_encode(EncCtx *ec, AVFrame *frame, int pts, FILE *fp);



#endif