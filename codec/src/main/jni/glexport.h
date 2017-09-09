#ifndef GLEXPORT_H
#define GLEXPORT_H

#include <array>
#include <GLES3/gl3.h>

const int pbosize = 4;

class AsyncExport
{
public:
	AsyncExport(int w, int h);
	~AsyncExport();

	int readPixels(uint8_t *data, int width, int height);

private:
	int createPBO();
	int destroyPBO();

	int x;
	int y;
	int width;
	int height;
	int frameSize;

	int idx;
	int total;
	std::array<GLuint, pbosize> pbo;
};

#endif
