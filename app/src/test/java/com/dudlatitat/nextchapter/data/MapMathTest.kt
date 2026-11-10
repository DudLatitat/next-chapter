package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapMathTest {
    @Test fun tilesMatchTheStandardSlippyMapNumbers() {
        // Known tile for central Los Angeles at zoom 10 (OpenStreetMap wiki formula): x 175, y 408.
        assertEquals(175, MapMath.tileX(-118.2437, 10).toInt())
        assertEquals(408, MapMath.tileY(34.0522, 10).toInt())
        assertEquals(0.5, MapMath.tileX(0.0, 0), 1e-12)
        assertEquals(0.5, MapMath.tileY(0.0, 0), 1e-12)
    }

    @Test fun roundTripsBackToTheSamePlace() {
        for ((lat, lon) in listOf(34.0522 to -118.2437, 29.5647 to 103.7614, -33.86 to 151.21)) {
            for (z in listOf(3, 12, 19)) {
                assertEquals(lat, MapMath.lat(MapMath.tileY(lat, z), z), 1e-9)
                assertEquals(lon, MapMath.lon(MapMath.tileX(lon, z), z), 1e-9)
            }
        }
        assertEquals(-179.0, MapMath.wrapLon(181.0), 1e-9)
        assertEquals(179.0, MapMath.wrapLon(-181.0), 1e-9)
    }

    @Test fun readsPastedCoordinates() {
        assertEquals(Spot(34.0522, -118.2437), MapMath.parseCoords("34.0522, -118.2437"))
        assertEquals(Spot(34.0522, -118.2437), MapMath.parseCoords(" 34.0522 -118.2437 "))
        assertNull(MapMath.parseCoords("123 Main St"))
        assertNull(MapMath.parseCoords("95.0, 10.0"))
    }

    @Test fun readsSearchResultsAndShortensNames() {
        val reply = """[{"lat":"34.0500","lon":"-118.2500","display_name":"123, Main Street, Downtown, Los Angeles, California, 90012, United States"},
            {"lat":"bad","lon":"1"}, {"lat":"10","lon":"20","display_name":"City Hall, Spring Street, Los Angeles"}]"""
        val r = MapMath.parseSearch(reply)
        assertEquals(2, r.size)
        assertEquals("123 Main Street, Downtown", r[0].name)
        assertEquals(34.05, r[0].spot.lat, 1e-9)
        assertEquals("City Hall, Spring Street", r[1].name)
        assertTrue(MapMath.parseSearch("not json").isEmpty())
        assertTrue(MapMath.parseSearch("""{"error":"x"}""").isEmpty())
    }
}
