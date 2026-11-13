package app.miscompras.com.nearby

import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Intérprete del subconjunto habitual de `opening_hours` de OpenStreetMap:
 * `24/7`, rangos y listas de días (`Mo-Fr`, `Sa,Su`), varios tramos (`09:00-14:00,17:00-20:30`),
 * `off` y cierres pasada la medianoche (`18:00-02:00`). Los festivos (`PH`/`SH`) se ignoran.
 *
 * Si encuentra algo que no entiende (semanas, meses, `sunrise`…) [parse] devuelve `null`
 * y la pantalla muestra el texto original en vez de arriesgarse a decir "abierto" por error.
 */
class Schedule internal constructor(private val days: List<List<IntRange>>) {
    // Cada tramo en minutos desde las 00:00 del día; el fin puede pasar de 1440 (cierre tras medianoche).

    fun isOpen(now: LocalDateTime): Boolean {
        val d = now.dayOfWeek.value - 1
        val m = now.hour * 60 + now.minute
        val today = days[d].any { m in it.first until it.last }
        val prev = days[(d + 6) % 7].any { it.last > 1440 && (m + 1440) in it.first until it.last }
        return today || prev
    }

    /** "09:00–14:00, 17:00–20:30", o `null` si ese día está cerrado. */
    fun hoursText(day: DayOfWeek): String? {
        val intervals = days[day.value - 1]
        if (intervals.isEmpty()) return null
        return intervals.joinToString(", ") { "${fmt(it.first)}–${fmt(it.last % 1440).let { e -> if (it.last == 1440) "24:00" else e }}" }
    }

    /**
     * Estado corto para la cartera de tiendas: «Abierto · cierra 22:00»,
     * «Cerrado · abre 09:00», «Cerrado · abre mañana 09:00» o «Abierto 24 horas».
     */
    fun statusText(now: LocalDateTime): String {
        val d = now.dayOfWeek.value - 1
        val m = now.hour * 60 + now.minute
        val current = days[d].firstOrNull { m in it.first until it.last }
        val carried = days[(d + 6) % 7].firstOrNull { it.last > 1440 && (m + 1440) in it.first until it.last }
        if (current != null || carried != null) {
            if (days.all { it.size == 1 && it[0].first == 0 && it[0].last >= 1440 }) return "Abierto 24 horas"
            val end = if (current != null) current.last else carried!!.last - 1440
            return "Abierto · cierra ${fmt(end % 1440)}"
        }
        days[d].filter { it.first > m }.minByOrNull { it.first }?.let {
            return "Cerrado · abre ${fmt(it.first)}"
        }
        for (offset in 1..7) {
            val first = days[(d + offset) % 7].minByOrNull { it.first } ?: continue
            val whenText = if (offset == 1) "mañana" else dayLabels[(d + offset) % 7]
            return "Cerrado · abre $whenText ${fmt(first.first)}"
        }
        return "Cerrado"
    }

    private val dayLabels = listOf("el lunes", "el martes", "el miércoles", "el jueves", "el viernes", "el sábado", "el domingo")

    private fun fmt(minutes: Int) = "%02d:%02d".format(minutes / 60, minutes % 60)
}

object OpeningHours {
    private val dayNames = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
    private val daySelector = Regex("^(Mo|Tu|We|Th|Fr|Sa|Su|PH|SH)([-,](Mo|Tu|We|Th|Fr|Sa|Su|PH|SH))*$")
    private val interval = Regex("^(\\d{1,2}):(\\d{2})-(\\d{1,2}):(\\d{2})$")

    fun parse(raw: String?): Schedule? {
        if (raw.isNullOrBlank()) return null
        val days = MutableList(7) { emptyList<IntRange>() }
        for (ruleRaw in raw.split(';')) {
            val rule = ruleRaw.trim().replace(Regex(",\\s+"), ",")
            if (rule.isEmpty()) continue
            if (rule == "24/7") {
                for (i in 0..6) days[i] = listOf(0..1440)
                continue
            }
            val tokens = rule.split(Regex("\\s+"))
            var selector: String? = null
            var timesTokens = tokens
            if (daySelector.matches(tokens[0])) {
                selector = tokens[0]
                timesTokens = tokens.drop(1)
            }
            val coveredDays = if (selector == null) (0..6).toSet() else (daysOf(selector) ?: return null)
            if (selector != null && coveredDays.isEmpty()) continue // solo festivos: se ignora
            val times = timesTokens.joinToString("")
            val intervals: List<IntRange> = when {
                times.isEmpty() -> return null
                times == "off" || times == "closed" -> emptyList()
                else -> times.split(',').map { parseInterval(it) ?: return null }
            }
            coveredDays.forEach { days[it] = intervals }
        }
        return Schedule(days)
    }

    private fun parseInterval(text: String): IntRange? {
        val m = interval.find(text) ?: return null
        val (h1, m1, h2, m2) = m.destructured
        val start = h1.toInt() * 60 + m1.toInt()
        var end = h2.toInt() * 60 + m2.toInt()
        if (h1.toInt() > 24 || h2.toInt() > 24 || m1.toInt() > 59 || m2.toInt() > 59) return null
        if (end <= start) end += 1440 // cierra después de medianoche
        return start..end
    }

    /** Índices 0..6 (lunes..domingo). `null` si el selector no es válido; vacío si solo hay festivos. */
    private fun daysOf(selector: String): Set<Int>? {
        val result = mutableSetOf<Int>()
        for (part in selector.split(',')) {
            if (part == "PH" || part == "SH") continue
            val ends = part.split('-')
            val a = dayNames.indexOf(ends[0])
            if (a < 0) return null
            if (ends.size == 1) { result += a; continue }
            val b = dayNames.indexOf(ends[1])
            if (b < 0) return null
            var i = a
            while (true) { result += i; if (i == b) break; i = (i + 1) % 7 }
        }
        return result
    }
}
