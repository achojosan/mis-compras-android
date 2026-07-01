package app.miscompras.com.data.repository

import app.miscompras.com.data.local.database.dao.SupermarketDao
import app.miscompras.com.domain.model.Supermarket
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupermarketRepository @Inject constructor(
    private val supermarketDao: SupermarketDao
) {
    fun getAllSupermarkets(): Flow<List<Supermarket>> = supermarketDao.getAllSupermarkets()

    fun getSupermarketById(id: Long): Flow<Supermarket?> = supermarketDao.getSupermarketById(id)

    fun getDefaultSupermarkets(): Flow<List<Supermarket>> = supermarketDao.getDefaultSupermarkets()

    suspend fun insertSupermarket(supermarket: Supermarket): Long =
        supermarketDao.insertSupermarket(supermarket)

    suspend fun updateSupermarket(supermarket: Supermarket) =
        supermarketDao.updateSupermarket(supermarket)

    suspend fun deleteSupermarket(supermarket: Supermarket) =
        supermarketDao.deleteSupermarket(supermarket)

    suspend fun supermarketExists(name: String): Boolean =
        supermarketDao.supermarketExists(name)
}
