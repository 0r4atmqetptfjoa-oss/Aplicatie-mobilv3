package com.example.educationalapp.alphabet

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads a scaled bitmap from resources on a background thread.
 */
suspend fun loadScaledBitmapAsync(context: android.content.Context, resId: Int, maxDim: Int = 2048): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(context.resources, resId, options)
            val width = options.outWidth
            val height = options.outHeight
            if (width <= 0 || height <= 0) return@withContext null

            var inSample = 1
            while (width / inSample > maxDim || height / inSample > maxDim) {
                inSample *= 2
            }

            val opts2 = BitmapFactory.Options().apply { inSampleSize = inSample }
            BitmapFactory.decodeResource(context.resources, resId, opts2)
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }
}

/**
 * Asynchronously loads and remembers a scaled ImageBitmap to prevent UI jank.
 */
@Composable
fun rememberScaledImageBitmap(resId: Int, maxDim: Int = 2048): ImageBitmap? {
    val ctx = LocalContext.current
    var bitmap by remember(resId, maxDim) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(resId, maxDim) {
        val bmp = loadScaledBitmapAsync(ctx, resId, maxDim)
        bitmap = bmp?.asImageBitmap()
    }
    
    return bitmap
}
