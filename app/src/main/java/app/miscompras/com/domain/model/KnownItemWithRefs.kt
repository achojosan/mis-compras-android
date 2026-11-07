package app.miscompras.com.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class KnownItemWithRefs(
    @Embedded val item: KnownItem,
    @Relation(parentColumn = "id", entityColumn = "itemId")
    val references: List<ItemReference>
)
