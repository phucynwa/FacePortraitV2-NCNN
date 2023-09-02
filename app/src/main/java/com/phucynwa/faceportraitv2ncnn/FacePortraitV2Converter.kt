package com.phucynwa.faceportraitv2ncnn

import android.content.res.AssetManager
import android.graphics.Bitmap

class FacePortraitV2Converter : IFacePortraitV2Converter {

    init {
        System.loadLibrary("faceportraitv2")
    }

    external override fun init(mgr: AssetManager): Boolean
    external override fun convert(bitmap: Bitmap): Boolean
}
