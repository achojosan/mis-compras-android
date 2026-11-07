package app.miscompras.com.recognition

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

/** Umbrales ajustables del reconocimiento visual. */
object RecognitionConfig {
    /** Similitud coseno mínima para aceptar un artículo. */
    const val MIN_SCORE = 0.80f

    /** Diferencia mínima con el segundo artículo más parecido. */
    const val MIN_MARGIN = 0.05f

    /** Fotogramas consecutivos que deben coincidir en el mismo artículo. */
    const val STABLE_FRAMES = 3

    /** Lecturas consecutivas iguales del código de barras para darlo por bueno. */
    const val STABLE_BARCODE_READS = 2

    /** Tiempo sin resultado tras el cual se avisa de "no reconocido". */
    const val TIMEOUT_MS = 6_000L
}

/** Vector de características de una foto de referencia. */
class RefVector(val itemId: Long, val vector: FloatArray)

data class ItemScore(val itemId: Long, val score: Float)

sealed interface BarcodeResolution {
    /** Un único artículo tiene ese código: resuelto. */
    data class Single(val itemId: Long) : BarcodeResolution

    /** Varios artículos comparten el código: hay que desempatar visualmente. */
    data class Ambiguous(val itemIds: Set<Long>) : BarcodeResolution

    /** Ningún artículo registrado tiene ese código. */
    data object None : BarcodeResolution
}

object EmbeddingMath {
    fun cosine(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size || a.isEmpty()) return 0f
        var dot = 0f
        var na = 0f
        var nb = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            na += a[i] * a[i]
            nb += b[i] * b[i]
        }
        val denom = sqrt(na) * sqrt(nb)
        return if (denom == 0f) 0f else dot / denom
    }

    fun toBytes(v: FloatArray): ByteArray {
        val buf = ByteBuffer.allocate(v.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        v.forEach { buf.putFloat(it) }
        return buf.array()
    }

    fun fromBytes(b: ByteArray): FloatArray {
        val buf = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)
        return FloatArray(b.size / 4) { buf.getFloat() }
    }

    /** Mejor similitud por artículo (máximo entre sus fotos), de mayor a menor. */
    fun rank(query: FloatArray, refs: List<RefVector>): List<ItemScore> =
        refs.groupBy { it.itemId }
            .map { (id, list) -> ItemScore(id, list.maxOf { cosine(query, it.vector) }) }
            .sortedByDescending { it.score }
}

/**
 * Decisión de reconocimiento sin dependencias de Android (probada en tests JVM).
 *
 *  1. El código de barras manda: si es único, resuelve; si lo comparten varios
 *     artículos, el reconocimiento visual se restringe a ellos.
 *  2. El reconocimiento visual exige puntuación mínima, margen frente al segundo
 *     y [RecognitionConfig.STABLE_FRAMES] fotogramas seguidos con el mismo artículo.
 */
class RecognitionSession(
    private val minScore: Float = RecognitionConfig.MIN_SCORE,
    private val minMargin: Float = RecognitionConfig.MIN_MARGIN,
    private val stableFrames: Int = RecognitionConfig.STABLE_FRAMES,
    private val stableBarcodeReads: Int = RecognitionConfig.STABLE_BARCODE_READS
) {
    var restriction: Set<Long>? = null
        private set

    private var lastBarcode: String? = null
    private var barcodeCount = 0
    private var candidate: Long? = null
    private var candidateCount = 0

    fun reset() {
        restriction = null
        lastBarcode = null
        barcodeCount = 0
        candidate = null
        candidateCount = 0
    }

    /** Devuelve el código cuando se ha leído igual [stableBarcodeReads] veces seguidas. */
    fun onBarcodeRead(code: String?): String? {
        if (code == null) return null
        if (code == lastBarcode) barcodeCount++ else {
            lastBarcode = code
            barcodeCount = 1
        }
        return if (barcodeCount >= stableBarcodeReads) code else null
    }

    fun onBarcodeCandidates(itemIds: List<Long>): BarcodeResolution = when {
        itemIds.isEmpty() -> BarcodeResolution.None
        itemIds.size == 1 -> BarcodeResolution.Single(itemIds[0])
        else -> BarcodeResolution.Ambiguous(itemIds.toSet()).also { restriction = it.itemIds }
    }

    /** Devuelve el artículo cuando el resultado visual es fiable y estable. */
    fun onVisualRanking(ranked: List<ItemScore>): Long? {
        val allowed = restriction
        val list = if (allowed == null) ranked else ranked.filter { it.itemId in allowed }
        val best = list.firstOrNull()
        val second = list.getOrNull(1)
        val ok = best != null && best.score >= minScore &&
            (second == null || best.score - second.score >= minMargin)
        if (!ok) {
            candidate = null
            candidateCount = 0
            return null
        }
        if (candidate == best!!.itemId) candidateCount++ else {
            candidate = best.itemId
            candidateCount = 1
        }
        return if (candidateCount >= stableFrames) best.itemId else null
    }
}
