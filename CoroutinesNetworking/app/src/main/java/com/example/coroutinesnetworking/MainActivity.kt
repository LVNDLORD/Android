package com.example.coroutinesnetworking

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.coroutinesnetworking.ui.theme.CoroutinesNetworkingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val imageUrl = "https://plaky.com/blog/wp-content/uploads/2023/08/Intro.jpg"
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUrl) {
        val bitmap = downloadImage(imageUrl)
        imageBitmap.value = bitmap
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        imageBitmap.value?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(300.dp))
        } ?: Text("Loading image...")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp()
}


suspend fun downloadImage(url: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = java.net.URL(url).openStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}