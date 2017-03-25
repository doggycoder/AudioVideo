
#include "swscale.h"
#include "dlog.h"

ScaleCtx *ay_open_swscale(int src_w, int src_h, int src_pix_fmt, int dst_w, int dst_h, int dst_pix_fmt)
{
	int ret;

	ScaleCtx *sc = (ScaleCtx *)malloc(sizeof(ScaleCtx));
	if (!sc) {
		return NULL;
	}
	memset(sc, 0, sizeof(ScaleCtx));

	/* create scaling context */
	sc->sws_ctx = sws_getContext(src_w, src_h, src_pix_fmt,
	                             dst_w, dst_h, dst_pix_fmt,
	                             SWS_BILINEAR, NULL, NULL, NULL);
	if (!sc->sws_ctx) {
		ALOG(LOG_ERROR,
		        "Impossible to create scale context for the conversion "
		        "fmt:%s s:%dx%d -> fmt:%s s:%dx%d\n",
		        av_get_pix_fmt_name(src_pix_fmt), src_w, src_h,
		        av_get_pix_fmt_name(dst_pix_fmt), dst_w, dst_h);
		return NULL;
	}

	sc->frame = av_frame_alloc();
	if (!sc->frame) {
		ALOG(LOG_ERROR, "Could not allocate video frame\n");
		return NULL;
	}

	sc->frame->format = dst_pix_fmt;
	sc->frame->width = dst_w;
	sc->frame->height = dst_h;

	/* the image can be allocated by any means and av_image_alloc() is
	* just the most convenient way if av_malloc() is to be used */
	ret = av_image_alloc(sc->frame->data, sc->frame->linesize, sc->frame->width, sc->frame->height, sc->frame->format, 1);
	if (ret < 0) {
		ALOG(LOG_ERROR, "Could not allocate raw picture buffer\n");
		return NULL;
	}

	sc->src_w = src_w;
	sc->src_h = src_h;
	sc->dst_w = dst_w;
	sc->dst_h = dst_h;
	sc->src_pix_fmt = src_pix_fmt;
	sc->dst_pix_fmt = dst_pix_fmt;

	return sc;
}

void ay_close_swscale(ScaleCtx *sc)
{
	if (sc) {
		sws_freeContext(sc->sws_ctx);
		av_frame_free(&sc->frame);
		free(sc);
	}
}


int ay_do_swscale(ScaleCtx *sc, AVFrame *src)
{
	const char *dst_size = NULL;
	const char *dst_filename = NULL;
	FILE *dst_file;
	int dst_bufsize;
	int i, ret;

	/* convert to destination format */
	ret = sws_scale(sc->sws_ctx, (const uint8_t * const*)src->data,
	                src->linesize, 0, src->height, sc->frame->data, sc->frame->linesize);

	return ret;
}

