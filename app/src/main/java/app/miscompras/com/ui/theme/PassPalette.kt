package app.miscompras.com.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

/**
 * Paleta de «pases» de la cartera: el color propio de cada tienda y de cada artículo.
 *
 * Se guarda solo el [id] (índice) en la base de datos, no el color: así cada pase tiene
 * su variante clara, su variante oscura y, en alto contraste, se sustituye por la
 * superficie del tema. No toca `primary`, que sigue siendo el color de tema que elige
 * la persona usuaria para botones, interruptores y foco.
 */
data class PassColor(
    val id: Int,
    val name: String,
    val light: Color,
    val onLight: Color,
    val dark: Color,
    val onDark: Color
)

object PassPalette {
    val all: List<PassColor> = listOf(
        PassColor(0, "Verde", Color(0xFF145A29), Color.White, Color(0xFF1F7A38), Color.White),
        PassColor(1, "Azul", Color(0xFF1E3A8A), Color.White, Color(0xFF3552B8), Color.White),
        PassColor(2, "Terracota", Color(0xFFA93C1A), Color.White, Color(0xFFC2501F), Color.White),
        PassColor(3, "Ámbar", Color(0xFFFFC83D), Color(0xFF1B1405), Color(0xFFFFC83D), Color(0xFF1B1405)),
        PassColor(4, "Tinta", Color(0xFF16221B), Color.White, Color(0xFF2B3C32), Color.White)
    )

    /** Pase de una tienda sin color elegido: estable según su id. */
    fun defaultFor(key: Long): PassColor = all[Math.floorMod(key, all.size.toLong()).toInt()]

    fun byId(id: Int?, fallbackKey: Long = 0L): PassColor =
        all.firstOrNull { it.id == id } ?: defaultFor(fallbackKey)

    /** Color de la paleta más cercano a [argb] (por ejemplo, el dominante de un logo). */
    fun nearestId(argb: Int): Int {
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        return all.minBy { c ->
            val cr = (c.light.red * 255).toInt()
            val cg = (c.light.green * 255).toInt()
            val cb = (c.light.blue * 255).toInt()
            val dr = r - cr; val dg = g - cg; val db = b - cb
            // Pesos perceptuales sencillos (el ojo distingue más el verde).
            2 * dr * dr + 4 * dg * dg + 3 * db * db
        }.id
    }
}

/** Relación de contraste WCAG entre dos colores. */
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)
}

/** Colores ya resueltos para pintar un pase en el tema actual. */
data class ResolvedPass(
    val container: Color,
    val content: Color,
    /** Solo en alto contraste: el pase se reduce a superficie con borde de 2 dp. */
    val outlined: Boolean
)

@Composable
fun resolvePass(pass: PassColor): ResolvedPass {
    val scheme = MaterialTheme.colorScheme
    if (LocalAccessibilityConfig.current.highContrast) {
        return ResolvedPass(scheme.surface, scheme.onSurface, outlined = true)
    }
    val isDark = scheme.background.luminance() < 0.5f
    return if (isDark) ResolvedPass(pass.dark, pass.onDark, false)
    else ResolvedPass(pass.light, pass.onLight, false)
}
