package app.miscompras.com.data.local.database.dao

import androidx.room.*
import app.miscompras.com.domain.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE supermarketId = :supermarketId ORDER BY name ASC")
    fun getProductsBySupermarket(supermarketId: Long): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE supermarketId = :supermarketId AND isPurchased = 0 ORDER BY name ASC")
    fun getPendingProductsBySupermarket(supermarketId: Long): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isPurchased = 0 ORDER BY name ASC")
    fun getAllPendingProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): Flow<Product?>

    @Query("SELECT DISTINCT name FROM products ORDER BY name ASC")
    fun getAllProductNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET isPurchased = :isPurchased, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun updatePurchaseStatus(productId: Long, isPurchased: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM products WHERE supermarketId = :supermarketId AND isPurchased = 1")
    suspend fun deletePurchasedProducts(supermarketId: Long)
}
