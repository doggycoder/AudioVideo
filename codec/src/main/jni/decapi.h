#include <string>

class VideoDecode
{
public:
	VideoDecode();
	int open_video(const std::string &name);
	int decode_frame(uint8_t *out);
	int close_video();

	int getWidth() const;
	int getHeight() const;
	int getFormat() const;
	int getLineSize(int *linesize) const;

private:
	void *aydec;
	int linesize[4];
};
