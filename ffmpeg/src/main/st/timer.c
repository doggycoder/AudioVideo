#include <sys/time.h>
#include "timer.h"
#include "dlog.h"

TEXPORT  int timer_start()
{
	struct timeval tv;
	gettimeofday(&tv, NULL);
	return tv.tv_usec / 1000 + (tv.tv_sec & 0xffffff) * 1000;
}

TEXPORT  int timer_end(char *str, int t1)
{
	int t2;
	struct timeval tv;

	gettimeofday(&tv, NULL);
	t2 = tv.tv_usec / 1000 + (tv.tv_sec & 0xffffff) * 1000;

	ALOG(LOG_INFO, "%s take %dms", str, t2 - t1);
	return t2;
}

