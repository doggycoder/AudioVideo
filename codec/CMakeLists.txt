cmake_minimum_required(VERSION 3.4.1)
set(lib-path ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI})

aux_source_directory(src/main/jni SRC_FILE)
add_library(VideoConvert SHARED ${SRC_FILE})
find_library(log-lib log)
find_library(gl-lib GLESv3)
target_link_libraries(VideoConvert ${log-lib} ${gl-lib} ${lib-path}/libdec.so)

