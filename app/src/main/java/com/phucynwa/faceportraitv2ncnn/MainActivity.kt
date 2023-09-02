package com.phucynwa.faceportraitv2ncnn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.phucynwa.faceportraitv2ncnn.ui.component.FacePortraitV2Screen
import com.phucynwa.faceportraitv2ncnn.ui.theme.FacePotraitV2NCNNTheme

class MainActivity : ComponentActivity() {

    private var facePortraitV2Converter: FacePortraitV2Converter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FacePotraitV2NCNNTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FacePortraitV2Screen(
                        convert = {
                            facePortraitV2Converter?.convert(it)
                            it
                        }
                    )
                }
            }
        }
        facePortraitV2Converter = FacePortraitV2Converter()
        facePortraitV2Converter?.init(assets)
    }

    override fun onDestroy() {
        super.onDestroy()
        facePortraitV2Converter = null
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FacePotraitV2NCNNTheme {
        Greeting("Android")
    }
}
