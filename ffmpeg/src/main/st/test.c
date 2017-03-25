
#include "aydec.h"

int test(const char *filename, const char *path)
{
	AYFRAME out;
	int ret;
	int t1 = 0;

	AYDEC *aydec = aydec_open(filename);

	for (int i = 0; ; i++)
	{
		if (aydec_decode_frame(aydec, &out) < 0)
			break;
	}

	aydec_decode_getdelayed_frame(aydec, &out);

	aydec_close(aydec);

	return 0;
}

int main(int argc, char **argv)
{
	//av_log_set_level(AV_LOG_DEBUG);


	test(argv[1], "C:\\project\\ffmpegdemo\\ffmpegdemo\\ffmpegdemo\\test.png");


	return 0;
}
