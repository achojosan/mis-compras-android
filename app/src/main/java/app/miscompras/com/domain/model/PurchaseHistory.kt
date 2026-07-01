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
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("supermarketId")]
)
data class PurchaseHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supermarketId: Long,
    val totalAmount: Double,
    val purchasedAt: Long = System.currentTimeMillis(),
    val productsJson: String
)
