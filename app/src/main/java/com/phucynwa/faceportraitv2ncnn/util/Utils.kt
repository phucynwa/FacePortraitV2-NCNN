package com.phucynwa.faceportraitv2ncnn.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.util.Log
import java.io.FileNotFoundException

object Utils {

    @Throws(FileNotFoundException::class)
    fun decodeUri(context: Context, selectedImage: Uri): Bitmap? {
        // Decode image size
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(context.contentResolver.openInputStream(selectedImage), null, o)

        // The new size we want to scale to
        val REQUIRED_SIZE = 400

        // Find the correct scale value. It should be the power of 2.
        var width_tmp = o.outWidth
        var height_tmp = o.outHeight
        var scale = 1
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                || height_tmp / 2 < REQUIRED_SIZE
            ) {
                break
            }
            width_tmp /= 2
            height_tmp /= 2
            scale *= 2
        }

        // Decode with inSampleSize
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        val bitmap = BitmapFactory.decodeStream(
            context.contentResolver.openInputStream(selectedImage),
            null,
            o2
        )

        // Rotate according to EXIF
        var rotate = 0
        try {
            val exif = ExifInterface(context.contentResolver.openInputStream(selectedImage)!!)
            val orientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }
        } catch (e: Throwable) {
            Log.e("MainActivity", e.stackTraceToString())
        }
        val matrix = Matrix()
        matrix.postRotate(rotate.toFloat())
        return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
