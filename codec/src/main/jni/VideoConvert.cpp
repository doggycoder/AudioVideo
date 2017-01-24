//#include "VideoConvert.h"
#include "jni.h"

#define max(x,y)  (x>y?x:y)
#define min(x,y)  (x<y?x:y)
#define y(r,g,b)  (((66 * r + 129 * g + 25 * b + 128) >> 8) + 16)
#define u(r,g,b)  (((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128)
#define v(r,g,b)  (((112 * r - 94 * g - 18 * b + 128) >> 8) + 128)
#define color(x)  ((unsigned char)((x < 0) ? 0 : ((x > 255) ? 255 : x)))

#define RGBA_YUV420SP   0x00004012
#define BGRA_YUV420SP   0x00004210
#define RGBA_YUV420P    0x00014012
#define BGRA_YUV420P    0x00014210
#define RGB_YUV420SP    0x00003012
#define RGB_YUV420P     0x00013012
#define BGR_YUV420SP    0x00003210
#define BGR_YUV420P     0x00013210

/**
*   type 0-3位表示b的偏移量
*        4-7位表示g的偏移量
*        8-11位表示r的偏移量
*        12-15位表示rgba一个像素所占的byte
*        16-19位表示yuv的类型，0为420sp，1为420p
*/

void rgbaToYuv(int width,int height,unsigned char * rgb,unsigned char * yuv,int type){
    const int frameSize = width * height;
    const int yuvType=(type&0x10000)>>16;
    const int byteRgba=(type&0x0F000)>>12;
    const int rShift=(type&0x00F00)>>8;
    const int gShift=(type&0x000F0)>>4;
    const int bShift= (type&0x0000F);
    const int uIndex=0;
    const int vIndex=yuvType; //yuvType为1表示YUV420p,为0表示420sp

    int yIndex = 0;
    int uvIndex[2]={frameSize,frameSize+frameSize/4};

    unsigned char R, G, B, Y, U, V;
    unsigned int index = 0;
    for (int j = 0; j < height; j++) {
 	   for (int i = 0; i < width; i++) {
 		   index = j * width + i;

 		   R = rgb[index*byteRgba+rShift]&0xFF;
 		   G = rgb[index*byteRgba+gShift]&0xFF;
 		   B = rgb[index*byteRgba+bShift]&0xFF;

 		   Y = y(R,G,B);
 		   U = u(R,G,B);
 		   V = v(R,G,B);

 		   yuv[yIndex++] = color(Y);
 		   if (j % 2 == 0 && index % 2 == 0) {
 			   yuv[uvIndex[uIndex]++] =color(U);
 			   yuv[uvIndex[vIndex]++] =color(V);
 		   }
 	   }
    }
}


/*
 * Class:     com_aiya_jni_DataConvert
 * Method:    rgbaToYuv420p
 * Signature: ([BII[B)V
 */
extern "C" {

    void Java_com_wuwang_jni_DataConvert_rgbaToYuv
      (JNIEnv * env, jobject obj, jbyteArray rgba, jint width, jint height,
      jbyteArray yuv,jint type){
        jbyte * rgbaBuffer = env->GetByteArrayElements(rgba,0);
        unsigned char * cRgba=(unsigned char *)rgbaBuffer;
        jbyte* yuvBuffer = env->GetByteArrayElements(yuv,0);
        unsigned char * cYuv=(unsigned char *)yuvBuffer;
        rgbaToYuv(width,height,cRgba,cYuv,type);
        env->ReleaseByteArrayElements(rgba, rgbaBuffer, 0);
        env->ReleaseByteArrayElements(yuv, yuvBuffer, 0);
    }

}

