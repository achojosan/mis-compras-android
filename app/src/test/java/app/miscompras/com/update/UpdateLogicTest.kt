package app.miscompras.com.update

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateLogicTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val real = javaClass.getResource("/github_release_latest.json")!!.readText()

    @Test fun numericVersionComparison() {
        assertTrue(UpdateLogic.isNewer("1.9", "1.10"))      // 1.10 > 1.9 (no alfabético)
        assertTrue(UpdateLogic.isNewer("1.9", "v2.0"))
        assertTrue(UpdateLogic.isNewer("1.9", "1.9.1"))
        assertFalse(UpdateLogic.isNewer("1.10", "1.9"))
        assertFalse(UpdateLogic.isNewer("1.9", "1.9"))
        assertFalse(UpdateLogic.isNewer("1.9", "1.9.0"))
    }

    @Test fun invalidVersionsNeverTriggerAnUpdate() {
        assertFalse(UpdateLogic.isNewer("1.9", "latest"))
        assertFalse(UpdateLogic.isNewer("1.9", ""))
        assertFalse(UpdateLogic.isNewer("1.9", "1.x"))
        assertFalse(UpdateLogic.isNewer("dev", "2.0"))
        assertNull(UpdateLogic.parseVersion("1..2"))
    }

    @Test fun parsesTheRealGitHubResponse() {
        val r = UpdateLogic.parseRelease(real, json)!!
        assertEquals("1.9", r.version)
        assertEquals("https://github.com/achojosan/mis-compras-android/releases/download/v1.9/MisCompras-1.9.apk", r.apkUrl)
        assertEquals(81335361L, r.apkSize)
        assertEquals("e4bb5307cadf1012d8f73a928ef0b41e30e032a9d39ac83a6c75645a6564d961", r.sha256)
    }

    @Test fun shaFallsBackToTheReleaseNotes() {
        val body = """{"tag_name":"v2.0","body":"- SHA-256 del APK: `ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789`",
            "assets":[{"name":"a.apk","size":1,"browser_download_url":"https://github.com/achojosan/mis-compras-android/releases/download/v2.0/a.apk"}]}"""
        assertEquals("abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789", UpdateLogic.parseRelease(body, json)!!.sha256)
    }

    @Test fun rejectsDraftsPreReleasesAndForeignDownloads() {
        val asset = """"assets":[{"name":"a.apk","size":1,"browser_download_url":"https://github.com/achojosan/mis-compras-android/releases/download/v2.0/a.apk"}]"""
        assertNull(UpdateLogic.parseRelease("""{"tag_name":"v2.0","draft":true,$asset}""", json))
        assertNull(UpdateLogic.parseRelease("""{"tag_name":"v2.0","prerelease":true,$asset}""", json))
        assertNull(UpdateLogic.parseRelease("""{"tag_name":"nightly",$asset}""", json))
        assertNull(UpdateLogic.parseRelease("""{"tag_name":"v2.0","assets":[{"name":"a.apk","size":1,"browser_download_url":"https://evil.example/a.apk"}]}""", json))
        assertNull(UpdateLogic.parseRelease("""{"tag_name":"v2.0","assets":[{"name":"notas.txt","size":1,"browser_download_url":"https://github.com/achojosan/mis-compras-android/releases/download/v2.0/notas.txt"}]}""", json))
    }

    @Test fun highlightsComeFromTheNovedadesSection() {
        val h = UpdateLogic.highlights(UpdateLogic.parseRelease(real, json)!!.notes)
        assertEquals(4, h.size)
        assertEquals("Reconocimiento de artículos", h[0])
        assertEquals("Tiendas cercanas", h[1])
        assertTrue(h.all { it.length <= 81 })   // titulares cortos: el aviso no debe desbordar
        assertFalse(h.any { it.contains("**") || it.contains("`") })
    }

    @Test fun formatSize() {
        assertEquals("78 MB", UpdateLogic.formatSize(81335361))
        assertEquals("", UpdateLogic.formatSize(0))
    }
}
