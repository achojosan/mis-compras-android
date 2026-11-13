package app.miscompras.com.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migración 2 -> 3.
 *
 * Cambia la relación de `purchase_history` con `supermarkets`:
 *  - `supermarketId` pasa a ser opcional (nullable).
 *  - Se añade `supermarketName` con una copia del nombre de la tienda.
 *  - La clave foránea pasa de CASCADE a SET NULL, de modo que borrar una
 *    tienda ya no elimina su historial de compras.
 *
 * Como SQLite no permite alterar una clave foránea en sitio, se recrea la tabla.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS purchase_history_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                supermarketId INTEGER,
                supermarketName TEXT,
                totalAmount REAL NOT NULL,
                purchasedAt INTEGER NOT NULL,
                productsJson TEXT NOT NULL,
                FOREIGN KEY(supermarketId) REFERENCES supermarkets(id) ON DELETE SET NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO purchase_history_new
                (id, supermarketId, supermarketName, totalAmount, purchasedAt, productsJson)
            SELECT
                ph.id,
                ph.supermarketId,
                s.name,
                ph.totalAmount,
                ph.purchasedAt,
                ph.productsJson
            FROM purchase_history ph
            LEFT JOIN supermarkets s ON s.id = ph.supermarketId
            """.trimIndent()
        )

        db.execSQL("DROP TABLE purchase_history")
        db.execSQL("ALTER TABLE purchase_history_new RENAME TO purchase_history")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_purchase_history_supermarketId " +
                "ON purchase_history(supermarketId)"
        )
    }
}

/** Migración 3 -> 4: añade `known_items` e `item_references` (reconocimiento de artículos). */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // SQL idéntico al generado por Room (AppDatabase_Impl.createAllTables);
        // cualquier diferencia haría fallar la validación del esquema.
        db.execSQL("CREATE TABLE IF NOT EXISTS `known_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `barcode` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_known_items_barcode` ON `known_items` (`barcode`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `item_references` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `itemId` INTEGER NOT NULL, `imagePath` TEXT NOT NULL, `embedding` BLOB NOT NULL, FOREIGN KEY(`itemId`) REFERENCES `known_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_references_itemId` ON `item_references` (`itemId`)")
    }
}

/** Migración 4 -> 5: tarjeta de cliente en las tiendas (dos columnas nulas; no toca los datos existentes). */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `supermarkets` ADD COLUMN `loyaltyCode` TEXT")
        db.execSQL("ALTER TABLE `supermarkets` ADD COLUMN `loyaltyFormat` INTEGER")
    }
}

/**
 * Migración 5 -> 6: color de pase por tienda y color/icono por artículo
 * (tres columnas nulas; no toca los datos existentes).
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `supermarkets` ADD COLUMN `passColor` INTEGER")
        db.execSQL("ALTER TABLE `known_items` ADD COLUMN `color` INTEGER")
        db.execSQL("ALTER TABLE `known_items` ADD COLUMN `icon` TEXT")
    }
}
