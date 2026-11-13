package app.miscompras.com.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityScaleTest {
    @Test fun sistemaGrandeYAppNormalRespetaElSistema() {
        assertEquals(1.3f, effectiveFontScale(1.3f, 1.0f), 0.001f)
    }

    @Test fun sistemaYAppSeMultiplicanConTope() {
        assertEquals(MAX_EFFECTIVE_FONT_SCALE, effectiveFontScale(2.0f, 1.4f), 0.001f)
        assertEquals(1.4f * 1.2f, effectiveFontScale(1.2f, 1.4f), 0.001f)
    }

    @Test fun umbralDeTextoGrande() {
        assertFalse(AccessibilityConfig(fontScale = 1.2f).isLargeText)
        assertTrue(AccessibilityConfig(fontScale = 1.3f).isLargeText)
    }
}
