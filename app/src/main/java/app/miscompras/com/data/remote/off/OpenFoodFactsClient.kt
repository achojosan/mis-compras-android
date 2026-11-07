package app.miscompras.com.data.remote.off

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class OffProduct(
    val name: String,
    val brand: String?,
    val quantity: String?,
    val nutriScore: String?
) {
    /** Texto de notas para el artículo: marca, cantidad y Nutri-Score. */
    fun notes(): String = listOfNotNull(
        brand?.let { "Marca: $it" },
        quantity?.let { "Cantidad: $it" },
        nutriScore?.let { "Nutri-Score: ${it.uppercase()}" }
    ).joinToString(". ")
}

@Serializable
private data class OffResponse(val status: Int = 0, val product: OffProductDto? = null)

@Serializable
private data class OffProductDto(
    val product_name: String? = null,
    val product_name_es: String? = null,
    val brands: String? = null,
    val quantity: String? = null,
    val nutriscore_grade: String? = null
)

object OffParser {
    private val nutriScores = setOf("a", "b", "c", "d", "e")

    /** `null` si el producto no existe o no tiene nombre. */
    fun parse(body: String, json: Json): OffProduct? {
        val response = json.decodeFromString(OffResponse.serializer(), body)
        val p = response.product ?: return null
        if (response.status != 1) return null
        val name = (p.product_name_es?.takeIf { it.isNotBlank() } ?: p.product_name)?.trim()
        if (name.isNullOrBlank()) return null
        return OffProduct(
            name = name,
            // "Nutella, Ferrero" -> "Nutella": la primera marca es la principal.
            brand = p.brands?.substringBefore(',')?.trim()?.takeIf { it.isNotEmpty() },
            quantity = p.quantity?.trim()?.takeIf { it.isNotEmpty() },
            nutriScore = p.nutriscore_grade?.lowercase()?.takeIf { it in nutriScores }
        )
    }
}

/** Consulta Open Food Facts. Solo se envía el código de barras, nada más. */
@Singleton
class OpenFoodFactsClient @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json
) {
    private val cache = HashMap<String, OffProduct?>()

    /** `null` si no se conoce el producto. Lanza [IOException] si no hay red. Cachea por código. */
    suspend fun lookup(barcode: String): OffProduct? {
        synchronized(cache) { if (cache.containsKey(barcode)) return cache[barcode] }
        val result = withContext(Dispatchers.IO) {
            val url = "https://world.openfoodfacts.org/api/v2/product/$barcode.json".toHttpUrl().newBuilder()
                .addQueryParameter("fields", "code,product_name,product_name_es,brands,nutriscore_grade,quantity")
                .build()
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Open Food Facts HTTP ${response.code}")
                OffParser.parse(response.body?.string() ?: throw IOException("Respuesta vacía"), json)
            }
        }
        synchronized(cache) { cache[barcode] = result }
        return result
    }
}
