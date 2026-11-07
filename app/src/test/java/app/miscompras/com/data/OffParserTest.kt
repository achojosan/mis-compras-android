package app.miscompras.com.data

import app.miscompras.com.data.remote.off.OffParser
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OffParserTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private fun res(name: String) = javaClass.getResource(name)!!.readText()

    @Test fun parsesKnownProduct() {
        val p = OffParser.parse(res("/off_nutella.json"), json)!!
        assertEquals("Nutella", p.name)
        assertEquals("Nutella", p.brand)
        assertEquals("400 g e", p.quantity)
        assertNull(p.nutriScore) // "unknown" no es una nota válida
        assertEquals("Marca: Nutella. Cantidad: 400 g e", p.notes())
    }

    @Test fun unknownProductIsNull() {
        assertNull(OffParser.parse(res("/off_unknown.json"), json))
    }

    @Test fun prefersSpanishNameAndKeepsValidNutriScore() {
        val body = """{"status":1,"product":{"product_name":"Cookies","product_name_es":"Galletas","brands":"Marca X, Otra","nutriscore_grade":"c"}}"""
        val p = OffParser.parse(body, json)!!
        assertEquals("Galletas", p.name)
        assertEquals("Marca X", p.brand)
        assertEquals("C", p.nutriScore?.uppercase())
    }
}
