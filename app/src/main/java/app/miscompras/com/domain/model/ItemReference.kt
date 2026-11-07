package app.miscompras.com.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Foto de referencia de un [KnownItem] junto con su vector de características. */
@Entity(
    tableName = "item_references",
    foreignKeys = [
        ForeignKey(
            entity = KnownItem::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId")]
)
data class ItemReference(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val imagePath: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val embedding: ByteArray
) {
    override fun equals(other: Any?): Boolean =
        other is ItemReference && id == other.id && itemId == other.itemId &&
            imagePath == other.imagePath && embedding.contentEquals(other.embedding)

    override fun hashCode(): Int = 31 * id.hashCode() + imagePath.hashCode()
}
