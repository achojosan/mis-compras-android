package app.miscompras.com

import app.miscompras.com.util.StoreLinks
import org.junit.Assert.assertEquals
import org.junit.Test

class StoreLinksTest {
    @Test fun geoWithCoordinatesMarksTheExactPoint() {
        assertEquals("geo:40.4,-3.7?q=40.4,-3.7(El%20Corte%20Ingl%C3%A9s)", StoreLinks.geoUri("El Corte Inglés", 40.4, -3.7))
    }

    @Test fun geoWithoutCoordinatesSearchesByName() {
        assertEquals("geo:0,0?q=Lidl%20supermercado", StoreLinks.geoUri("Lidl", null, null))
    }

    @Test fun webFallback() {
        assertEquals("https://www.google.com/maps/search/?api=1&query=40.4,-3.7", StoreLinks.webUri("Lidl", 40.4, -3.7))
        assertEquals("https://www.google.com/maps/search/?api=1&query=Lidl%20supermercado", StoreLinks.webUri("Lidl", null, null))
    }
}
