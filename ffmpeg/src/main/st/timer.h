#ifndef TIMER_H
#define TIMER_H


#if defined(ANDROID)
#define TEXPORT __attribute ((visibility("default")))
#else
#define TEXPORT
#endif

#ifdef __cplusplus
extern "C"
{
#endif


TEXPORT int timer_start();

TEXPORT int timer_end(char *str, int t1);


#ifdef __cplusplus
}
#endif

#endif