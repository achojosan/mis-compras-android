package app.miscompras.com.data.local.database.dao

import androidx.room.*
import app.miscompras.com.domain.model.ItemReference
import app.miscompras.com.domain.model.KnownItem
import app.miscompras.com.domain.model.KnownItemWithRefs
import kotlinx.coroutines.flow.Flow

@Dao
interface KnownItemDao {
    @Transaction
    @Query("SELECT * FROM known_items ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<KnownItemWithRefs>>

    @Transaction
    @Query("SELECT * FROM known_items WHERE id = :id")
    fun observeById(id: Long): Flow<KnownItemWithRefs?>

    @Transaction
    @Query("SELECT * FROM known_items WHERE id = :id")
    suspend fun getById(id: Long): KnownItemWithRefs?

    @Query("SELECT * FROM known_items WHERE barcode = :barcode")
    suspend fun findByBarcode(barcode: String): List<KnownItem>

    @Query("SELECT * FROM item_references")
    suspend fun getAllReferences(): List<ItemReference>

    @Query("SELECT * FROM item_references WHERE itemId = :itemId")
    suspend fun getReferences(itemId: Long): List<ItemReference>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: KnownItem): Long

    @Update
    suspend fun updateItem(item: KnownItem)

    @Query("DELETE FROM known_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Insert
    suspend fun insertReference(reference: ItemReference): Long

    @Query("DELETE FROM item_references WHERE id = :id")
    suspend fun deleteReference(id: Long)
}
