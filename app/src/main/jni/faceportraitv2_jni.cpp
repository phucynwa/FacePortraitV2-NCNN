//
// Created by Phuc YNWA on 02/09/2023.
//

#include <android/asset_manager_jni.h>
#include <android/bitmap.h>
#include <android/log.h>

#include <jni.h>

#include <string>
#include <vector>

// ncnn
#include "gpu.h"
#include "net.h"
#include "benchmark.h"

// opencv
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

static ncnn::UnlockedPoolAllocator g_blob_pool_allocator;
static ncnn::PoolAllocator g_workspace_pool_allocator;

static ncnn::Net face_portrait_v2_net;

void MatToBitmap(JNIEnv *env, cv::Mat &src, jobject bitmap, bool needPremultiplyAlpha);

extern "C" {


JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, "FacePortraitV2", "JNI_OnLoad");
    ncnn::create_gpu_instance();
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, "FacePortraitV2", "JNI_OnUnload");
    ncnn::destroy_gpu_instance();
}

JNIEXPORT jboolean JNICALL
Java_com_phucynwa_faceportraitv2ncnn_FacePortraitV2Converter_init(JNIEnv *env, jobject thiz,
                                                                  jobject assetManager) {
    ncnn::Option opt;
    opt.lightmode = true;
    opt.num_threads = 4;
    opt.blob_allocator = &g_blob_pool_allocator;
    opt.workspace_allocator = &g_workspace_pool_allocator;

    // use vulkan compute
    if (ncnn::get_gpu_count() != 0)
        opt.use_vulkan_compute = true;

    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);

    face_portrait_v2_net.opt = opt;
    int ret0 = face_portrait_v2_net.load_param(mgr, "face_paint_512_v2.param");
    int ret1 = face_portrait_v2_net.load_model(mgr, "face_paint_512_v2.bin");

    __android_log_print(ANDROID_LOG_DEBUG, "FacePortraitV2", "load %d %d", ret0, ret1);

    return JNI_TRUE;
}


JNIEXPORT jboolean JNICALL
Java_com_phucynwa_faceportraitv2ncnn_FacePortraitV2Converter_convert(JNIEnv *env, jobject thiz,
                                                                     jobject bitmap) {
    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, bitmap, &info);
    const int width = info.width;
    const int height = info.height;

    int target_w = 512;
    int target_h = 512;

    const float mean_vals[3] = {127.5f, 127.5f, 127.5f};
    const float norm_vals[3] = {1 / 127.5f, 1 / 127.5f, 1 / 127.5f};
    ncnn::Mat in = ncnn::Mat::from_android_bitmap_resize(env, bitmap, ncnn::Mat::PIXEL_RGB, target_w, target_h);
    in.substract_mean_normalize(mean_vals, norm_vals);
    ncnn::Mat out;
    {
        ncnn::Extractor ex = face_portrait_v2_net.create_extractor();
        ex.set_vulkan_compute(true);
        ex.input("in0", in);
        ex.extract("out0", out);
    }

    __android_log_print(ANDROID_LOG_DEBUG, "FacePortraitV2","w%d x h%d", out.w, out.h);

    cv::Mat result(out.h, out.w, CV_32FC3);
    for (int c = 0; c < out.c; c++) {
        float *out_data = out.channel(c);
        for (int row = 0; row < out.h; row++) {
            for (int col = 0; col < out.w; col++) {
                result.at<cv::Vec3f>(row, col)[c] = out_data[row * out.h + col];
            }
        }
    }
    cv::Mat result8U(out.h, out.w, CV_8UC3);
    result.convertTo(result8U, CV_8UC3, 127.5, 127.5);
    cv::Mat dst(height, width, result8U.type());
    resize(result8U, dst, dst.size(), 0, 0, cv::INTER_CUBIC);
    MatToBitmap(env, dst, bitmap, false);
    return JNI_TRUE;
}

}

void MatToBitmap(JNIEnv *env, cv::Mat &src, jobject bitmap, bool needPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
        if (src.type() == CV_8UC1) {
            __android_log_print(ANDROID_LOG_DEBUG, "FacePortraitV2",
                                "MatToBitmap: CV_8UC1 -> RGBA_8888");
            cvtColor(src, tmp, cv::COLOR_GRAY2RGBA);
        } else if (src.type() == CV_8UC3) {
            __android_log_print(ANDROID_LOG_DEBUG, "FacePortraitV2",
                                "MatToBitmap: CV_8UC3 -> RGBA_8888");
            cvtColor(src, tmp, cv::COLOR_RGB2RGBA);
        } else if (src.type() == CV_8UC4) {
            __android_log_print(ANDROID_LOG_DEBUG, "FacePortraitV2",
                                "MatToBitmap: CV_8UC4 -> RGBA_8888");
            if (needPremultiplyAlpha) cvtColor(src, tmp, cv::COLOR_RGBA2mRGBA);
            else src.copyTo(tmp);
        }
    } else {
        // info.format == ANDROID_BITMAP_FORMAT_RGB_565
        cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
        if (src.type() == CV_8UC1) {
            __android_log_print(ANDROID_LOG_DEBUG, "FacePortraitV2",
                                "MatToBitmap: CV_8UC1 -> RGB_565");
            cvtColor(src, tmp, cv::COLOR_GRAY2BGR565);
        } else if (src.type() == CV_8UC3) {
            __android_log_print(ANDROID_LOG_DEBUG, "FacePortraitV2",
                                "MatToBitmap: CV_8UC3 -> RGB_565");
            cvtColor(src, tmp, cv::COLOR_RGB2BGR565);
        } else if (src.type() == CV_8UC4) {
            __android_log_print(ANDROID_LOG_DEBUG, "FacePortraitV2",
                                "MatToBitmap: CV_8UC4 -> RGB_565");
            cvtColor(src, tmp, cv::COLOR_RGBA2BGR565);
        }
    }
    AndroidBitmap_unlockPixels(env, bitmap);
    return;
}
