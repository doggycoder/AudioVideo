#include "encode.h"
#include "dlog.h"

EncCtx *open_encoder(int codec_id, int pix_fmt, int bit_rate, int width, int height)
{
	EncCtx *ec = (EncCtx *)malloc(sizeof(EncCtx));
	if (!ec)
		return NULL;

	/* find the video encoder */
	ec->codec = avcodec_find_encoder(codec_id);
	if (!ec->codec) {
		ALOG(LOG_ERROR, "Codec not found\n");
		return NULL;
	}

	ec->c = avcodec_alloc_context3(ec->codec);
	if (!ec->c) {
		ALOG(LOG_ERROR, "Could not allocate video codec context\n");
		return NULL;
	}

	/* put sample parameters */
	ec->c->bit_rate = bit_rate;
	/* resolution must be a multiple of two */
	ec->c->width = width;
	ec->c->height = height;
	/* frames per second */
	ec->c->time_base = (AVRational) { 1, 25 };
	/* emit one intra frame every ten frames
	* check frame pict_type before passing frame
	* to encoder, if frame->pict_type is AV_PICTURE_TYPE_I
	* then gop_size is ignored and the output of encoder
	* will always be I frame irrespective to gop_size
	*/
	ec->c->gop_size = 10;
	ec->c->max_b_frames = 0;
	ec->c->pix_fmt = pix_fmt;// AV_PIX_FMT_YUV420P;

	if (codec_id == AV_CODEC_ID_H264)
		av_opt_set(ec->c->priv_data, "preset", "slow", 0);

	/* open it */
	if (avcodec_open2(ec->c, ec->codec, NULL) < 0) {
		ALOG(LOG_ERROR, "Could not open codec\n");
		return NULL;
	}

	return ec;
}

void close_encoder(EncCtx *ec)
{
	if (ec) {
		avcodec_close(ec->c);
		av_free(ec->c);
		free(ec);
	}

}

void video_encode(EncCtx *ec, AVFrame *frame, int pts, FILE *fp)
{
	int  ret, got_output;

	uint8_t endcode[] = { 0, 0, 1, 0xb7 };

	av_init_packet(&ec->pkt);
	ec->pkt.data = NULL;    // packet data will be allocated by the encoder
	ec->pkt.size = 0;

	frame->pts = pts;

	/* encode the image */
	ret = avcodec_encode_video2(ec->c, &ec->pkt, frame, &got_output);
	if (ret < 0) {
		ALOG(LOG_ERROR, "Error encoding frame\n");
		return;
	}

	if (got_output) {
		if (fp) {
			ALOG(LOG_DEBUG, "Write encoded frame (size=%5d)\n", ec->pkt.size);
			fwrite(ec->pkt.data, 1, ec->pkt.size, fp);
		}
		av_packet_unref(&ec->pkt);
	}
}
