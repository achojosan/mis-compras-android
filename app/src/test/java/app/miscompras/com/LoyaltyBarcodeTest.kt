package app.miscompras.com

import app.miscompras.com.recognition.LoyaltyBarcode
import com.google.mlkit.vision.barcode.common.Barcode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoyaltyBarcodeTest {
    @Test fun guessFormatFromTypedNumber() {
        assertEquals(Barcode.FORMAT_EAN_13, LoyaltyBarcode.guessFormat("8410000000008"))
        assertEquals(Barcode.FORMAT_UPC_A, LoyaltyBarcode.guessFormat("123456789012"))
        assertEquals(Barcode.FORMAT_EAN_8, LoyaltyBarcode.guessFormat("12345670"))
        assertEquals(Barcode.FORMAT_CODE_128, LoyaltyBarcode.guessFormat("ABC-123"))
    }

    @Test fun rendersValidEan13() {
        val m = LoyaltyBarcode.encode("5901234123457", Barcode.FORMAT_EAN_13)
        assertNotNull(m)
        assertEquals(1200, m!!.width)
    }

    @Test fun badEanChecksumFallsBackToCode128() {
        val m = LoyaltyBarcode.encode("5901234123458", Barcode.FORMAT_EAN_13) // dígito de control incorrecto
        assertNotNull(m)
    }

    @Test fun rendersQrAndCode128() {
        assertEquals(800, LoyaltyBarcode.encode("https://ejemplo.es/tarjeta/123", Barcode.FORMAT_QR_CODE)!!.width)
        assertTrue(LoyaltyBarcode.encode("ES-12345-6789", Barcode.FORMAT_CODE_128)!!.height > 0)
    }

    @Test fun unknownFormatUsesGuess() {
        assertNotNull(LoyaltyBarcode.encode("12345678", null))
    }
}
