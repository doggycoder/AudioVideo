#include <array>
#include <GLES3/gl3.h>
#include "glexport.h"
#include "sys/time.h"
#include "android/log.h"

using namespace std;

static int timer_start()
{
	struct timeval tv;
	gettimeofday(&tv, NULL);
	return tv.tv_usec / 1000 + (tv.tv_sec & 0xfffff) * 1000;
}

static int timer_end(char *str, int t1)
{
	int t2;
	struct timeval tv;

	gettimeofday(&tv, NULL);
	t2 = tv.tv_usec / 1000 + (tv.tv_sec & 0xfffff) * 1000;

	__android_log_print(ANDROID_LOG_ERROR,"wuwang", "%s take %dms", str, t2 - t1);
	return t2;
}

AsyncExport::AsyncExport(int w, int h)
	: width(w), height(h)
{
	x = y = 0;
	idx = total = 0;
	frameSize = width * height * 4;
	pbo[0] = 0;
}


AsyncExport::~AsyncExport()
{
	destroyPBO();
}


int AsyncExport::createPBO()
{
	glGenBuffers(pbo.size(), &pbo[0]);
	for (GLuint p : pbo)
	{
		glBindBuffer(GL_PIXEL_PACK_BUFFER, p);
		glBufferData(GL_PIXEL_PACK_BUFFER, frameSize, 0, GL_DYNAMIC_READ);
	}
	glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
	return 0;
}


int AsyncExport::destroyPBO()
{
	glDeleteBuffers(pbo.size(), &pbo[0]);
	return 0;
}


int AsyncExport::readPixels(uint8_t *data, int width, int height)
{
	int ret = 0;

#define USE_PBO 1
#if USE_PBO
	if (frameSize == 0 || frameSize != width * height * 4)
	{
		this->width = width;
		this->height = height;
		frameSize = width * height * 4;
	}

	if (pbo[0] == 0)
		createPBO();

	if (total < static_cast<int>(pbo.size()))
	{
		//glReadBuffer(GL_COLOR_ATTACHMENT0);
		glBindBuffer(GL_PIXEL_PACK_BUFFER, pbo[total++]);
		glReadPixels(x, y, width, height, GL_RGBA, GL_UNSIGNED_BYTE, 0);
	}
	else
	{
		//glReadBuffer(GL_COLOR_ATTACHMENT0);
		glBindBuffer(GL_PIXEL_PACK_BUFFER, pbo[idx]);
		int time=timer_start();
		void *ptr = glMapBufferRange(GL_PIXEL_PACK_BUFFER, 0, frameSize, GL_MAP_READ_BIT);
		timer_end("glMap",time);
		if (ptr && data)
		{
            time=timer_start();
			memcpy(data, ptr, frameSize);
            timer_end("memcpy",time);
            time=timer_start();
			glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
            timer_end("glUnmapBuffer",time);

            time=timer_start();
			glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, 0);
            timer_end("glReadPixels",time);
			idx = (idx + 1) % pbo.size();
			ret = frameSize;
		}
	}
	glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
#else

	//glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
	glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, data);
    ret=width*height*4;
#endif
	return ret;
}