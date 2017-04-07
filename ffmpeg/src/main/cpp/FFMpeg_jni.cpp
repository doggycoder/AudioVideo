#ifdef __cplusplus
extern "C" {
#endif


#include "AACDecoder.h"
#include "jni.h"
#include "FFMpegLog.h"

Codec * codec;

jstring Java_edu_wuwang_ffmpeg_FFMpeg_getInfo(JNIEnv * env, jclass obj){
    return env->NewStringUTF(Codec::getInfo(0));
}


void Java_edu_wuwang_ffmpeg_FFMpeg_init(JNIEnv * env, jclass obj){
    av_log_set_callback(ffmpeg_log);
    Codec::init();
}

jint Java_edu_wuwang_ffmpeg_FFMpeg_start(JNIEnv * env, jobject obj){
    codec=new AACDecoder();
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

void Java_edu_wuwang_ffmpeg_FFMpeg_set(JNIEnv * env, jobject obj, jint key, jint value){

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

