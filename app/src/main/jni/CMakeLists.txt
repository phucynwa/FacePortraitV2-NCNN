project(faceportraitv2)

cmake_minimum_required(VERSION 3.4.1)

set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv-mobile-4.8.0-android/sdk/native/jni)
find_package(OpenCV REQUIRED core imgproc highgui)

set(ncnn_DIR ${CMAKE_SOURCE_DIR}/ncnn-20230816-android-vulkan/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)

add_library(faceportraitv2 SHARED faceportraitv2_jni.cpp)

target_link_libraries(faceportraitv2 ncnn ${OpenCV_LIBS})
