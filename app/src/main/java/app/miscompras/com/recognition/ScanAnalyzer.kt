package app.miscompras.com.recognition

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlin.math.min

/**
 * Analizador de fotogramas compartido por reconocimiento y registro, de modo que
 * ambos usan EXACTAMENTE el mismo preprocesado (rotación + recorte central):
 * si no, las similitudes entre fotos registradas y fotogramas en vivo no serían comparables.
 *
 * Por cada fotograma entrega el código de barras leído (si lo hay) y, como mucho cada
 * [cropIntervalMs], el recorte cuadrado central (la zona del marco guía de la pantalla).
 * Los callbacks llegan en el hilo principal.
 */
class ScanAnalyzer(
    private val cropIntervalMs: Long = 400L,
    formats: IntArray = PRODUCT_FORMATS,
    /** Formato ML Kit del código recién leído (se llama justo antes de [onFrame]). */
    private val onFormat: ((Int) -> Unit)? = null,
    private val onFrame: (barcode: String?, crop: Bitmap?) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(formats.first(), *formats.drop(1).toIntArray())
            .build()
    )
    private var lastCropAt = 0L

    @ExperimentalGetImage
    override fun analyze(proxy: ImageProxy) {
        val media = proxy.image
        if (media == null) {
            proxy.close()
            return
        }
        val now = SystemClock.elapsedRealtime()
        val crop = if (now - lastCropAt >= cropIntervalMs) {
            lastCropAt = now
            runCatching { centerCrop(proxy) }.getOrNull()
        } else null

        val input = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { codes ->
                val hit = codes.firstOrNull { it.rawValue != null }
                if (hit != null) onFormat?.invoke(hit.format)
                onFrame(hit?.rawValue, crop)
            }
            .addOnFailureListener { onFrame(null, crop) }
            // El proxy se cierra solo cuando ML Kit ha terminado con el fotograma.
            .addOnCompleteListener { proxy.close() }
    }

    fun close() = scanner.close()

    private fun centerCrop(proxy: ImageProxy): Bitmap {
        val raw = proxy.toBitmap()
        val rotation = proxy.imageInfo.rotationDegrees
        val rotated = if (rotation != 0) {
            Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, Matrix().apply { postRotate(rotation.toFloat()) }, true)
        } else raw
        val side = (min(rotated.width, rotated.height) * GUIDE_FRACTION).toInt()
        val x = (rotated.width - side) / 2
        val y = (rotated.height - side) / 2
        val square = Bitmap.createBitmap(rotated, x, y, side, side)
        return if (side > MAX_SIDE) Bitmap.createScaledBitmap(square, MAX_SIDE, MAX_SIDE, true) else square
    }

    companion object {
        /** Fracción del lado menor que ocupa el marco guía (debe coincidir con la UI). */
        const val GUIDE_FRACTION = 0.7f
        private const val MAX_SIDE = 512

        /** Códigos de productos (lo que se registra al reconocer artículos). */
        val PRODUCT_FORMATS = intArrayOf(
            Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E
        )

        /** Tarjetas de cliente: además de EAN/UPC suelen usar Code 128, QR o PDF417. */
        val CARD_FORMATS = intArrayOf(
            Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39, Barcode.FORMAT_CODE_93, Barcode.FORMAT_ITF,
            Barcode.FORMAT_CODABAR, Barcode.FORMAT_QR_CODE, Barcode.FORMAT_PDF417,
            Barcode.FORMAT_DATA_MATRIX, Barcode.FORMAT_AZTEC
        )
    }
}
