//
// Created by aiya on 2017/4/5.
//

#ifndef AUDIOVIDEO_FFMPEGLOG_H
#define AUDIOVIDEO_FFMPEGLOG_H

#ifdef __cplusplus
extern "C"{
#endif

#include "android/log.h"
#include "libavutil/log.h"

#define FFMPEG_TAG "FFMPEG_LOG_"

#define LOG(level,TAG,...) ((void)__android_log_print(level, TAG, __VA_ARGS__))
#define LOGD(tag,...) __android_log_print(ANDROID_LOG_DEBUG,tag, __VA_ARGS__)
#define LOGE(tag,...) __android_log_print(ANDROID_LOG_ERROR,tag, __VA_ARGS__)
#define LOGI(tag,...) __android_log_print(ANDROID_LOG_INFO,tag, __VA_ARGS__)

static void ffmpeg_log(void *ptr, int level, const char *fmt, va_list vl) {
    int ffplv;
    switch (level){
        case AV_LOG_ERROR:
            ffplv = ANDROID_LOG_ERROR;
            break;
        case AV_LOG_WARNING:
            ffplv = ANDROID_LOG_WARN;
            break;
        case AV_LOG_INFO:
            ffplv = ANDROID_LOG_INFO;
            break;
        case AV_LOG_VERBOSE:
            ffplv=ANDROID_LOG_VERBOSE;
        default:
            ffplv = ANDROID_LOG_DEBUG;
            break;
    }
    va_list vl2;
    char line[1024];
    static int print_prefix = 1;
    va_copy(vl2, vl);
    av_log_format_line(ptr, level, fmt, vl2, line, sizeof(line), &print_prefix);
    va_end(vl2);
    LOG(ffplv, FFMPEG_TAG, "%s", line);
}

#ifdef __cplusplus
};
#endif
#endif //AUDIOVIDEO_FFMPEGLOG_H
