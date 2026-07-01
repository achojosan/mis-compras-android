package app.miscompras.com.domain.model

data class NearbyPlace(
    val id: Long,
    val name: String,
    val brand: String?,
    val latitude: Double,
    val longitude: Double,
    val openingHours: String?,
    val phone: String?,
    val website: String?,
    val distanceMeters: Double,
    val isOpenNow: Boolean?
)
