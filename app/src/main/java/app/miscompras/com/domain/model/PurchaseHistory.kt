package app.miscompras.com.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchase_history",
    foreignKeys = [
        ForeignKey(
            entity = Supermarket::class,
            parentColumns = ["id"],
            childColumns = ["supermarketId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("supermarketId")]
)
data class PurchaseHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // Nulo si el supermercado fue eliminado; el historial se conserva igualmente.
    val supermarketId: Long?,
    // Copia del nombre para poder mostrar el gasto aunque la tienda ya no exista.
    val supermarketName: String? = null,
    val totalAmount: Double,
    val purchasedAt: Long = System.currentTimeMillis(),
    val productsJson: String
)
