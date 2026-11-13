package app.miscompras.com.di

import android.content.Context
import androidx.room.Room
import app.miscompras.com.data.local.database.AppDatabase
import app.miscompras.com.data.local.database.MIGRATION_2_3
import app.miscompras.com.data.local.database.MIGRATION_3_4
import app.miscompras.com.data.local.database.MIGRATION_4_5
import app.miscompras.com.data.local.database.MIGRATION_5_6
import app.miscompras.com.data.local.database.dao.KnownItemDao
import app.miscompras.com.data.local.database.dao.ProductDao
import app.miscompras.com.data.local.database.dao.PurchaseHistoryDao
import app.miscompras.com.data.local.database.dao.SupermarketDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mis_compras_database"
        )
        .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideSupermarketDao(database: AppDatabase): SupermarketDao {
        return database.supermarketDao()
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun providePurchaseHistoryDao(database: AppDatabase): PurchaseHistoryDao {
        return database.purchaseHistoryDao()
    }

    @Provides
    fun provideKnownItemDao(database: AppDatabase): KnownItemDao {
        return database.knownItemDao()
    }
}
