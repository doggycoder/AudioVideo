#ifndef AYDEC_H
#define AYDEC_H

#include <math.h>
#include <sys/time.h>
#include "libavcodec/avcodec.h"

#include "format.h"
#include "swscale.h"
#include "decode.h"
#include "encode.h"


typedef struct
{
	DecCtx *dec;
	ScaleCtx *sc;
	AVFormatContext *ifmt_ctx;
	AVFrame *out;
} AYDEC;

typedef struct
{
	void *data[4];
	int linesize[4];
	int width;
	int height;
	int format;
	int got_frame;
} AYFRAME;

#ifdef __cplusplus
   extern "C"
   {
#endif

AYDEC *aydec_open(const char *filename);

int aydec_decode_frame(AYDEC *aydec, AYFRAME *out);


int aydec_decode_getdelayed_frame(AYDEC *aydec, AYFRAME *out);


int aydec_close(AYDEC *aydec);

#ifdef __cplusplus
}
#endif

#endif
