package app.miscompras.com.domain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Artículo que el usuario ha dado de alta para que la app lo reconozca
 * (por código de barras y/o por su aspecto visual).
 */
@Entity(
    tableName = "known_items",
    indices = [Index("barcode")]
)
data class KnownItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val barcode: String? = null,
    /** Id del color de pase del artículo (PassPalette); nulo = el de por defecto. */
    val color: Int? = null,
    /** Clave del icono del artículo (ver ItemIcons); nula = ninguno. */
    val icon: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
