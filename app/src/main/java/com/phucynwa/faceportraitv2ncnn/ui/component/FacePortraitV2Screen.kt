package com.phucynwa.faceportraitv2ncnn.ui.component

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.phucynwa.faceportraitv2ncnn.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FacePortraitV2Screen(
    modifier: Modifier = Modifier,
    convert: (Bitmap) -> Bitmap? = { it }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val (imageUri, setImageUri) = remember {
        mutableStateOf<Uri?>(null)
    }
    val (outputBitmap, setOutputBitmap) = remember {
        mutableStateOf<Bitmap?>(null)
    }
    val pickImageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(), setImageUri)
    Column(
        modifier = modifier
    ) {
        Row {
            Button(
                onClick = {
                    val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    pickImageLauncher.launch(request)
                }
            ) {
                Text(text = "Select Image")
            }
            Button(
                onClick = {
                    imageUri ?: return@Button
                    coroutineScope.launch(Dispatchers.Default) {
                        val bitmap = Utils.decodeUri(context, selectedImage = imageUri)
                        if (bitmap != null) {
                            val bitmap = convert(bitmap)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Success !!!", Toast.LENGTH_SHORT).show()
                                setOutputBitmap(bitmap)
                            }
                        }
                    }
                }
            ) {
                Text(text = "Convert To Anime")
            }
        }
        Column(modifier = Modifier.weight(1F)) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                )
                if (outputBitmap != null) {
                    AsyncImage(
                        model = outputBitmap,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

