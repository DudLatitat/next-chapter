package com.dudlatitat.nextchapter.ui.settings

import android.content.Context
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dudlatitat.nextchapter.data.MapMath
import com.dudlatitat.nextchapter.data.Spot
import com.dudlatitat.nextchapter.notify.PlaceWatch
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.floor
import kotlin.math.pow

/**
 * Set or edit Work / Home: search an address (or paste coordinates), then drag the map so the
 * pin sits on the exact spot (where you park), and save. The map is OpenStreetMap's tiles, drawn
 * by the app itself (no map library); search is OpenStreetMap's Nominatim. Both only go online
 * while this screen is open; noticing arrivals stays offline.
 */
@Composable
fun SpotPicker(title: String, initial: Spot?, onSave: (Spot) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var lat by remember { mutableDoubleStateOf(initial?.lat ?: 34.0522) }
    var lon by remember { mutableDoubleStateOf(initial?.lon ?: -118.2437) }
    var zoom by remember { mutableIntStateOf(if (initial != null) 17 else 11) }
    var label by remember { mutableStateOf(initial?.label ?: "") }
    var radius by remember { mutableIntStateOf(initial?.radius ?: com.dudlatitat.nextchapter.data.Places.DEFAULT_RADIUS) }
    var query by remember { mutableStateOf("") }
    val results = remember { mutableStateListOf<MapMath.Place>() }
    var searching by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var locating by remember { mutableStateOf(false) }

    fun search() {
        val q = query.trim()
        if (q.isEmpty()) return
        MapMath.parseCoords(q)?.let { lat = it.lat; lon = it.lon; zoom = 17; label = ""; results.clear(); message = null; return }
        searching = true; message = null
        scope.launch {
            val found = withContext(Dispatchers.IO) { Nominatim.search(q) }
            searching = false
            results.clear()
            if (found == null) message = "Couldn't search right now (no internet?). You can still drag the map or paste coordinates."
            else if (found.isEmpty()) message = "Nothing found. Try adding the city, or paste coordinates."
            else if (found.size == 1) { lat = found[0].spot.lat; lon = found[0].spot.lon; zoom = 17; label = found[0].name }
            else results.addAll(found)
        }
    }

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BackHandler(onBack = onClose)
        Column(Modifier.fillMaxSize().background(Night.ground2).padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
                Text(title, style = MaterialTheme.typography.titleLarge, color = Night.ink)
            }
            OutlinedTextField(
                query, { query = it.take(120) }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Address, place or coordinates") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { search() }),
                trailingIcon = { TextButton(onClick = { search() }, enabled = !searching) { Text(if (searching) "…" else "Search", color = Night.sun) } },
            )
            message?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Night.coral, modifier = Modifier.padding(top = 4.dp)) }
            if (results.isNotEmpty()) Column(Modifier.padding(vertical = 4.dp)) {
                results.take(5).forEach { p ->
                    Text(
                        "📍 ${p.name}", style = MaterialTheme.typography.bodyMedium, color = Night.ink,
                        modifier = Modifier.fillMaxWidth().clickable { lat = p.spot.lat; lon = p.spot.lon; zoom = 17; label = p.name; results.clear() }.padding(vertical = 8.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(18.dp))) {
                TileMap(lat, lon, zoom, radius, onMove = { a, b -> lat = a; lon = b }, onZoom = { zoom = it.coerceIn(MapMath.MIN_ZOOM, MapMath.MAX_ZOOM) })
                // The pin stays in the middle; the map moves under it.
                Text("📍", fontSize = 34.sp, modifier = Modifier.align(Alignment.Center).padding(bottom = 34.dp))
                Column(Modifier.align(Alignment.TopEnd).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MapButton("+") { zoom = (zoom + 1).coerceAtMost(MapMath.MAX_ZOOM) }
                    MapButton("−") { zoom = (zoom - 1).coerceAtLeast(MapMath.MIN_ZOOM) }
                    MapButton(if (locating) "…" else "◎") {
                        locating = true
                        PlaceWatch.currentSpot(context) { s ->
                            locating = false
                            if (s != null) { lat = s.lat; lon = s.lon; zoom = 17; label = "" } else message = "Couldn't find you. Is Location on?"
                        }
                    }
                }
                Text(
                    "© OpenStreetMap contributors", fontSize = 10.sp, color = Color(0xFF333333),
                    modifier = Modifier.align(Alignment.BottomEnd).background(Color(0xCCFFFFFF)).padding(horizontal = 4.dp, vertical = 1.dp),
                )
            }
            Text(
                (if (label.isNotBlank()) "$label · " else "") + "%.5f, %.5f".format(java.util.Locale.US, lat, lon),
                style = MaterialTheme.typography.bodySmall, color = Night.muted, modifier = Modifier.padding(vertical = 6.dp),
            )
            Text("Drag the map so the pin is where you park. Pinch or use + / − to zoom.", style = MaterialTheme.typography.labelSmall, color = Night.muted)
            // How close counts as "arrived": the gold circle on the map.
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Text("Arrived within", style = MaterialTheme.typography.bodySmall, color = Night.ink)
                androidx.compose.material3.Slider(
                    value = radius.toFloat(),
                    onValueChange = { radius = (it / 10).toInt() * 10 },
                    valueRange = com.dudlatitat.nextchapter.data.Places.MIN_RADIUS.toFloat()..com.dudlatitat.nextchapter.data.Places.MAX_RADIUS.toFloat(),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Night.sun, activeTrackColor = Night.sun),
                )
                Text(MapMath.radiusLabel(radius), style = MaterialTheme.typography.labelMedium, color = Night.sun)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onSave(Spot(lat, MapMath.wrapLon(lon), label, radius)); onClose() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = Night.ground2),
            ) { Text("Save this spot") }
        }
    }
}

@Composable
private fun MapButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(Night.ground1.copy(alpha = 0.9f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = Night.ink, fontSize = 20.sp) }
}

/** OpenStreetMap tiles around (lat, lon) at [zoom]; drag to pan, pinch to change zoom. */
@Composable
private fun TileMap(lat: Double, lon: Double, zoom: Int, radius: Int, onMove: (Double, Double) -> Unit, onZoom: (Int) -> Unit) {
    val context = LocalContext.current
    val tilePx = with(LocalDensity.current) { 128.dp.toPx() }
    val tiles = remember { mutableStateMapOf<String, ImageBitmap>() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val cx = MapMath.tileX(lon, zoom)
    val cy = MapMath.tileY(lat, zoom)
    val n = 2.0.pow(zoom).toInt()

    // Which tiles are on screen right now.
    val needed = remember(cx, cy, zoom, size, tilePx) {
        if (size == IntSize.Zero) emptyList() else {
            val x0 = floor(cx - size.width / 2.0 / tilePx).toInt(); val x1 = floor(cx + size.width / 2.0 / tilePx).toInt()
            val y0 = floor(cy - size.height / 2.0 / tilePx).toInt(); val y1 = floor(cy + size.height / 2.0 / tilePx).toInt()
            buildList { for (x in x0..x1) for (y in y0..y1) if (y in 0 until n) add(Triple(x, y, zoom)) }
        }
    }
    LaunchedEffect(needed) {
        needed.forEach { (x, y, z) ->
            val key = "$z/${Math.floorMod(x, n)}/$y"
            if (key !in tiles) launch { TileLoader.load(context, key)?.let { tiles[key] = it } }
        }
    }

    var pinch by remember { mutableDoubleStateOf(1.0) }
    // The gesture keeps running across recompositions, so it reads the latest center through these.
    val curLat by androidx.compose.runtime.rememberUpdatedState(lat)
    val curLon by androidx.compose.runtime.rememberUpdatedState(lon)
    Canvas(
        Modifier.fillMaxSize().background(Color(0xFFDDE3EA))
            .onSizeChanged { size = it }
            .pointerInput(zoom, tilePx) {
                detectTransformGestures { _, pan, zoomChange, _ ->
                    // Pan: the map follows your finger, so the center moves the other way.
                    val nx = MapMath.tileX(curLon, zoom) - pan.x / tilePx
                    val ny = (MapMath.tileY(curLat, zoom) - pan.y / tilePx).coerceIn(0.0, n.toDouble())
                    onMove(MapMath.lat(ny, zoom), MapMath.wrapLon(MapMath.lon(nx, zoom)))
                    pinch *= zoomChange
                    if (pinch > 1.6) { pinch = 1.0; onZoom(zoom + 1) } else if (pinch < 0.6) { pinch = 1.0; onZoom(zoom - 1) }
                }
            },
    ) {
        needed.forEach { (x, y, z) ->
            val img = tiles["$z/${Math.floorMod(x, n)}/$y"] ?: return@forEach
            val left = ((x - cx) * tilePx + this.size.width / 2).toInt()
            val top = ((y - cy) * tilePx + this.size.height / 2).toInt()
            drawImage(img, dstOffset = IntOffset(left, top), dstSize = IntSize(tilePx.toInt() + 1, tilePx.toInt() + 1))
        }
        // The arrival zone around the pin (the pin sits at the center).
        val r = (MapMath.metersToTiles(radius.toDouble(), lat, zoom) * tilePx).toFloat()
        drawCircle(Night.sun.copy(alpha = 0.18f), radius = r, center = this.center)
        drawCircle(Night.sun, radius = r, center = this.center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
    }
}

/** Downloads OpenStreetMap tiles politely (2 at a time, a clear app name) and keeps them for a week. */
private object TileLoader {
    private val gate = Semaphore(2)
    private const val WEEK_MS = 7L * 24 * 3600 * 1000

    suspend fun load(context: Context, key: String): ImageBitmap? = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "tiles/" + key.replace('/', '_') + ".png")
        try {
            if (file.exists() && System.currentTimeMillis() - file.lastModified() < WEEK_MS) {
                return@withContext BitmapFactory.decodeFile(file.path)?.asImageBitmap()
            }
            gate.withPermit {
                val c = URL("https://tile.openstreetmap.org/$key.png").openConnection() as HttpURLConnection
                c.setRequestProperty("User-Agent", Nominatim.USER_AGENT)
                c.connectTimeout = 8000; c.readTimeout = 8000
                val bytes = c.inputStream.use { it.readBytes() }
                file.parentFile?.mkdirs(); file.writeBytes(bytes)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }
        } catch (_: Exception) { null }
    }
}

/** OpenStreetMap's address search: one request per tap of Search (their rules: at most 1 a second). */
internal object Nominatim {
    const val USER_AGENT = "NextChapter/1.x (private Android app; github.com/DudLatitat/next-chapter)"

    /** Up to 5 places, an empty list if nothing matched, or null if the search couldn't run. */
    fun search(q: String): List<MapMath.Place>? = try {
        val url = "https://nominatim.openstreetmap.org/search?format=jsonv2&limit=5&q=" + URLEncoder.encode(q, "UTF-8")
        val c = URL(url).openConnection() as HttpURLConnection
        c.setRequestProperty("User-Agent", USER_AGENT)
        c.setRequestProperty("Accept-Language", "en")
        c.connectTimeout = 8000; c.readTimeout = 8000
        MapMath.parseSearch(c.inputStream.bufferedReader().use { it.readText() })
    } catch (_: Exception) { null }
}
