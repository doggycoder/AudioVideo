#ifndef DLOG_H
#define DLOG_H

enum levels_tag {
  LOG_UNKNOWN = 0,
  LOG_DEFAULT,
  LOG_VERBOSE,
  LOG_DEBUG,
  LOG_INFO,
  LOG_WARN,
  LOG_ERROR,
  LOG_FATAL,
  LOG_SILENT,
  LOG_LAST
};

/* ANDRIOD IMPLEMENTATION */
#if defined(ANDROID)

#include <stdlib.h>
#include <sys/system_properties.h>
#include <android/log.h>
#if defined(LOG_TAG)
#undef LOG_TAG
#endif
#define LOG_TAG "AYEFFECTS"


typedef enum levels_tag levels_e;

static levels_e priority_levels[LOG_LAST] = {LOG_UNKNOWN,
                                             LOG_DEFAULT,
                                             LOG_VERBOSE,
                                             LOG_DEBUG,
                                             LOG_INFO,
                                             LOG_WARN,
                                             LOG_ERROR,
                                             LOG_FATAL,
                                             LOG_SILENT
                                            };
#ifdef HAVE_SYS_PROPERTY

#define LOGGING(PRIORITY, fmt, ...)                                                                     \
        do {                                                                                            \
                char buf[64];                                                                           \
                __system_property_get("ay.effects.debug", buf);                                         \
                if (1 || !strcmp(buf, "1") || !strcmp(buf, "true") || PRIORITY >= LOG_ERROR)                 \
                        __android_log_print(priority_levels[PRIORITY], LOG_TAG, fmt, ##__VA_ARGS__);    \
        } while(0)

#else

#define LOGGING(PRIORITY, fmt, ...) __android_log_print(priority_levels[PRIORITY], LOG_TAG, fmt, ##__VA_ARGS__)

#endif


#else  // IOS

static char *priority_levels[] = {(char*)"UNKNOWN",
                                  (char*)"DEFAULT",
                                  (char*)"VERBOSE",
                                  (char*)"DEBUG",
                                  (char*)"INFO",
                                  (char*)"WARN",
                                  (char*)"ERROR",
                                  (char*)"FATAL",
                                  (char*)"SILENT",
                                  0
                                 };


#ifdef DEBUG

#define LOGGING(PRIORITY, fmt, ...)                                                                       \
    do {                                                                                                  \
        char *priority = priority_levels[PRIORITY];                                                       \
        printf("%s/%s:%d [%s] " fmt " \n", __FILE__, __func__, __LINE__, priority, ##__VA_ARGS__);        \
    } while(0)

#else

#define LOGGING(PRIORITY, fmt, ...)                                                                       \
    do {                                                                                                  \
        if (PRIORITY > LOG_INFO) {                                                                        \
                char *priority = priority_levels[PRIORITY];                                               \
                printf("%s/%s:%d [%s] " fmt " \n", __FILE__, __func__, __LINE__, priority, ##__VA_ARGS__);\
        }                                                                                                 \
    } while(0)

#endif

#endif // #if defined(ANDROID)


#define ALOG(PRIORITY, fmt, ...) LOGGING(PRIORITY, fmt, ##__VA_ARGS__)

#define MALLOC_CHECK(ptr_type, ptr, size)                                                       \
    {                                                                                           \
        ptr = (ptr_type) malloc(size);                                                          \
        if (ptr == NULL)                                                                        \
        {                                                                                       \
            ALOG(LOG_ERROR, "Memory allocation error FILE: %s LINE: %i\n", __FILE__, __LINE__); \
        }                                                                                       \
    }

#define REALLOC_CHECK(ptr_type, ptr, size)                                                      \
    {                                                                                           \
        ptr = (ptr_type) realloc(ptr, size);                                                    \
        if (ptr == NULL)                                                                        \
        {                                                                                       \
            ALOG(LOG_ERROR, "Memory allocation error FILE: %s LINE: %i\n", __FILE__, __LINE__); \
        }                                                                                       \
    }

#define FREE_CHECK(ptr)     \
    {                       \
        free((void*) ptr);  \
        ptr = NULL;         \
    }


#define GL_CHECK(x)                                                                                            \
        x;                                                                                                     \
        {                                                                                                      \
            GLenum glError = glGetError();                                                                     \
            if(glError != GL_NO_ERROR)                                                                         \
            {                                                                                                  \
                ALOG(LOG_ERROR, "glGetError() = %i (%#.8x) at %s:%i\n", glError, glError, __FILE__, __LINE__); \
            }                                                                                                  \
        }

#endif  //DLOG_H
