#include "format.h"
#include "dlog.h"

#define USE_BUFFER 0

struct buffer_data {
	uint8_t *ptr;
	size_t buff_size;
	size_t pos;
	size_t size; ///< size left in the buffer
};

static int read_func(void *opaque, uint8_t *buf, int size)
{
	struct buffer_data *bd = (struct buffer_data *)opaque;
	size = FFMIN(size, bd->size);

	ALOG(LOG_DEBUG, "ptr:%p, pos:%zu, size:%zu\n", bd->ptr, bd->pos, bd->size);

	/* copy internal buffer data to buf */
	memcpy(buf, bd->ptr + bd->pos, size);
	bd->pos  += size;
	bd->size -= size;

	return size;
}

static int64_t seek_func(void *opaque, int64_t offset, int whence)
{
	struct buffer_data *bd = (struct buffer_data *)opaque;

	if (bd == NULL) {
		ALOG(LOG_DEBUG, "bd == NULL");
		return -1;
	}
	else if (whence == AVSEEK_SIZE) {
		ALOG(LOG_DEBUG, "whence == AVSEEK_SIZE, offset %d, ptr %p", offset, bd->ptr);
		return  bd->buff_size ; // "size of my handle in bytes"
	}
	else if (whence == SEEK_CUR) { // relative to current of file
		ALOG(LOG_DEBUG, "whence == SEEK_CUR, offset %d", offset);
		bd->pos  += offset;
	}
	else if (whence == SEEK_END) { // relative to end of file
		ALOG(LOG_DEBUG, "whence == SEEK_END, offset %d", offset);
		if (offset >= 0)
			return -1;
		bd->pos = bd->buff_size + offset;
	}
	else if (whence == SEEK_SET) { // relative to start of file
		ALOG(LOG_DEBUG, "whence == SEEK_SET, offset %d", offset);
		bd->pos = offset;
	}
	else {
		ALOG(LOG_DEBUG, "Error Seek, offset %d", offset);
		return -1;
	}

	bd->size = bd->buff_size - bd->pos - 1;
	return bd->pos;
}

AVFormatContext *ay_open_video(const char *filename)
{
	int ret;
	unsigned int i;

	AVFormatContext *ifmt_ctx = NULL;

#if USE_BUFFER
	AVIOContext *avio_ctx = NULL;
	uint8_t *buffer = NULL, *avio_ctx_buffer = NULL;
	size_t buffer_size, avio_ctx_buffer_size = 4096;
	struct buffer_data bd = { 0 };
	AVProbeData probeData;


	/* slurp file content into buffer */
	ret = av_file_map(filename, &buffer, &buffer_size, 0, NULL);
	if (ret < 0)
		goto end;
	probeData.buf = buffer;
	probeData.buf_size = buffer_size;
	probeData.filename = "";

	/* fill opaque structure used by the AVIOContext read callback */
	bd.ptr  = buffer;
	bd.buff_size = buffer_size;
	bd.size = buffer_size;
	bd.pos = 0;

	if (!(ifmt_ctx = avformat_alloc_context())) {
		ret = AVERROR(ENOMEM);
		goto end;
	}

	avio_ctx_buffer = av_malloc(avio_ctx_buffer_size);
	if (!avio_ctx_buffer) {
		ret = AVERROR(ENOMEM);
		goto end;
	}

	avio_ctx = avio_alloc_context(avio_ctx_buffer, avio_ctx_buffer_size,
	                              0, &bd, &read_func, NULL, NULL);
	if (!avio_ctx) {
		ret = AVERROR(ENOMEM);
		goto end;
	}
	ifmt_ctx->pb = avio_ctx;
	ifmt_ctx->iformat = av_probe_input_format(&probeData, 1);
	ifmt_ctx->flags = AVFMT_FLAG_CUSTOM_IO | AVFMT_NOFILE;
#endif

	if ((ret = avformat_open_input(&ifmt_ctx, filename, NULL, NULL)) < 0) {
		ALOG(LOG_ERROR, "Cannot open input file %s\n", filename);
		goto end;
	}


	if ((ret = avformat_find_stream_info(ifmt_ctx, NULL)) < 0) {
		ALOG(LOG_ERROR, "Cannot find stream information\n");
		goto end;
	}

	for (i = 0; i < ifmt_ctx->nb_streams; i++) {
		AVStream *stream;
		AVCodecContext *codec_ctx;
		stream = ifmt_ctx->streams[i];
		codec_ctx = stream->codec;
		/* Reencode video & audio and remux subtitles etc. */
		if (codec_ctx->codec_type == AVMEDIA_TYPE_VIDEO) {
			/* Open decoder */
			ret = avcodec_open2(codec_ctx,
			                    avcodec_find_decoder(codec_ctx->codec_id), NULL);
			if (ret < 0) {
				ALOG(LOG_ERROR, "Failed to open decoder for stream #%u\n", i);
				goto end;
			}
		}
	}

	av_dump_format(ifmt_ctx, 0, filename, 0);
	return ifmt_ctx;

end:
	avformat_close_input(&ifmt_ctx);

#if USE_BUFFER
	/* note: the internal buffer could have changed, and be != avio_ctx_buffer */
	if (avio_ctx) {
		av_freep(&avio_ctx->buffer);
		av_freep(&avio_ctx);
	}
	av_file_unmap(buffer, buffer_size);
#endif

	ALOG(LOG_ERROR, "Error occurred: %s\n", av_err2str(ret));
	return NULL;
}


int ay_read_frame(AVFormatContext *ifmt_ctx, AVPacket *pkt)
{
	int ret = av_read_frame(ifmt_ctx, pkt);
	return ret;
}

void ay_close_video(AVFormatContext **ctx)
{
	avformat_close_input(ctx);
}
