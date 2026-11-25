package com.example.projetorevisao.data.media

import android.content.Context
import com.example.projetorevisao.data.remote.BackendHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Baixa o vídeo de um relato para o cache do app antes de tocar.
 *
 * Vídeos gravados no celular costumam ter o índice (moov) no fim do arquivo. O player precisa
 * pular até lá, e o servidor ainda não aceita download em partes (Range), então tocar direto da
 * internet falhava com "Não é possível reproduzir este vídeo". Tocando do arquivo local funciona
 * com qualquer servidor. Os vídeos ficam no cache (o Android limpa se faltar espaço) e os mais
 * antigos são apagados além de [MAX_FILES].
 */
object VideoCache {
    private const val DIR = "reels"
    private const val MAX_FILES = 12

    suspend fun get(context: Context, url: String): File? = withContext(Dispatchers.IO) {
        val absolute = BackendHttp.absoluteUrl(url)
        val dir = File(context.cacheDir, DIR).apply { mkdirs() }
        val target = File(dir, sha1(absolute) + ".mp4")
        if (target.exists() && target.length() > 0) {
            target.setLastModified(System.currentTimeMillis())
            return@withContext target
        }
        val partial = File(dir, target.name + ".part")
        try {
            val connection = (URL(absolute).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
            }
            try {
                if (connection.responseCode !in 200..299) return@withContext null
                connection.inputStream.use { input -> partial.outputStream().use { input.copyTo(it, 64 * 1024) } }
            } finally {
                connection.disconnect()
            }
            if (!partial.renameTo(target)) return@withContext null
            trim(dir)
            target
        } catch (_: Exception) {
            partial.delete()
            null
        }
    }

    private fun trim(dir: File) {
        val files = dir.listFiles { f -> f.name.endsWith(".mp4") }?.sortedByDescending { it.lastModified() } ?: return
        files.drop(MAX_FILES).forEach { it.delete() }
    }

    private fun sha1(text: String): String =
        MessageDigest.getInstance("SHA-1").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
}
