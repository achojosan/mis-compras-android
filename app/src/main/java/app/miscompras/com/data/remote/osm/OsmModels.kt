package app.miscompras.com.data.remote.osm

import kotlinx.serialization.Serializable

@Serializable
data class OsmResponse(val elements: List<OsmElement> = emptyList())

/** Punto de un elemento OSM. Los `way`/`relation` lo traen en `center` (con `out center`). */
@Serializable
data class OsmCenter(val lat: Double, val lon: Double)

@Serializable
data class OsmElement(
    val type: String,
    val id: Long,
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OsmCenter? = null,
    val tags: Map<String, String> = emptyMap()
) {
    val latitude: Double? get() = lat ?: center?.lat
    val longitude: Double? get() = lon ?: center?.lon
    val name: String? get() = tags["name"]
    val brand: String? get() = tags["brand"]
    val operator: String? get() = tags["operator"]
    val openingHours: String? get() = tags["opening_hours"]
}
