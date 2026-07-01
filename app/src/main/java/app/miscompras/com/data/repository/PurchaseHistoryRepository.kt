package app.miscompras.com.data.repository

import app.miscompras.com.data.local.database.dao.PurchaseHistoryDao
import app.miscompras.com.domain.model.PurchaseHistory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseHistoryRepository @Inject constructor(
    private val purchaseHistoryDao: PurchaseHistoryDao
) {
    fun getAllHistory(): Flow<List<PurchaseHistory>> =
        purchaseHistoryDao.getAllHistory()

    fun getHistoryById(id: Long): Flow<PurchaseHistory?> =
        purchaseHistoryDao.getHistoryById(id)

    fun getHistoryBySupermarket(supermarketId: Long): Flow<List<PurchaseHistory>> =
        purchaseHistoryDao.getHistoryBySupermarket(supermarketId)

    suspend fun insertHistory(history: PurchaseHistory): Long =
        purchaseHistoryDao.insertHistory(history)

    suspend fun deleteHistory(history: PurchaseHistory) =
        purchaseHistoryDao.deleteHistory(history)
}
