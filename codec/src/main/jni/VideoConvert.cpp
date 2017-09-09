//#include "VideoConvert.h"
#include "jni.h"

#define max(x, y)  (x>y?x:y)
#define min(x, y)  (x<y?x:y)
//#define y(r,g,b)  ((66 * r + 129 * g + 25 * b + 0x1080) >> 8)
//#define u(r,g,b)  ((-38 * r - 74 * g + 112 * b + 0x8080) >> 8)
//#define v(r,g,b)  ((112 * r - 94 * g - 18 * b + 0x8080) >> 8)
#define uR(Y,U,V) (((Y<<8)+((V<<8)+(V<<5)+(V<<2)))>>8)
#define uG(Y,U,V) (((Y<<8)-((U<<6)+(U<<5)+(U<<2))-((V<<7)+(V<<4)+(V<<2)+V))>>8)
#define uB(Y,U,V) (((Y<<8)+(U<<9)+(U<<3))>>8)

#define uint8 unsigned char

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

static uint8 __inline y(uint8 r,uint8 g,uint8 b){
    return (uint8)((66 * r + 129 * g + 25 * b + 0x1080) >> 8);
}
static uint8 __inline u(uint8 r,uint8 g,uint8 b){
    return (uint8)((-38 * r - 74 * g + 112 * b + 0x8080) >> 8);
}
static unsigned char __inline v(uint8 r,uint8 g,uint8 b){
    return (uint8)((112 * r - 94 * g - 18 * b + 0x8080) >> 8);
}

//yuv转RGB未验证

static uint8 __inline r(uint8 y,uint8 u,uint8 v){
    return (uint8)((296*y+411*v+0xE000)>>8);
}

static uint8 __inline g(uint8 y,uint8 u,uint8 v){
    return (uint8)((299*y-101*u-211*v+0x87b3)>>8);
}

static uint8 __inline b(uint8 y,uint8 u,uint8 v){
    return (uint8)((299*y+519U-0x115CD)>>8);
}

void rgbaToYuv(int width, int height, unsigned char *rgb, unsigned char *yuv, int type) {
    const int frameSize = width * height;
    const int yuvType = (type & 0x10000) >> 16;
    const int byteRgba = (type & 0x0F000) >> 12;
    const int rShift = (type & 0x00F00) >> 8;
    const int gShift = (type & 0x000F0) >> 4;
    const int bShift = (type & 0x0000F);
    const int uIndex = 0;
    const int vIndex = yuvType; //yuvType为1表示YUV420p,为0表示420sp

    int yIndex = 0;
    int uvIndex[2] = {frameSize, frameSize + frameSize / 4};

    unsigned char R, G, B, Y, U, V;
    int index = 0;
    for (int j = 0; j < height; j++) {
        for (int i = 0; i < width; i++) {
            index = j * width + i;

            R = rgb[index * byteRgba + rShift];
            G = rgb[index * byteRgba + gShift] ;
            B = rgb[index * byteRgba + bShift];

            Y = y(R, G, B);
            U = u(R, G, B);
            V = v(R, G, B);

            yuv[yIndex++] = Y;
            if (j % 2 == 0 && index % 2 == 0) {
                yuv[uvIndex[uIndex]++] = U;
                yuv[uvIndex[vIndex]++] = V;
            }
        }
    }
}

/**
*   type 0-3位表示b的偏移量
*        4-7位表示g的偏移量
*        8-11位表示r的偏移量
*        12-15位表示rgba一个像素所占的byte
*        16-19位表示yuv的类型，0为420sp，1为420p
*/

void yuvToRgba(int width, int height, unsigned char *rgb, unsigned char *yuv, int type) {
    const int frameSize = width * height;
    const int yuvType = (type & 0x100000) >> 24;
    const int byteRgba = (type & 0xF0000) >> 16;
    const int aShift = (type & 0x0F000) >>12;
    const int rShift = (type & 0x00F00) >> 8;
    const int gShift = (type & 0x000F0) >> 4;
    const int bShift = (type & 0x0000F);
    const int uIndex = 0;
    const int vIndex = yuvType; //yuvType为1表示YUV420p,为0表示420sp

    int uvIndex[2] = {frameSize, frameSize + frameSize / 4};

    unsigned char R, G, B, Y, U=0, V=0;
    int index = 0;
    for (int j = 0; j < height; j++) {
        for (int i = 0; i < width; i++) {
            index = j * width + i;
            Y=yuv[index];
            if (j % 2 == 0 && index % 2 == 0) {
                U=yuv[uvIndex[uIndex]++];
                V=yuv[uvIndex[vIndex]++];
            }
            R = r(Y,U,V);
            G = g(Y,U,V);
            B = b(Y,U,V);

            rgb[index*byteRgba+rShift]=R;
            rgb[index*byteRgba+gShift]=G;
            rgb[index*byteRgba+bShift]=B;
            if(byteRgba==4){
                rgb[index*byteRgba+aShift]=1;
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
        (JNIEnv *env, jobject obj, jbyteArray rgba, jint width, jint height,
         jbyteArray yuv, jint type) {
    jbyte *rgbaBuffer = env->GetByteArrayElements(rgba, 0);
    unsigned char *cRgba = (unsigned char *) rgbaBuffer;
    jbyte *yuvBuffer = env->GetByteArrayElements(yuv, 0);
    unsigned char *cYuv = (unsigned char *) yuvBuffer;
    rgbaToYuv(width, height, cRgba, cYuv, type);
    env->ReleaseByteArrayElements(rgba, rgbaBuffer, 0);
    env->ReleaseByteArrayElements(yuv, yuvBuffer, 0);
}

void Java_com_wuwang_jni_DataConvert_yuvToRgba
        (JNIEnv *env, jobject obj, jbyteArray yuv, jint width, jint height,
         jbyteArray rgba,jint type) {
    jbyte *rgbaBuffer = env->GetByteArrayElements(rgba, 0);
    unsigned char *cRgba = (unsigned char *) rgbaBuffer;
    jbyte *yuvBuffer = env->GetByteArrayElements(yuv, 0);
    unsigned char *cYuv = (unsigned char *) yuvBuffer;
    yuvToRgba(width, height, cRgba, cYuv, type);
    env->ReleaseByteArrayElements(rgba, rgbaBuffer, 0);
    env->ReleaseByteArrayElements(yuv, yuvBuffer, 0);
}

}

