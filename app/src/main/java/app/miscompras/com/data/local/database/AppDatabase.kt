package app.miscompras.com.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import app.miscompras.com.data.local.database.dao.KnownItemDao
import app.miscompras.com.data.local.database.dao.ProductDao
import app.miscompras.com.data.local.database.dao.PurchaseHistoryDao
import app.miscompras.com.data.local.database.dao.SupermarketDao
import app.miscompras.com.domain.model.ItemReference
import app.miscompras.com.domain.model.KnownItem
import app.miscompras.com.domain.model.Product
import app.miscompras.com.domain.model.PurchaseHistory
import app.miscompras.com.domain.model.Supermarket

@Database(
    entities = [
        Supermarket::class, Product::class, PurchaseHistory::class,
        KnownItem::class, ItemReference::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun supermarketDao(): SupermarketDao
    abstract fun productDao(): ProductDao
    abstract fun purchaseHistoryDao(): PurchaseHistoryDao
    abstract fun knownItemDao(): KnownItemDao
}
