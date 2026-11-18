package com.example.projetorevisao.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/** Arquivo pronto para envio. */
class PreparedMedia(val bytes: ByteArray, val mimeType: String, val fileName: String)

/** Erro com mensagem já em português, para mostrar ao usuário. */
class MediaPreparationException(message: String) : Exception(message)

object MediaAttachment {
    const val MAX_ATTACHMENTS = 5
    private const val MAX_VIDEO_BYTES = 50L * 1024 * 1024
    private const val MAX_IMAGE_SIDE = 1600
    private val VIDEO_TYPES = setOf("video/mp4", "video/3gpp", "video/quicktime")

    fun isVideo(context: Context, uri: Uri): Boolean =
        context.contentResolver.getType(uri)?.startsWith("video/") == true

    /**
     * Fotos são reduzidas (lado maior até 1600 px) e regravadas em JPEG, o que também remove os
     * metadados EXIF (como a localização gravada pela câmera). Vídeos vão como estão, até 50 MB.
     */
    suspend fun prepare(context: Context, uri: Uri, index: Int): PreparedMedia = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val type = resolver.getType(uri).orEmpty()
        if (type.startsWith("video/")) {
            if (type !in VIDEO_TYPES) throw MediaPreparationException("Formato de vídeo não aceito. Use MP4.")
            val size = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            if (size > MAX_VIDEO_BYTES) throw MediaPreparationException("O vídeo passa de 50 MB. Grave um trecho menor.")
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw MediaPreparationException("Não foi possível ler o vídeo.")
            if (bytes.size > MAX_VIDEO_BYTES) throw MediaPreparationException("O vídeo passa de 50 MB. Grave um trecho menor.")
            return@withContext PreparedMedia(bytes, type, "video_$index.${type.substringAfter('/')}")
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0) throw MediaPreparationException("Não foi possível ler a foto.")
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_IMAGE_SIDE) sample *= 2
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: throw MediaPreparationException("Não foi possível ler a foto.")

        // Fotos da câmera guardam a orientação no EXIF; aplicamos a rotação antes de regravar (o EXIF é descartado).
        val rotation = resolver.openInputStream(uri)?.use { input ->
            when (ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        val scale = minOf(1f, MAX_IMAGE_SIDE.toFloat() / maxOf(decoded.width, decoded.height))
        val bitmap = if (scale < 1f || rotation != 0f) {
            val matrix = Matrix().apply {
                postScale(scale, scale)
                postRotate(rotation)
            }
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                .also { if (it !== decoded) decoded.recycle() }
        } else {
            decoded
        }
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 82, out)
        bitmap.recycle()
        PreparedMedia(out.toByteArray(), "image/jpeg", "foto_$index.jpg")
    }
}
