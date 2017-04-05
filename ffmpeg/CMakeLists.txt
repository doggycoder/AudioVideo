# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.
cmake_minimum_required(VERSION 3.4)

set(LIBDIR ${CMAKE_SOURCE_DIR}/libs/armeabi)

set(CMAKE_C_FLAGS -std=c++11)
aux_source_directory(./src/main/cpp DIR_SRCS)
include_directories(./include)
add_library(FFMpeg SHARED ${DIR_SRCS})
target_link_libraries(FFMpeg
                ${LIBDIR}/libavformat.a
                ${LIBDIR}/libavcodec.a
                #libx264.a
                ${LIBDIR}/libavdevice.a
                ${LIBDIR}/libavfilter.a
                ${LIBDIR}/libswscale.a
                ${LIBDIR}/libswresample.a
                ${LIBDIR}/libpostproc.a
                ${LIBDIR}/libavutil.a
                 log z m)
