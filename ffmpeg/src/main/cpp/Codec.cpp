//
// Created by aiya on 2017/4/5.
//

#include "Codec.h"

char * msg= new char[64];

char * Codec::getInfo(int key) {
    return (char *) avcodec_configuration();
}

void Codec::init() {
    av_register_all();
    av_log(NULL,AV_LOG_DEBUG,"register success");
}

void Codec::release() {

}

void Codec::log(int ret, const char * func) {
    av_strerror(ret,msg,64);
    av_log(NULL,AV_LOG_ERROR,"%s error: %d,%s",func,ret,msg);
}

void Codec::setCachePath(const char *path) {
    this->cachePath=path;
}

char * Codec::file(const char * name){
    char * file= (char *) cachePath;
    strcat(file,name);
    return file;
}

void Codec::set(int key, int value) {

}

int Codec::get(int key) {
    return 0;
}