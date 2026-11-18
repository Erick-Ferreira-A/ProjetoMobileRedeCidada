package com.example.projetorevisao.server.repository

import com.example.projetorevisao.server.config.AppConfig
import com.example.projetorevisao.server.database.DatabaseFactory
import com.example.projetorevisao.server.models.MediaDto
import java.io.File
import java.io.InputStream
import java.util.UUID

/** Arquivo guardado em disco e o tipo com que deve ser servido. */
data class StoredMedia(val file: File, val mimeType: String)

sealed interface UploadResult {
    data class Success(val media: MediaDto) : UploadResult
    data class Rejected(val message: String, val notFound: Boolean = false, val forbidden: Boolean = false) : UploadResult
}

/**
 * Fotos e vídeos anexados aos relatos. Os arquivos ficam em [AppConfig.mediaDirectory] com nome aleatório;
 * o banco guarda só a referência. O tipo é conferido pelos primeiros bytes, não só pelo que o app declara.
 */
class MediaRepository {

    private val baseDir: File get() = File(AppConfig.mediaDirectory).also { it.mkdirs() }
    /** Pasta onde os arquivos ficam (usada para apagar anexos na moderação). */
    val directory: File get() = baseDir

    private enum class Kind(val maxBytes: Long) { IMAGE(10L * 1024 * 1024), VIDEO(50L * 1024 * 1024) }

    private val allowed = mapOf(
        "image/jpeg" to (Kind.IMAGE to "jpg"),
        "image/png" to (Kind.IMAGE to "png"),
        "image/webp" to (Kind.IMAGE to "webp"),
        "video/mp4" to (Kind.VIDEO to "mp4"),
        "video/3gpp" to (Kind.VIDEO to "3gp"),
        "video/quicktime" to (Kind.VIDEO to "mov")
    )

    companion object {
        const val MAX_PER_INCIDENT = 5
        fun urlFor(mediaId: String) = "/api/v1/media/$mediaId"
    }

    fun save(userId: String, incidentId: String, declaredMime: String?, input: InputStream): UploadResult {
        val reporter = DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement("SELECT reporter_id FROM incidents WHERE id = ?").use { stmt ->
                stmt.setString(1, incidentId)
                stmt.executeQuery().use { rs -> if (rs.next()) (rs.getString(1) ?: "") else null }
            }
        } ?: return UploadResult.Rejected("Relato não encontrado.", notFound = true)
        if (reporter != userId) return UploadResult.Rejected("Só quem criou o relato pode anexar arquivos.", forbidden = true)
        if (mediaFor(incidentId).size >= MAX_PER_INCIDENT) {
            return UploadResult.Rejected("Cada relato aceita no máximo $MAX_PER_INCIDENT arquivos.")
        }

        val mime = declaredMime?.substringBefore(';')?.trim()?.lowercase()
        val (kind, ext) = allowed[mime]
            ?: return UploadResult.Rejected("Formato não aceito. Envie foto (JPG, PNG, WEBP) ou vídeo (MP4, 3GP, MOV).")

        val mediaId = UUID.randomUUID().toString()
        val target = File(baseDir, "$mediaId.$ext")
        val size = try {
            copyWithLimit(input, target, kind.maxBytes)
        } catch (_: TooLargeException) {
            target.delete()
            return UploadResult.Rejected("Arquivo muito grande. Limite: ${kind.maxBytes / (1024 * 1024)} MB.")
        }

        if (size == 0L || !matchesSignature(target, kind, mime!!)) {
            target.delete()
            return UploadResult.Rejected("O conteúdo do arquivo não corresponde ao formato informado.")
        }

        DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement(
                "INSERT INTO incident_media (id, incident_id, uploader_id, file_name, mime_type, size_bytes, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)"
            ).use { stmt ->
                stmt.setString(1, mediaId)
                stmt.setString(2, incidentId)
                stmt.setString(3, userId)
                stmt.setString(4, target.name)
                stmt.setString(5, mime)
                stmt.setLong(6, size)
                stmt.setLong(7, System.currentTimeMillis())
                stmt.executeUpdate()
            }
        }
        return UploadResult.Success(MediaDto(id = mediaId, url = urlFor(mediaId), mimeType = mime))
    }

    /** Busca o arquivo pelo id do banco (nunca pelo nome vindo da URL, para evitar acesso a outros arquivos). */
    fun find(mediaId: String): StoredMedia? = DatabaseFactory.getConnection().use { conn ->
        conn.prepareStatement("SELECT file_name, mime_type FROM incident_media WHERE id = ?").use { stmt ->
            stmt.setString(1, mediaId)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) return null
                val file = File(baseDir, rs.getString("file_name"))
                if (file.isFile) StoredMedia(file, rs.getString("mime_type")) else null
            }
        }
    }

    fun mediaFor(incidentId: String): List<MediaDto> = mediaForAll(listOf(incidentId))[incidentId].orEmpty()

    fun mediaForAll(incidentIds: List<String>): Map<String, List<MediaDto>> {
        if (incidentIds.isEmpty()) return emptyMap()
        val result = mutableMapOf<String, MutableList<MediaDto>>()
        DatabaseFactory.getConnection().use { conn ->
            val placeholders = incidentIds.joinToString(",") { "?" }
            conn.prepareStatement(
                "SELECT id, incident_id, mime_type FROM incident_media WHERE incident_id IN ($placeholders) ORDER BY created_at"
            ).use { stmt ->
                incidentIds.forEachIndexed { i, id -> stmt.setString(i + 1, id) }
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        val id = rs.getString("id")
                        result.getOrPut(rs.getString("incident_id")) { mutableListOf() }
                            .add(MediaDto(id = id, url = urlFor(id), mimeType = rs.getString("mime_type")))
                    }
                }
            }
        }
        return result
    }

    private class TooLargeException : Exception()

    private fun copyWithLimit(input: InputStream, target: File, maxBytes: Long): Long {
        var total = 0L
        target.outputStream().use { out ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                if (total > maxBytes) throw TooLargeException()
                out.write(buffer, 0, read)
            }
        }
        return total
    }

    private fun matchesSignature(file: File, kind: Kind, mime: String): Boolean {
        val head = ByteArray(12)
        val n = file.inputStream().use { it.read(head) }
        if (n < 12) return false
        fun ascii(from: Int, to: Int) = String(head.copyOfRange(from, to), Charsets.US_ASCII)
        return when {
            mime == "image/jpeg" -> head[0] == 0xFF.toByte() && head[1] == 0xD8.toByte() && head[2] == 0xFF.toByte()
            mime == "image/png" -> head[0] == 0x89.toByte() && ascii(1, 4) == "PNG"
            mime == "image/webp" -> ascii(0, 4) == "RIFF" && ascii(8, 12) == "WEBP"
            kind == Kind.VIDEO -> ascii(4, 8) == "ftyp" // MP4, 3GP e MOV usam a caixa "ftyp"
            else -> false
        }
    }
}
