package app.miscompras.com.nearby

import app.miscompras.com.data.remote.osm.OsmElement
import java.text.Normalizer
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Cadena de supermercados conocida. [name] es el nombre canónico con el que se guarda la tienda
 * (el mismo que usan los logos y `SupermarketApps.forName`), p. ej. "Dia" aunque OSM diga "Supermercados Dia".
 *
 * @param startsWithOnly solo coincide si el nombre EMPIEZA por el alias (evita "Frutas del Día").
 */
class Chain(val name: String, val aliases: List<String>, val startsWithOnly: Boolean = false)

object Chains {
    val all = listOf(
        Chain("Mercadona", listOf("mercadona")),
        Chain("Carrefour", listOf("carrefour")),
        Chain("Lidl", listOf("lidl")),
        Chain("Aldi", listOf("aldi")),
        Chain("Dia", listOf("dia", "supermercados dia"), startsWithOnly = true),
        Chain("Hipercor", listOf("hipercor")),
        Chain("Supercor", listOf("supercor")),
        Chain("El Corte Inglés", listOf("el corte ingles")),
        Chain("Alcampo", listOf("alcampo")),
        Chain("Consum", listOf("consum")),
        Chain("Eroski", listOf("eroski")),
        Chain("BM Supermercados", listOf("bm", "bm supermercados"), startsWithOnly = true)
    )

    fun normalize(value: String): String =
        Normalizer.normalize(value.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("\\s+"), " ")

    /** La cadena a la que pertenece un establecimiento OSM (por marca, nombre u operador). */
    fun match(vararg candidates: String?): Chain? {
        for (raw in candidates) {
            val text = raw?.let(::normalize)?.takeIf { it.isNotEmpty() } ?: continue
            for (chain in all) {
                for (alias in chain.aliases) {
                    val regex = if (chain.startsWithOnly) Regex("^${Regex.escape(alias)}(\\s|$)")
                    else Regex("(^|\\s)${Regex.escape(alias)}(\\s|$)")
                    if (regex.containsMatchIn(text)) return chain
                }
            }
        }
        return null
    }
}

/** La sucursal más cercana de una cadena. */
data class NearbyChain(
    val chainName: String,
    val distanceMeters: Double,
    val latitude: Double,
    val longitude: Double,
    val openingHours: String?,
    val branchCount: Int
)

object NearbyChains {
    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    /** Una entrada por cadena (la sucursal más cercana), ordenadas por distancia. */
    fun pick(elements: List<OsmElement>, lat: Double, lon: Double): List<NearbyChain> =
        elements.mapNotNull { e ->
            val eLat = e.latitude ?: return@mapNotNull null
            val eLon = e.longitude ?: return@mapNotNull null
            val chain = Chains.match(e.brand, e.name, e.operator) ?: return@mapNotNull null
            Triple(chain, e, distanceMeters(lat, lon, eLat, eLon))
        }.groupBy { it.first.name }
            .map { (chainName, branches) ->
                val (_, nearest, distance) = branches.minBy { it.third }
                NearbyChain(
                    chainName = chainName,
                    distanceMeters = distance,
                    latitude = nearest.latitude!!,
                    longitude = nearest.longitude!!,
                    openingHours = nearest.openingHours,
                    branchCount = branches.size
                )
            }.sortedBy { it.distanceMeters }

    /** "350 m" / "1,2 km". */
    fun formatDistance(meters: Double): String =
        if (meters < 1000) "${(meters / 10).toInt() * 10} m"
        else String.format(Locale("es", "ES"), "%.1f km", meters / 1000)
}
