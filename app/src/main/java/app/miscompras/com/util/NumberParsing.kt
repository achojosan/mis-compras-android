package app.miscompras.com.util

/**
 * Convierte un texto introducido por el usuario a [Double] aceptando tanto la
 * coma como el punto como separador decimal (los teclados en español suelen
 * usar la coma).
 *
 * Admite también el formato con separador de millares, p. ej. "1.234,56".
 */
fun String.toDoubleOrNullLocalized(): Double? {
    val trimmed = trim()
    if (trimmed.isEmpty()) return null

    val normalized = when {
        trimmed.contains(',') && trimmed.contains('.') ->
            // "1.234,56" -> quitar los puntos de millar y usar la coma decimal
            trimmed.replace(".", "").replace(',', '.')
        else ->
            trimmed.replace(',', '.')
    }

    return normalized.toDoubleOrNull()
}
