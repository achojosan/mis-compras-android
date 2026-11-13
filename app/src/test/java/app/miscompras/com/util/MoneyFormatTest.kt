package app.miscompras.com.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatTest {
    private fun String.plain() = replace(' ', ' ')

    @Test fun eurosUsanComaYSimbolo() {
        assertEquals("10,68 €", formatEuros(10.68).plain())
        assertEquals("21,36 €", formatEuros(21.36).plain())
    }

    @Test fun cantidadSinCerosSobrantes() {
        assertEquals("12", formatQuantity(12.0))
        assertEquals("1,5", formatQuantity(1.5))
    }

    @Test fun unidadEnSingularConUno() {
        assertEquals("1 unidad", quantityLabel(1.0, "unidades"))
        assertEquals("2 unidades", quantityLabel(2.0, "unidades"))
        assertEquals("1,5 kg", quantityLabel(1.5, "kg"))
    }
}
