package app.miscompras.com.data.local.database.dao

import androidx.room.*
import app.miscompras.com.domain.model.Supermarket
import kotlinx.coroutines.flow.Flow

@Dao
interface SupermarketDao {
    @Query("SELECT * FROM supermarkets ORDER BY name ASC")
    fun getAllSupermarkets(): Flow<List<Supermarket>>

    @Query("SELECT * FROM supermarkets WHERE id = :id")
    fun getSupermarketById(id: Long): Flow<Supermarket?>

    @Query("SELECT * FROM supermarkets WHERE isDefault = 1")
    fun getDefaultSupermarkets(): Flow<List<Supermarket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupermarket(supermarket: Supermarket): Long

    @Update
    suspend fun updateSupermarket(supermarket: Supermarket)

    @Delete
    suspend fun deleteSupermarket(supermarket: Supermarket)

    @Query("SELECT EXISTS(SELECT 1 FROM supermarkets WHERE name = :name LIMIT 1)")
    suspend fun supermarketExists(name: String): Boolean
}
