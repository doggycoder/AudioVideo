
#include "decode.h"
#include "dlog.h"


DecCtx *ay_open_decoder(enum AVCodecID id)
{
	DecCtx *dc = (DecCtx *)malloc(sizeof(DecCtx));
	if (!dc)
		return NULL;

	av_init_packet(&dc->avpkt);

	/* find the MPEG-1 video decoder */
	dc->codec = avcodec_find_decoder(id);
	if (!dc->codec) {
		ALOG(LOG_ERROR, "Codec not found\n");
		return NULL;
	}

	dc->c = avcodec_alloc_context3(dc->codec);
	if (!dc->c) {
		ALOG(LOG_ERROR, "Could not allocate video codec context\n");
		return NULL;
	}

	if (dc->codec->capabilities & AV_CODEC_CAP_TRUNCATED)
		dc->c->flags |= AV_CODEC_FLAG_TRUNCATED; // we do not send complete frames
	dc->c->thread_count = 4;
	dc->c->thread_type = FF_THREAD_FRAME | FF_THREAD_SLICE;

	/* For some codecs, such as msmpeg4 and mpeg4, width and height
	MUST be initialized there because this information is not
	available in the bitstream. */

	/* open it */
	if (avcodec_open2(dc->c, dc->codec, NULL) < 0) {
		ALOG(LOG_ERROR, "Could not open codec\n");
		return NULL;
	}

	dc->frame = av_frame_alloc();
	if (!dc->frame) {
		ALOG(LOG_ERROR, "Could not allocate video frame\n");
		return NULL;
	}

	return dc;
}


void ay_close_decoder(DecCtx *dec)
{
	if (dec)
	{
		avcodec_close(dec->c);
		av_free(dec->c);
		av_frame_free(&dec->frame);
		free(dec);
	}
}

int ay_decode_frame(DecCtx *dec, int *frame_count, int *got_frame)
{
	int len;

	*got_frame = 0;
	len = avcodec_decode_video2(dec->c, dec->frame, got_frame, &dec->avpkt);
	if (len < 0) {
		ALOG(LOG_ERROR, "Error while decoding frame %d\n", *frame_count);
		return len;
	}
	if (*got_frame) {
		if (frame_count)
			(*frame_count)++;
	}
	if (dec->avpkt.data) {
		dec->avpkt.size -= len;
		dec->avpkt.data += len;
	}
	return 0;
}

