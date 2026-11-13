package app.miscompras.com.recognition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DistinctiveWordTest {
    @Test fun separaPorPuntoMedio() {
        assertEquals("Salmón", distinctiveWord("Lata gato · Salmón"))
        assertEquals("Lata gato", commonPart("Lata gato · Salmón"))
    }

    @Test fun separaPorGuion() {
        assertEquals("Pollo", distinctiveWord("Lata gato - Pollo"))
    }

    @Test fun sinSeparadorDevuelveElNombre() {
        assertEquals("Leche entera", distinctiveWord("Leche entera"))
        assertNull(commonPart("Leche entera"))
    }

    @Test fun elGuionDeUnaPalabraNoSeparaNada() {
        assertEquals("Coca-Cola", distinctiveWord("Coca-Cola"))
    }
}
