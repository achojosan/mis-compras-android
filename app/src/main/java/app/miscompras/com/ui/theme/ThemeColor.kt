package app.miscompras.com.ui.theme

import androidx.compose.ui.graphics.Color

enum class ThemeColor(val value: Int, val displayName: String, val previewColor: Color) {
    Green(0, "Verde", Color(0xFF2E7D32)),
    Blue(1, "Azul", Color(0xFF1976D2)),
    Purple(2, "Púrpura", Color(0xFF7B1FA2)),
    Orange(3, "Naranja", Color(0xFFE64A19)),
    Pink(4, "Rosa", Color(0xFFC2185B)),
    Gray(5, "Gris", Color(0xFF455A64)),
    Red(6, "Rojo", Color(0xFFD32F2F));

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: Green
    }
}
