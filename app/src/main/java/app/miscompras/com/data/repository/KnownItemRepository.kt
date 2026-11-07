package app.miscompras.com.data.repository

import android.content.Context
import android.graphics.Bitmap
import app.miscompras.com.data.local.database.dao.KnownItemDao
import app.miscompras.com.domain.model.ItemReference
import app.miscompras.com.domain.model.KnownItem
import app.miscompras.com.domain.model.KnownItemWithRefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KnownItemRepository @Inject constructor(
    private val dao: KnownItemDao,
    @ApplicationContext private val context: Context
) {
    private val refsDir: File
        get() = File(context.filesDir, "item_refs").apply { mkdirs() }

    fun observeAll(): Flow<List<KnownItemWithRefs>> = dao.observeAll()

    fun observeById(id: Long): Flow<KnownItemWithRefs?> = dao.observeById(id)

    suspend fun getById(id: Long): KnownItemWithRefs? = dao.getById(id)

    suspend fun findByBarcode(barcode: String): List<KnownItem> = dao.findByBarcode(barcode)

    suspend fun getAllReferences(): List<ItemReference> = dao.getAllReferences()

    suspend fun saveItem(item: KnownItem): Long =
        if (item.id == 0L) dao.insertItem(item)
        else {
            dao.updateItem(item.copy(updatedAt = System.currentTimeMillis()))
            item.id
        }

    /** Guarda la foto en disco y su embedding en la base de datos. */
    suspend fun addReference(itemId: Long, photo: Bitmap, embedding: ByteArray): Long =
        withContext(Dispatchers.IO) {
            val file = File(refsDir, "ref_${itemId}_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { photo.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            dao.insertReference(ItemReference(itemId = itemId, imagePath = file.absolutePath, embedding = embedding))
        }

    suspend fun deleteReference(reference: ItemReference) = withContext(Dispatchers.IO) {
        dao.deleteReference(reference.id)
        File(reference.imagePath).delete()
    }

    suspend fun deleteItem(id: Long) = withContext(Dispatchers.IO) {
        val refs = dao.getReferences(id)
        dao.deleteItem(id)
        refs.forEach { File(it.imagePath).delete() }
    }
}
