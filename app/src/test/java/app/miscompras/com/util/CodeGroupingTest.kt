package app.miscompras.com.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CodeGroupingTest {
    @Test fun agrupaDeCuatroEnCuatro() {
        assertEquals("8412 3456 7890 5", groupCode("8412345678905"))
    }

    @Test fun noTocaUnCodigoConLetrasNiUnQrLargo() {
        assertEquals("AB12345", groupCode("AB12345"))
        val qr = "1".repeat(40)
        assertEquals(qr, groupCode(qr))
    }

    @Test fun codigoCortoSeQuedaIgual() {
        assertEquals("1234", groupCode("1234"))
    }
}
