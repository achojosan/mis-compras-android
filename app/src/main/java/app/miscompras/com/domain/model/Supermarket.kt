package app.miscompras.com.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supermarkets")
data class Supermarket(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val logoPath: String? = null,
    val isDefault: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val openingHours: String? = null,
    /** Tarjeta de cliente: contenido del código y formato ML Kit con el que se leyó. */
    val loyaltyCode: String? = null,
    val loyaltyFormat: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
