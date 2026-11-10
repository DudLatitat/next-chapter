package com.dudlatitat.nextchapter.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sinh
import kotlin.math.tan

/**
 * The math behind the small map in Settings → Work & home (OpenStreetMap's standard "slippy map"
 * tiles, Web Mercator), plus reading addresses back from OpenStreetMap's search. Pure, tested.
 * Positions are in tile units: at zoom z the world is 2^z tiles across.
 */
object MapMath {
    const val MIN_ZOOM = 3
    const val MAX_ZOOM = 19

    fun tileX(lon: Double, z: Int): Double = (lon + 180.0) / 360.0 * 2.0.pow(z)

    fun tileY(lat: Double, z: Int): Double {
        val r = Math.toRadians(lat.coerceIn(-85.0511, 85.0511))
        return (1.0 - ln(tan(r) + 1.0 / cos(r)) / PI) / 2.0 * 2.0.pow(z)
    }

    fun lon(tileX: Double, z: Int): Double = tileX / 2.0.pow(z) * 360.0 - 180.0

    fun lat(tileY: Double, z: Int): Double = Math.toDegrees(atan(sinh(PI * (1.0 - 2.0 * tileY / 2.0.pow(z)))))

    /** Wraps a longitude back into -180…180 after panning past the date line. */
    fun wrapLon(lon: Double): Double = ((lon + 180.0) % 360.0 + 360.0) % 360.0 - 180.0

    /** "34.0522, -118.2437" (or with a space, or "34.0522 -118.2437") → a spot; null if it isn't coordinates. */
    fun parseCoords(text: String): Spot? {
        val m = Regex("""^\s*(-?\d{1,2}(?:\.\d+)?)\s*[, ]\s*(-?\d{1,3}(?:\.\d+)?)\s*$""").find(text) ?: return null
        val lat = m.groupValues[1].toDouble()
        val lon = m.groupValues[2].toDouble()
        return if (lat in -90.0..90.0 && lon in -180.0..180.0) Spot(lat, lon) else null
    }

    /** A search result: a short name for the list, and where it is. */
    data class Place(val name: String, val spot: Spot)

    /** Reads OpenStreetMap's (Nominatim) search reply; bad or empty replies give an empty list. */
    fun parseSearch(json: String): List<Place> = try {
        (Json.parseToJsonElement(json) as? JsonArray).orEmpty().mapNotNull { e ->
            val o = e as? JsonObject ?: return@mapNotNull null
            val lat = o["lat"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: return@mapNotNull null
            val lon = o["lon"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: return@mapNotNull null
            val name = o["display_name"]?.jsonPrimitive?.content.orEmpty()
            Place(shortName(name), Spot(lat, lon, shortName(name)))
        }
    } catch (_: Exception) { emptyList() }

    /** "123, Main Street, Downtown, Los Angeles, …, United States" → "123 Main Street, Downtown". */
    fun shortName(display: String): String {
        val parts = display.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return ""
        val first = if (parts.size > 1 && parts[0].all { it.isDigit() || it == '-' }) "${parts[0]} ${parts[1]}" else parts[0]
        val rest = parts.drop(if (first.contains(' ') && parts.size > 1 && parts[0].all { it.isDigit() || it == '-' }) 2 else 1)
        return (listOf(first) + rest.take(1)).joinToString(", ")
    }
}
