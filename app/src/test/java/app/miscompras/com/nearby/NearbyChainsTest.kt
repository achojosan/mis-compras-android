package app.miscompras.com.nearby

import app.miscompras.com.data.remote.osm.OsmResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NearbyChainsTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val elements = json.decodeFromString(
        OsmResponse.serializer(),
        javaClass.getResource("/overpass_madrid.json")!!.readText()
    ).elements

    @Test fun chainMatching() {
        assertEquals("Dia", Chains.match("Dia Market")?.name)
        assertEquals("Dia", Chains.match(null, "Supermercados Dia")?.name)
        assertEquals("Carrefour", Chains.match("Carrefour Express")?.name)
        assertEquals("Alcampo", Chains.match("Mi Alcampo")?.name)
        assertEquals("El Corte Inglés", Chains.match("El Corte Inglés")?.name)
        assertNull(Chains.match("Frutas del Día"))
        assertNull(Chains.match("Alimentación"))
        assertNull(Chains.match(null, null))
    }

    @Test fun realFixtureHasWaysWithCenterCoordinates() {
        val ways = elements.filter { it.type == "way" }
        assertTrue(ways.isNotEmpty())
        assertTrue(ways.all { it.latitude != null && it.longitude != null })
    }

    @Test fun picksOneNearestBranchPerChainSortedByDistance() {
        val picked = NearbyChains.pick(elements, 40.4168, -3.7038)
        assertTrue(picked.isNotEmpty())
        assertEquals(picked.size, picked.map { it.chainName }.toSet().size)
        assertEquals(picked.sortedBy { it.distanceMeters }, picked)
        assertNotNull(picked.firstOrNull { it.chainName == "Dia" })
        assertNotNull(picked.firstOrNull { it.chainName == "Carrefour" })
        assertTrue(picked.all { it.distanceMeters < 1500 }) // el radio del fixture es 1200 m
    }

    @Test fun distanceAndFormat() {
        // Puerta del Sol -> Plaza Mayor ~ 250 m
        val d = NearbyChains.distanceMeters(40.4168, -3.7038, 40.4155, -3.7074)
        assertTrue(d in 250.0..450.0)
        assertEquals("350 m", NearbyChains.formatDistance(354.0))
        assertEquals("1,2 km", NearbyChains.formatDistance(1234.0))
    }
}
