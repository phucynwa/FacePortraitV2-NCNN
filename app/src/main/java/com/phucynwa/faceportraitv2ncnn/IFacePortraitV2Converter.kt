package com.phucynwa.faceportraitv2ncnn

import android.content.res.AssetManager
import android.graphics.Bitmap

interface IFacePortraitV2Converter {

    fun init(mgr: AssetManager): Boolean
    fun convert(bitmap: Bitmap): Boolean
}
