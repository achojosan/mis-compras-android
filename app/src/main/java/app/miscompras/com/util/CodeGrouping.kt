package app.miscompras.com.util

/**
 * Agrupa un número de tarjeta en bloques de 4 para leerlo (y teclearlo) mejor:
 * «8412345678905» → «8412 3456 7890 5». Solo si es numérico y de longitud razonable;
 * un QR o un código con letras se deja tal cual.
 */
fun groupCode(code: String): String {
    val trimmed = code.trim()
    if (trimmed.length !in 5..24 || !trimmed.all { it.isDigit() }) return trimmed
    return trimmed.chunked(4).joinToString(" ")
}
