#ifdef __cplusplus
extern "C" {
#endif


#include "jni.h"
#include "FFMpegLog.h"
#include "AACDecoder.h"
#include "H264Decoder.h"
#include "Mp4Decoder.h"

#define KEY_STR_CACHE_PATH 0x8001
#define DECODER_H264 0xf001
#define DECODER_AAC 0xf002
#define DECODER_MP4 0xf003

const char * cachePath;

Codec * codec;

jstring Java_edu_wuwang_ffmpeg_FFMpeg_getInfo(JNIEnv * env, jclass obj){
    return env->NewStringUTF(Codec::getInfo(0));
}


void Java_edu_wuwang_ffmpeg_FFMpeg_init(JNIEnv * env, jclass obj){
    av_log_set_callback(ffmpeg_log);
    Codec::init();
}

jint Java_edu_wuwang_ffmpeg_FFMpeg_start(JNIEnv * env, jobject obj,jint type){
    switch (type){
        case DECODER_H264:
            codec=new H264Decoder();
            break;
        case DECODER_AAC:
            codec=new AACDecoder();
            break;
        case DECODER_MP4:
            codec=new Mp4Decoder();
        default:
            break;
    }
    codec->setCachePath(cachePath);
    return codec->start();
}

jint Java_edu_wuwang_ffmpeg_FFMpeg_input(JNIEnv * env, jobject obj, jbyteArray data){
    return codec->input((uint8_t *) env->GetByteArrayElements(data, JNI_FALSE));
}

jint Java_edu_wuwang_ffmpeg_FFMpeg_output(JNIEnv * env, jobject obj, jbyteArray data){
    return codec->output((uint8_t *) env->GetByteArrayElements(data, JNI_FALSE));
}

jint Java_edu_wuwang_ffmpeg_FFMpeg_stop(JNIEnv * env, jobject obj){
    return codec->stop();
}

void Java_edu_wuwang_ffmpeg_FFMpeg_setInt(JNIEnv * env, jclass obj, jint key, jint value){

}

void Java_edu_wuwang_ffmpeg_FFMpeg_setStr(JNIEnv * env, jclass obj, jint key, jstring value){
    switch (key){
        case KEY_STR_CACHE_PATH:
            cachePath=env->GetStringUTFChars(value, (jboolean *) JNI_FALSE);
            break;
        default:
            break;
    }
}

int Java_edu_wuwang_ffmpeg_FFMpeg_get(JNIEnv * env, jobject obj, jint key){
    return codec->get(key);
}

void Java_edu_wuwang_ffmpeg_FFMpeg_release(JNIEnv * env, jclass obj){
    Codec::release();
}


#ifdef __cplusplus
}
#endif

