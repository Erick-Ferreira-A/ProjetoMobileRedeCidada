package com.example.projetorevisao.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.data.remote.BackendHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Cache em memória das fotos já baixadas (até ~16 MB). */
private val imageCache = object : LruCache<String, ImageBitmap>(16 * 1024 * 1024) {
    override fun sizeOf(key: String, value: ImageBitmap) = value.width * value.height * 4
}

/** Miniatura de uma foto do servidor. O toque abre o visualizador do próprio app ([onClick]). */
@Composable
fun RemoteMediaTile(url: String, isVideo: Boolean, size: Dp, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bitmap by produceState(initialValue = imageCache.get(url), url, isVideo) {
        if (value != null || isVideo) return@produceState
        val bytes = BackendHttp.downloadBytes(url) ?: return@produceState
        value = withContext(Dispatchers.Default) {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 600) sample *= 2
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
                ?.asImageBitmap()
        }?.also { imageCache.put(url, it) }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClickLabel = if (isVideo) "Assistir vídeo" else "Ver foto", onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val image = bitmap
        when {
            isVideo -> Icon(CitizenIcons.Play, contentDescription = "Vídeo", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(32.dp))
            image != null -> Image(image, contentDescription = "Foto do relato", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        }
    }
}

/** Miniatura de um arquivo escolhido no aparelho (antes do envio). */
@Composable
fun LocalMediaThumbnail(uri: Uri, isVideo: Boolean, size: Dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.loadThumbnail(uri, Size(256, 256), null).asImageBitmap()
                } else if (!isVideo) {
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = 8 })
                    }?.asImageBitmap()
                } else {
                    null
                }
            }.getOrNull()
        }
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let { Image(it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        if (isVideo) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(CitizenIcons.Play, contentDescription = "Vídeo", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}
