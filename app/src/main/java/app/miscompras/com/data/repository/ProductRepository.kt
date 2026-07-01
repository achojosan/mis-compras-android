package app.miscompras.com.data.repository

import app.miscompras.com.data.local.database.dao.ProductDao
import app.miscompras.com.domain.model.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {
    fun getProductsBySupermarket(supermarketId: Long): Flow<List<Product>> =
        productDao.getProductsBySupermarket(supermarketId)

    fun getPendingProductsBySupermarket(supermarketId: Long): Flow<List<Product>> =
        productDao.getPendingProductsBySupermarket(supermarketId)

    fun getAllPendingProducts(): Flow<List<Product>> =
        productDao.getAllPendingProducts()

    fun getProductById(id: Long): Flow<Product?> =
        productDao.getProductById(id)

    fun getAllProductNames(): Flow<List<String>> =
        productDao.getAllProductNames()

    suspend fun insertProduct(product: Product): Long =
        productDao.insertProduct(product)

    suspend fun updateProduct(product: Product) =
        productDao.updateProduct(product)

    suspend fun deleteProduct(product: Product) =
        productDao.deleteProduct(product)

    suspend fun updatePurchaseStatus(productId: Long, isPurchased: Boolean) =
        productDao.updatePurchaseStatus(productId, isPurchased)

    suspend fun deletePurchasedProducts(supermarketId: Long) =
        productDao.deletePurchasedProducts(supermarketId)
}
