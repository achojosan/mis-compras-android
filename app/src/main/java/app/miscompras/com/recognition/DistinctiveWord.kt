package app.miscompras.com.recognition

private val separators = listOf(" · ", " - ", " – ", " — ", ": ")

/**
 * Parte del nombre que distingue a un artículo de otros casi idénticos:
 * «Lata gato · Salmón» → «Salmón». Sin separador, el nombre entero.
 */
fun distinctiveWord(name: String): String {
    val trimmed = name.trim()
    val cut = separators
        .map { trimmed.lastIndexOf(it) to it.length }
        .filter { it.first >= 0 }
        .maxByOrNull { it.first }
        ?: return trimmed
    val tail = trimmed.substring(cut.first + cut.second).trim()
    return tail.ifEmpty { trimmed }
}

/** Parte común del nombre («Lata gato»), o `null` si el nombre no se divide. */
fun commonPart(name: String): String? {
    val trimmed = name.trim()
    val word = distinctiveWord(trimmed)
    if (word == trimmed) return null
    return trimmed.substring(0, trimmed.length - word.length).trimEnd(' ', '·', '-', '–', '—', ':').ifEmpty { null }
}
