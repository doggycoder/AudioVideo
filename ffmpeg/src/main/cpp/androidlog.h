//
// Created by aiya on 2017/3/25.
//

#ifndef AUDIOVIDEO_ANDROIDLOG_H
#define AUDIOVIDEO_ANDROIDLOG_H

#include "android/log.h"

#define LOGD(tag,fmt,...) __android_log_print(ANDROID_LOG_DEBUG,tag, fmt,__VA_ARGS__)



#endif //AUDIOVIDEO_ANDROIDLOG_H
