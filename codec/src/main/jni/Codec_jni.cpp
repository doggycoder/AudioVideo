//
// Created by aiya on 2017/8/29.
//
#include "jni.h"
#include "decapi.h"
#include <assert.h>
#include "string"


#define CODEC_JAVA "edu/wuwang/codec/NativeCodec"

#define KEY_WIDTH 0x0011
#define KEY_HEIGHT 0x0012
#define KEY_FORMAT 0x0013
#define KEY_LINE_SIZE 0x0014

#ifdef __cplusplus
extern "C" {
#endif

//jlong coCreate(JNIEnv * env,jclass clazz){
//    return (jlong) new VideoDecode();
//}
//
//jint coOpenVideo(JNIEnv * env,jclass obj,jlong id,jstring path){
//    char * mp= (char *) env->GetStringUTFChars(path, JNI_FALSE);
//    return ((VideoDecode *)id)->open_video(std::string(mp));
//}
//
//jint coDecode(JNIEnv * env,jclass obj,jlong id,jbyteArray array){
//    jbyte * data=  env->GetByteArrayElements(array, JNI_FALSE);
//    int ret= ((VideoDecode *)id)->decode_frame((uint8_t *) data);
//    env->ReleaseByteArrayElements(array,  data, JNI_COMMIT);
//    return ret;
//}
//
//jint coCloseVideo(JNIEnv * env,jclass obj,jlong id){
//    return ((VideoDecode *)id)->close_video();
//}
//
//void coRelease(JNIEnv * env,jclass obj,jlong id){
//    delete (VideoDecode *)id;
//}
//
//jint coGet(JNIEnv * env,jclass obj,jlong id,jint key){
//    VideoDecode * decode= (VideoDecode *) id;
//    switch (key){
//        case KEY_WIDTH:
//            return decode->getWidth();
//        case KEY_HEIGHT:
//            return decode->getHeight();
//        case KEY_FORMAT:
//            return decode->getFormat();
//        default:
//            return 0;
//    }
//}
//
//jint coGetArray(JNIEnv * env,jclass clazz,jlong id,jint key,jintArray value){
//    VideoDecode * decode= (VideoDecode *) id;
//    jint * d=env->GetIntArrayElements(value,JNI_FALSE);
//    int ret=0;
//    switch (key){
//        case KEY_LINE_SIZE:
//            ret=decode->getLineSize(d);
//            break;
//        case KEY_HEIGHT:
//            break;
//        default:
//            ret=0;
//            break;
//    }
//    env->ReleaseIntArrayElements(value,d,JNI_COMMIT);
//    return ret;
//}
//
//
//static JNINativeMethod g_methods[]={
//        {"_createObject",          "()J",                       (void *)coCreate},
//        {"_openVideo",             "(JLjava/lang/String;)I",    (void *)coOpenVideo},
//        {"_decode",              "(J[B)I",                    (void *)coDecode},
//        {"_closeVideo",           "(J)I",                        (void *)coCloseVideo},
//        {"_release",                 "(J)I",                      (void *)coRelease},
//        {"_getIntValue",                 "(JI)I",                      (void *)coGet},
//        {"_getIntValue",                 "(JI[I)I",                      (void *)coGetArray},
//};
//
//
//JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved){
//    JNIEnv* env = nullptr;
//
//    if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
//        return JNI_ERR;
//    }
//    assert(env != nullptr);
//    jclass clazz=env->FindClass(CODEC_JAVA);
//    env->RegisterNatives(clazz, g_methods, (int) (sizeof(g_methods) / sizeof((g_methods)[0])));
//
//    return JNI_VERSION_1_4;
//}
//
//JNIEXPORT void JNI_OnUnload(JavaVM *jvm, void *reserved){
//    //todo try ayDeInit here
//}

#ifdef __cplusplus
}
#endif
