package app.miscompras.com.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PassPaletteTest {
    @Test fun todasLasParejasCumplenContraste45() {
        for (c in PassPalette.all) {
            assertTrue("${c.name} claro", contrastRatio(c.light, c.onLight) >= 4.5f)
            assertTrue("${c.name} oscuro", contrastRatio(c.dark, c.onDark) >= 4.5f)
        }
    }

    @Test fun colorMasCercanoDeUnLogo() {
        assertEquals(0, PassPalette.nearestId(0xFF2E7D32.toInt())) // verde Mercadona
        assertEquals(1, PassPalette.nearestId(0xFF1A4C9C.toInt())) // azul Aldi
        assertEquals(3, PassPalette.nearestId(0xFFF5C518.toInt())) // amarillo
    }

    @Test fun elPorDefectoEsEstableYEstaEnRango() {
        assertEquals(PassPalette.defaultFor(7), PassPalette.defaultFor(7))
        assertTrue(PassPalette.defaultFor(-3).id in 0..4)
    }

    @Test fun idDesconocidoCaeAlPorDefecto() {
        assertEquals(PassPalette.defaultFor(2).id, PassPalette.byId(99, 2).id)
    }
}
