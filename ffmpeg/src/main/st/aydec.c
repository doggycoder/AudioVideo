
#include "aydec.h"
#include "dlog.h"
#include "timer.h"

AVFrame *recovery_rgba(AVFrame *frame, AVFrame *frameRGBA32, AVFrame *frameYUV420)
{
	int ret;

	// copy rgb
	memcpy(frame->data[0], frameRGBA32->data[0], frame->linesize[0] * frame->height);

	// copy alpha
	uint8_t *dst = frame->data[0]; // order: R G B A R G B A ...
	uint8_t *src = frameYUV420->data[0] + frameYUV420->linesize[0] * frameYUV420->height / 2;
	for (int j = 0; j < frame->height; j++)
	{
		for (int i = 0; i < frame->width; i++)
			dst[i * 4 + 3] = src[i];
		src += frameYUV420->linesize[0];
		dst += frame->linesize[0];
	}

	return frame;
}



static AVFrame *ay_malloc_frame(int width, int height, int pixfmt)
{
	int ret;
	AVFrame *frame = NULL;

	frame = av_frame_alloc();
	if (!frame) {
		ALOG(LOG_ERROR, "Could not allocate video frame\n");
		return NULL;
	}

	frame->format = pixfmt;//AV_PIX_FMT_BGR32;
	frame->width = width;
	frame->height = height;

	/* the image can be allocated by any means and av_image_alloc() is
	* just the most convenient way if av_malloc() is to be used */
	ret = av_image_alloc(frame->data, frame->linesize, frame->width, frame->height, frame->format, 1);
	if (ret < 0) {
		ALOG(LOG_ERROR, "Could not allocate raw picture buffer\n");
		return NULL;
	}

	return frame;
}

static void ay_free_frame(AVFrame **frame)
{
	av_frame_free(frame);
}


AYDEC *aydec_open(const char *filename)
{
	av_register_all();

	AYDEC *aydec = (AYDEC *)malloc(sizeof(AYDEC));
	if (!aydec)
		return NULL;
	memset(aydec, 0, sizeof(AYDEC));

	aydec->ifmt_ctx = ay_open_video(filename);
	if (!aydec->ifmt_ctx)
		return NULL;

	aydec->dec = ay_open_decoder(AV_CODEC_ID_H264);

	return aydec;
}


int aydec_decode_frame(AYDEC *aydec, AYFRAME *out)
{
	int t1, ret;
	static int frame_count = 0;
	DecCtx *dec;
	AVFormatContext *ifmt_ctx;
	out->got_frame = 0;

	if (!aydec)
		return -3;

	dec = aydec->dec;
	ifmt_ctx = aydec->ifmt_ctx;

	if (!dec || !ifmt_ctx)
		return -2;

	ret = ay_read_frame(ifmt_ctx, &dec->avpkt);
	if (ret < 0)
	{
		aydec_decode_getdelayed_frame(aydec, out);
		return ret;
	}

	// printf("frame %3d size %d\n", i, dec->avpkt.size);

	uint8_t *data = dec->avpkt.data;

	while (data < dec->avpkt.data + dec->avpkt.size - 4)
	{
		int len = (data[0] << 24) | (data[1] << 16) | (data[2] << 8) | (data[3]);
		data[0] = data[1] = data[2] = 0; data[3] = 1;
		data += len + 4;
	}
	t1 = timer_start();
	if (ay_decode_frame(dec, &frame_count, &out->got_frame) < 0)
		return -1;
	timer_end("decode frame", t1);

	if (out->got_frame)
	{
		out->format = dec->frame->format;
		out->width  = dec->frame->width;
		out->height = dec->frame->height / 2;
		out->data[0] = (void *)dec->frame->data[0];
		out->data[1] = (void *)dec->frame->data[1];
		out->data[2] = (void *)dec->frame->data[2];
		out->data[3] = (uint8_t *)dec->frame->data[0] + dec->frame->height * dec->frame->linesize[0] / 2; // alpha
		out->linesize[0] = dec->frame->linesize[0];
		out->linesize[1] = dec->frame->linesize[1];
		out->linesize[2] = dec->frame->linesize[2];
		out->linesize[3] = dec->frame->linesize[0];

		if (0)
		{
			EncCtx *enc = open_encoder(AV_CODEC_ID_PNG, aydec->out->format, 0, aydec->out->width, aydec->out->height);
			if (enc)
			{
				FILE *fp;
				char name[256] = { 0 };
				sprintf(name, "%s/out_%03d.png", "/sdcard/out", frame_count);
				fp = fopen(name, "wb");
				video_encode(enc, aydec->out, frame_count, fp);
				close_encoder(enc);
				fclose(fp);
			}
		}
	}

	return 0;
}

int aydec_decode_getdelayed_frame(AYDEC *aydec, AYFRAME *out)
{
	DecCtx *dec = aydec->dec;

	dec->avpkt.data = NULL;
	dec->avpkt.size = 0;

	ay_decode_frame(aydec->dec, NULL, &out->got_frame);

	if (out->got_frame)
	{
		out->format = dec->frame->format;
		out->width  = dec->frame->width;
		out->height = dec->frame->height / 2;
		out->data[0] = (void *)dec->frame->data[0];
		out->data[1] = (void *)dec->frame->data[1];
		out->data[2] = (void *)dec->frame->data[2];
		out->data[3] = (uint8_t *)dec->frame->data[0] + dec->frame->height * dec->frame->linesize[0] / 2; // alpha
		out->linesize[0] = dec->frame->linesize[0];
		out->linesize[1] = dec->frame->linesize[1];
		out->linesize[2] = dec->frame->linesize[2];
		out->linesize[3] = dec->frame->linesize[0];
	}

	return out->got_frame;
}


int aydec_close(AYDEC *aydec)
{
	if (aydec)
	{
		ay_close_swscale(aydec->sc);
		ay_close_decoder(aydec->dec);
		ay_close_video(&aydec->ifmt_ctx);
		ay_free_frame(&aydec->out);
		free(aydec);
	}

	return 0;
}

int write_frame(FILE *fp, AVFrame *frame)
{
	int size = 0;

	if (fp && (frame->format == AV_PIX_FMT_YUV420P))
	{
		uint8_t *y = frame->data[0];
		uint8_t *u = frame->data[1];
		uint8_t *v = frame->data[2];

		for (int i = 0; i < frame->height; i++)
		{
			fwrite(y + i * frame->linesize[0], 1, frame->width, fp);
		}

		for (int i = 0; i < frame->height / 2; i++)
		{
			fwrite(u + i * frame->linesize[1], 1, frame->width / 2, fp);
		}

		for (int i = 0; i < frame->height / 2; i++)
		{
			fwrite(v + i * frame->linesize[2], 1, frame->width / 2, fp);
		}
		size = frame->width * frame->height * 3 / 2;
	}

	return size;
}


