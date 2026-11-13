package app.miscompras.com.nearby

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class OpeningHoursTest {
    // 2026-11-02 es lunes.
    private fun at(day: Int, h: Int, m: Int = 0) = LocalDateTime.of(2026, 11, 1 + day, h, m) // day: 1=lunes..7=domingo

    private fun open(raw: String, day: Int, h: Int, m: Int = 0) = OpeningHours.parse(raw)!!.isOpen(at(day, h, m))

    @Test fun simpleRange() {
        assertTrue(open("Mo-Fr 09:00-20:00", 1, 10))
        assertFalse(open("Mo-Fr 09:00-20:00", 1, 8, 59))
        assertFalse(open("Mo-Fr 09:00-20:00", 1, 20))
    }

    @Test fun daysNotMentionedAreClosed() {
        assertFalse(open("Mo-Fr 09:00-20:00", 6, 12))
    }

    @Test fun moFrDoesNotApplyToSunday() { // el parser antiguo daba abierto cualquier día con "Mo-Fr"
        assertFalse(open("Mo-Fr 09:00-20:00; Sa 10:00-14:00", 7, 11))
    }

    @Test fun multipleIntervals() {
        val raw = "Mo-Fr 10:30-14:30,17:30-20:30; Sa 10:30-14:00"
        assertTrue(open(raw, 2, 11))
        assertFalse(open(raw, 2, 15))
        assertTrue(open(raw, 2, 18))
        assertTrue(open(raw, 6, 13))
        assertFalse(open(raw, 6, 17))
    }

    @Test fun spaceAfterCommaInIntervals() {
        assertTrue(open("Mo-Fr 09:00-14:00, 17:00-20:30", 3, 18))
    }

    @Test fun laterRuleOverridesSameDays() {
        assertFalse(open("Mo-Su 09:00-22:00; Su off", 7, 12))
        assertTrue(open("Mo-Su 09:00-22:00; Su off", 3, 12))
    }

    @Test fun twentyFourSeven() {
        assertTrue(open("24/7", 7, 3))
    }

    @Test fun closesAfterMidnight() {
        val raw = "Mo-Su 18:00-02:00"
        assertTrue(open(raw, 1, 23))
        assertTrue(open(raw, 2, 1)) // martes 01:00: sigue abierto del lunes
        assertFalse(open(raw, 2, 3))
    }

    @Test fun publicHolidayRulesAreIgnored() {
        assertTrue(open("Mo-Sa 09:00-22:00; Su off; PH off", 1, 12))
    }

    @Test fun unsupportedSyntaxReturnsNull() {
        assertNull(OpeningHours.parse("Mo-Fr sunrise-sunset"))
        assertNull(OpeningHours.parse("Jan-Mar Mo-Fr 09:00-20:00"))
        assertNull(OpeningHours.parse("wxyz"))
        assertNull(OpeningHours.parse(null))
        assertNull(OpeningHours.parse("  "))
    }

    @Test fun hoursText() {
        val s = OpeningHours.parse("Mo-Fr 09:00-14:00,17:00-20:30; Sa 09:00-24:00")!!
        assertEquals("09:00–14:00, 17:00–20:30", s.hoursText(DayOfWeek.MONDAY))
        assertEquals("09:00–24:00", s.hoursText(DayOfWeek.SATURDAY))
        assertNull(s.hoursText(DayOfWeek.SUNDAY))
    }

    @Test fun allHoursInRealMadridFixtureAreHandled() {
        // Ninguna cadena de horarios real debe lanzar excepción; las no soportadas devuelven null.
        val text = javaClass.getResource("/overpass_madrid.json")!!.readText()
        Regex("\"opening_hours\":\\s*\"([^\"]+)\"").findAll(text).forEach {
            val s = OpeningHours.parse(it.groupValues[1])
            if (s != null) s.isOpen(at(3, 12))
        }
        assertNotNull(OpeningHours.parse("Mo-Sa 09:00-22:00; Su 10:00-16:00"))
    }

    private fun status(raw: String, day: Int, h: Int, m: Int = 0) = OpeningHours.parse(raw)!!.statusText(at(day, h, m))

    @Test fun statusAbiertoMuestraCuandoCierra() {
        assertEquals("Abierto · cierra 20:00", status("Mo-Fr 09:00-20:00", 1, 10))
    }

    @Test fun statusCerradoMuestraCuandoAbreHoy() {
        assertEquals("Cerrado · abre 09:00", status("Mo-Fr 09:00-20:00", 1, 8))
    }

    @Test fun statusCerradoMananaYDiaConcreto() {
        assertEquals("Cerrado · abre mañana 09:00", status("Mo-Fr 09:00-20:00", 1, 21))
        assertEquals("Cerrado · abre el lunes 09:00", status("Mo-Fr 09:00-20:00", 6, 12))
    }

    @Test fun statusCierreTrasMedianoche() {
        assertEquals("Abierto · cierra 02:00", status("Mo-Su 18:00-02:00", 2, 1))
    }

    @Test fun status24Horas() {
        assertEquals("Abierto 24 horas", status("24/7", 3, 5))
    }
}
