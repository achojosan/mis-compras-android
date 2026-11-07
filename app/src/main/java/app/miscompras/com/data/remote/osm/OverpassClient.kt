package app.miscompras.com.data.remote.osm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverpassClient @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json
) {
    /**
     * Supermercados y tiendas de alimentación alrededor de un punto. Lanza [IOException] si ningún
     * servidor responde a tiempo. Los servidores públicos de Overpass se saturan a menudo (pueden tardar
     * más de 30 s), así que se prueba un servidor de respaldo con un límite de tiempo por intento.
     */
    suspend fun nearbyShops(lat: Double, lon: Double, radiusMeters: Int = DEFAULT_RADIUS): List<OsmElement> =
        withContext(Dispatchers.IO) {
            val attemptClient = client.newBuilder()
                .callTimeout(ATTEMPT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(ATTEMPT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()
            var last: IOException? = null
            for (server in SERVERS) {
                try {
                    val url = server.toHttpUrl().newBuilder()
                        .addQueryParameter("data", query(lat, lon, radiusMeters))
                        .build()
                    attemptClient.newCall(Request.Builder().url(url).build()).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Overpass HTTP ${response.code}")
                        val body = response.body?.string() ?: throw IOException("Respuesta vacía")
                        return@withContext json.decodeFromString(OsmResponse.serializer(), body).elements
                    }
                } catch (e: IOException) {
                    last = e
                }
            }
            throw last ?: IOException("Sin servidores")
        }

    companion object {
        const val DEFAULT_RADIUS = 10_000
        private const val ATTEMPT_TIMEOUT_SECONDS = 30L
        private val SERVERS = listOf(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.private.coffee/api/interpreter"
        )

        /** `nwr` = nodos, vías y relaciones; `out tags center` evita descargar la geometría completa. */
        fun query(lat: Double, lon: Double, radius: Int): String = """
            [out:json][timeout:25];
            nwr["shop"~"supermarket|convenience"](around:$radius,$lat,$lon);
            out tags center;
        """.trimIndent()
    }
}
