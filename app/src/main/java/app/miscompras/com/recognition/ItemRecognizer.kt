package app.miscompras.com.recognition

import android.graphics.Bitmap
import app.miscompras.com.data.repository.KnownItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Une el repositorio y el embedder con la lógica pura de [RecognitionSession]. */
@Singleton
class ItemRecognizer @Inject constructor(
    private val repository: KnownItemRepository,
    private val embedder: ItemEmbedder
) {
    private var refs: List<RefVector> = emptyList()

    /** Recarga las fotos de referencia (llamar al empezar a escanear). */
    suspend fun loadReferences() = withContext(Dispatchers.IO) {
        refs = repository.getAllReferences().map { RefVector(it.itemId, EmbeddingMath.fromBytes(it.embedding)) }
    }

    suspend fun itemsForBarcode(code: String): List<Long> =
        repository.findByBarcode(code).map { it.id }

    suspend fun rank(crop: Bitmap): List<ItemScore> = withContext(Dispatchers.Default) {
        EmbeddingMath.rank(embedder.embed(crop), refs)
    }

    suspend fun embed(crop: Bitmap): FloatArray = withContext(Dispatchers.Default) { embedder.embed(crop) }
}
