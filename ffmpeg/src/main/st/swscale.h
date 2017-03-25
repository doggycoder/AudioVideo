#ifndef SWSCALE_H
#define SWSCALE_H

#include "libavutil/imgutils.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"

typedef struct
{
	struct SwsContext *sws_ctx;
	uint8_t *src_data[4];
	uint8_t *dst_data[4];
	int src_linesize[4];
	int dst_linesize[4];
	enum AVPixelFormat src_pix_fmt;// = AV_PIX_FMT_YUV420P;
	enum AVPixelFormat dst_pix_fmt;// = AV_PIX_FMT_RGB24;
	int src_w, src_h;
	int dst_w, dst_h;
	AVFrame *frame;
} ScaleCtx;

ScaleCtx *ay_open_swscale(int src_w, int src_h, int src_pix_fmt, int dst_w, int dst_h, int dst_pix_fmt);

void ay_close_swscale(ScaleCtx *sc);

int ay_do_swscale(ScaleCtx *sc, AVFrame *src);

#endif
