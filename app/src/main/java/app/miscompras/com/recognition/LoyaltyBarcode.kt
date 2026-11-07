package app.miscompras.com.recognition

import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

/**
 * Vuelve a dibujar el código de una tarjeta de cliente. ML Kit solo LEE códigos; para mostrarlos
 * en pantalla se generan con ZXing en el mismo formato con el que se leyeron.
 */
object LoyaltyBarcode {
    fun toZxing(mlkitFormat: Int): BarcodeFormat? = when (mlkitFormat) {
        Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
        Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
        Barcode.FORMAT_CODE_93 -> BarcodeFormat.CODE_93
        Barcode.FORMAT_CODABAR -> BarcodeFormat.CODABAR
        Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
        Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
        Barcode.FORMAT_ITF -> BarcodeFormat.ITF
        Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
        Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
        Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
        Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF_417
        Barcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
        else -> null
    }

    fun isTwoDimensional(format: BarcodeFormat): Boolean = format in setOf(
        BarcodeFormat.QR_CODE, BarcodeFormat.DATA_MATRIX, BarcodeFormat.AZTEC
    )

    /** Formato probable para un número escrito a mano (sin saber con qué se leería). */
    fun guessFormat(code: String): Int = when {
        code.all { it.isDigit() } && code.length == 13 -> Barcode.FORMAT_EAN_13
        code.all { it.isDigit() } && code.length == 12 -> Barcode.FORMAT_UPC_A
        code.all { it.isDigit() } && code.length == 8 -> Barcode.FORMAT_EAN_8
        else -> Barcode.FORMAT_CODE_128
    }

    /**
     * Matriz de bits lista para pintar, o `null` si el código no se puede dibujar en ese formato
     * (p. ej. dígito de control EAN incorrecto): en ese caso se reintenta como Code 128.
     */
    fun encode(code: String, mlkitFormat: Int?): BitMatrix? {
        val preferred = mlkitFormat?.let(::toZxing) ?: toZxing(guessFormat(code))!!
        return tryEncode(code, preferred) ?: tryEncode(code, BarcodeFormat.CODE_128)
    }

    private fun tryEncode(code: String, format: BarcodeFormat): BitMatrix? = try {
        val (w, h) = if (isTwoDimensional(format)) 800 to 800 else 1200 to 360
        MultiFormatWriter().encode(code, format, w, h)
    } catch (_: Exception) {
        null
    }
}
