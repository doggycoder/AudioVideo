//
// Created by aiya on 2017/9/6.
//

#include "jni.h"
#include "glexport.h"
#include <assert.h>
#include "string"


#define CODEC_JAVA "com/wuwang/jni/GLExport"

#define KEY_WIDTH 0x0011
#define KEY_HEIGHT 0x0012
#define KEY_FORMAT 0x0013
#define KEY_LINE_SIZE 0x0014

#ifdef __cplusplus
extern "C" {
#endif

jlong coCreate(JNIEnv * env,jclass clazz,jint width,jint height){
    return (jlong) new AsyncExport(width,height);
}

jint coReadPixels(JNIEnv * env,jclass clazz,jlong id,jbyteArray data,jint width,jint height){
    jbyte * d=env->GetByteArrayElements(data,JNI_FALSE);
    jint ret = ((AsyncExport *)id)->readPixels((uint8_t *) d, width, height);
    env->ReleaseByteArrayElements(data,d,JNI_COMMIT);
    return ret;
}

void coRelease(JNIEnv * env,jclass clazz,jlong id){
    delete (AsyncExport *) id;
}


static JNINativeMethod g_methods[]={
        {"_createObject",          "(II)J",                       (void *)coCreate},
        {"_readPixels",             "(J[BII)I",                   (void *)coReadPixels},
        {"_release",              "(J)V",                    (void *)coRelease},
};


JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved){
    JNIEnv* env = nullptr;

    if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
    }
    assert(env != nullptr);
    jclass clazz=env->FindClass(CODEC_JAVA);
    env->RegisterNatives(clazz, g_methods, (int) (sizeof(g_methods) / sizeof((g_methods)[0])));

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *jvm, void *reserved){
    //todo try ayDeInit here
}

#ifdef __cplusplus
}
#endif
