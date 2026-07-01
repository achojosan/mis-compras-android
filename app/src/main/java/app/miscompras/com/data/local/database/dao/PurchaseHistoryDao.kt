package app.miscompras.com.data.local.database.dao

import androidx.room.*
import app.miscompras.com.domain.model.PurchaseHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseHistoryDao {
    @Query("SELECT * FROM purchase_history ORDER BY purchasedAt DESC")
    fun getAllHistory(): Flow<List<PurchaseHistory>>

    @Query("SELECT * FROM purchase_history WHERE id = :id")
    fun getHistoryById(id: Long): Flow<PurchaseHistory?>

    @Query("SELECT * FROM purchase_history WHERE supermarketId = :supermarketId ORDER BY purchasedAt DESC")
    fun getHistoryBySupermarket(supermarketId: Long): Flow<List<PurchaseHistory>>

    @Insert
    suspend fun insertHistory(history: PurchaseHistory): Long

    @Delete
    suspend fun deleteHistory(history: PurchaseHistory)
}
