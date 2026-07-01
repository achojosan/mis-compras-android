package app.miscompras.com.data.remote.osm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OsmResponse(
    val elements: List<OsmElement> = emptyList()
)

@Serializable
data class OsmElement(
    val type: String,
    val id: Long,
    val lat: Double? = null,
    val lon: Double? = null,
    val tags: Map<String, String> = emptyMap()
) {
    val name: String? get() = tags["name"]
    val brand: String? get() = tags["brand"]
    val operator: String? get() = tags["operator"]
    val openingHours: String? get() = tags["opening_hours"]
    val phone: String? get() = tags["phone"]
    val website: String? get() = tags["website"]
}
