package app.miscompras.com.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

/** Dónde se publican las versiones. */
object UpdateConfig {
    const val REPO = "achojosan/mis-compras-android"
    const val LATEST_API = "https://api.github.com/repos/$REPO/releases/latest"
    const val RELEASES_PAGE = "https://github.com/$REPO/releases/latest"

    /** Solo se descargan APK de las releases de este repositorio, por HTTPS. */
    const val DOWNLOAD_PREFIX = "https://github.com/$REPO/releases/download/"
}

@Serializable
private data class GhAsset(
    val name: String,
    val size: Long = 0,
    val digest: String? = null,
    @SerialName("browser_download_url") val url: String
)

@Serializable
private data class GhRelease(
    @SerialName("tag_name") val tag: String,
    val body: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val assets: List<GhAsset> = emptyList()
)

/** Una versión publicada, lista para descargar. */
data class ReleaseInfo(
    val version: String,
    val notes: String,
    val apkUrl: String,
    val apkSize: Long,
    /** SHA-256 esperado del APK (minúsculas), si la release lo publica. */
    val sha256: String?
)

/** Lógica del actualizador sin dependencias de Android (probada con JVM). */
object UpdateLogic {
    private val sha256Regex = Regex("^[0-9a-f]{64}$")
    private val notesShaRegex = Regex("SHA-256 del APK:\\s*`?([0-9a-fA-F]{64})", RegexOption.IGNORE_CASE)

    /** "v1.10" → [1, 10]; `null` si no es una versión numérica. */
    fun parseVersion(raw: String): List<Int>? {
        val text = raw.trim().removePrefix("v").removePrefix("V")
        if (text.isEmpty()) return null
        return text.split('.').map { it.toIntOrNull() ?: return null }
    }

    /** Compara número a número, así 1.10 es más nueva que 1.9. */
    fun isNewer(current: String, latest: String): Boolean {
        val c = parseVersion(current) ?: return false
        val l = parseVersion(latest) ?: return false
        for (i in 0 until maxOf(c.size, l.size)) {
            val a = c.getOrElse(i) { 0 }
            val b = l.getOrElse(i) { 0 }
            if (b != a) return b > a
        }
        return false
    }

    /** `null` si es borrador, preversión, no tiene versión válida o no trae un APK de este repositorio. */
    fun parseRelease(body: String, json: Json): ReleaseInfo? {
        val release = json.decodeFromString(GhRelease.serializer(), body)
        if (release.draft || release.prerelease || parseVersion(release.tag) == null) return null
        val apk = release.assets.firstOrNull {
            it.name.endsWith(".apk", ignoreCase = true) && it.url.startsWith(UpdateConfig.DOWNLOAD_PREFIX)
        } ?: return null
        val notes = release.body.orEmpty()
        val fromDigest = apk.digest?.removePrefix("sha256:")?.lowercase(Locale.ROOT)?.takeIf { sha256Regex.matches(it) }
        return ReleaseInfo(
            version = release.tag.trim().removePrefix("v").removePrefix("V"),
            notes = notes,
            apkUrl = apk.url,
            apkSize = apk.size,
            sha256 = fromDigest ?: extractSha256(notes)
        )
    }

    fun extractSha256(notes: String): String? =
        notesShaRegex.find(notes)?.groupValues?.get(1)?.lowercase(Locale.ROOT)

    /** Las viñetas de la sección «Novedades» sin formato markdown, para enseñarlas en el aviso. */
    fun highlights(notes: String, max: Int = 4): List<String> {
        val lines = notes.lines()
        val start = lines.indexOfFirst { it.trim().startsWith("## Novedades") }
        if (start < 0) return emptyList()
        return lines.drop(start + 1)
            .takeWhile { !it.trim().startsWith("## ") }
            .map { it.trim() }
            .filter { it.startsWith("- ") }
            .map { it.removePrefix("- ").replace("**", "").replace("`", "").trim() }
            .map(::shorten)
            .take(max)
    }

    /** Titular corto de una viñeta: lo anterior a «:» si es breve; si no, la primera frase recortada. */
    private fun shorten(text: String): String {
        val title = text.substringBefore(':', "")
        if (title.isNotEmpty() && title.length <= 60) return title
        val sentence = text.substringBefore(". ").removeSuffix(".")
        return if (sentence.length <= 80) sentence else sentence.take(80).trimEnd(' ', ',') + "…"
    }

    /** "78 MB" */
    fun formatSize(bytes: Long): String =
        if (bytes <= 0) "" else "${(bytes / 1_048_576.0).let { Math.round(it) }} MB"
}
