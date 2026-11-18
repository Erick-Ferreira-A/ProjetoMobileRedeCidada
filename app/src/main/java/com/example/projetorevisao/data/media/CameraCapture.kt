package com.example.projetorevisao.data.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File

/** Arquivos temporários para a câmera gravar a foto/vídeo do relato. */
object CameraCapture {
    private const val MAX_VIDEO_SECONDS = 60
    private const val MAX_VIDEO_BYTES = 45L * 1024 * 1024

    fun newPhotoUri(context: Context): Uri = newFileUri(context, "foto_${System.currentTimeMillis()}.jpg")

    fun newVideoUri(context: Context): Uri = newFileUri(context, "video_${System.currentTimeMillis()}.mp4")

    private fun newFileUri(context: Context, name: String): Uri {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(dir, name))
    }

    /** Gravação limitada a 60 s e ~45 MB, para caber no limite de envio do servidor (50 MB). */
    class LimitedVideoCapture : ActivityResultContracts.CaptureVideo() {
        override fun createIntent(context: Context, input: Uri): Intent =
            super.createIntent(context, input)
                .putExtra(MediaStore.EXTRA_DURATION_LIMIT, MAX_VIDEO_SECONDS)
                .putExtra(MediaStore.EXTRA_SIZE_LIMIT, MAX_VIDEO_BYTES)
    }
}
