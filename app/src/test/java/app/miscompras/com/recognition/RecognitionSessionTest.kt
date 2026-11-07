package app.miscompras.com.recognition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecognitionSessionTest {

    private fun v(vararg x: Float) = x

    @Test
    fun cosine_identicalVectorsIsOne_orthogonalIsZero() {
        assertEquals(1f, EmbeddingMath.cosine(v(1f, 2f), v(2f, 4f)), 1e-5f)
        assertEquals(0f, EmbeddingMath.cosine(v(1f, 0f), v(0f, 1f)), 1e-5f)
    }

    @Test
    fun bytesRoundTrip() {
        val original = v(0.1f, -2.5f, 3f)
        assertTrue(original.contentEquals(EmbeddingMath.fromBytes(EmbeddingMath.toBytes(original))))
    }

    @Test
    fun rank_usesBestReferencePerItem() {
        val refs = listOf(RefVector(1, v(1f, 0f)), RefVector(1, v(0f, 1f)), RefVector(2, v(1f, 1f)))
        val ranked = EmbeddingMath.rank(v(0f, 1f), refs)
        assertEquals(1L, ranked[0].itemId)
        assertEquals(1f, ranked[0].score, 1e-5f)
        assertEquals(2, ranked.size)
    }

    @Test
    fun barcode_needsTwoEqualReads() {
        val s = RecognitionSession()
        assertNull(s.onBarcodeRead("123"))
        assertEquals("123", s.onBarcodeRead("123"))
    }

    @Test
    fun barcode_differentCodeResetsCount() {
        val s = RecognitionSession()
        s.onBarcodeRead("123")
        assertNull(s.onBarcodeRead("456"))
        assertEquals("456", s.onBarcodeRead("456"))
    }

    @Test
    fun barcodeResolution() {
        val s = RecognitionSession()
        assertEquals(BarcodeResolution.None, s.onBarcodeCandidates(emptyList()))
        assertEquals(BarcodeResolution.Single(7), s.onBarcodeCandidates(listOf(7)))
        assertNull(s.restriction)
        assertEquals(BarcodeResolution.Ambiguous(setOf(1, 2)), s.onBarcodeCandidates(listOf(1, 2)))
        assertEquals(setOf(1L, 2L), s.restriction)
    }

    @Test
    fun visual_requiresStabilityAcrossFrames() {
        val s = RecognitionSession()
        val ranked = listOf(ItemScore(1, 0.95f), ItemScore(2, 0.60f))
        assertNull(s.onVisualRanking(ranked))
        assertNull(s.onVisualRanking(ranked))
        assertEquals(1L, s.onVisualRanking(ranked))
    }

    @Test
    fun visual_rejectsLowScore() {
        val s = RecognitionSession()
        repeat(5) { assertNull(s.onVisualRanking(listOf(ItemScore(1, 0.70f)))) }
    }

    @Test
    fun visual_rejectsSmallMargin() {
        val s = RecognitionSession()
        repeat(5) { assertNull(s.onVisualRanking(listOf(ItemScore(1, 0.90f), ItemScore(2, 0.88f)))) }
    }

    @Test
    fun visual_changingBestItemResetsStability() {
        val s = RecognitionSession()
        val a = listOf(ItemScore(1, 0.95f), ItemScore(2, 0.5f))
        val b = listOf(ItemScore(2, 0.95f), ItemScore(1, 0.5f))
        assertNull(s.onVisualRanking(a))
        assertNull(s.onVisualRanking(a))
        assertNull(s.onVisualRanking(b))
        assertNull(s.onVisualRanking(b))
        assertEquals(2L, s.onVisualRanking(b))
    }

    @Test
    fun visual_restrictedToAmbiguousBarcodeCandidates() {
        val s = RecognitionSession()
        s.onBarcodeCandidates(listOf(2, 3))
        // El artículo 1 es el más parecido, pero no comparte el código: se ignora.
        val ranked = listOf(ItemScore(1, 0.99f), ItemScore(2, 0.90f), ItemScore(3, 0.50f))
        repeat(2) { assertNull(s.onVisualRanking(ranked)) }
        assertEquals(2L, s.onVisualRanking(ranked))
    }

    @Test
    fun reset_clearsState() {
        val s = RecognitionSession()
        s.onBarcodeCandidates(listOf(1, 2))
        s.reset()
        assertNull(s.restriction)
    }
}
