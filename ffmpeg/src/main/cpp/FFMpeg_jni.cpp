#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "jni.h"
#include "androidlog.h"

#define TAG "FFMPEG"



AVFormatContext * avFormatContext;
AVOutputFormat * outputFormat;
AVIOContext * avioContext;

void init(){
    av_register_all();
}

void setoutput(char * filename){
    avformat_alloc_output_context2(&avFormatContext,outputFormat,0,filename);
}

void start(){
//    avio_open2(&avioContext,)
}

jstring Java_edu_wuwang_ffmpeg_FFMpeg_getConfiguration(JNIEnv * env, jobject obj){
    return env->NewStringUTF(avcodec_configuration());
}


void Java_edu_wuwang_ffmpeg_FFMpeg_init(JNIEnv * env, jobject obj){
    init();
}

void Java_edu_wuwang_ffmpeg_FFMpeg_setOutput
        (JNIEnv * env, jobject obj, jstring data){
    const jchar * file= env->GetStringChars(data, JNI_FALSE);
    setoutput((char *) file);
    env->ReleaseStringChars(data, file);
}


void Java_edu_wuwang_ffmpeg_FFMpeg_start
        (JNIEnv * env, jobject obj){

}

void Java_edu_wuwang_ffmpeg_FFMpeg_writeFrame
        (JNIEnv * env, jobject obj, jbyteArray data){

}
void Java_edu_wuwang_ffmpeg_FFMpeg_set
        (JNIEnv * env, jobject obj, jint key, jint value){

}
void Java_edu_wuwang_ffmpeg_FFMpeg_release
        (JNIEnv * env, jobject obj){

}


