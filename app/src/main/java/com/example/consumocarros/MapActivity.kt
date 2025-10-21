package com.example.consumocarros

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class MapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var btnLocate: Button
    private lateinit var btnClear: Button
    private lateinit var destInput: EditText
    private lateinit var suggestions: ListView
    private lateinit var hint: TextView
    private lateinit var routesList: ListView

    private var origin: GeoPoint? = null
    private var originMarker: Marker? = null
    private var dest: GeoPoint? = null
    private var destMarker: Marker? = null
    private val routePolylines = mutableListOf<Polyline>()
    private val routesData = mutableListOf<JSONObject>()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    companion object {
        const val REQ_LOCATION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // importante: userAgent para osmdroid
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_map)

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        map.setMultiTouchControls(true)
        map.controller.setZoom(13.0)
        map.controller.setCenter(GeoPoint(40.4168, -3.7038))

        btnLocate = findViewById(R.id.btn_locate)
        btnClear = findViewById(R.id.btn_clear)
        destInput = findViewById(R.id.dest_input)
        suggestions = findViewById(R.id.suggestions)
        hint = findViewById(R.id.hint)
        routesList = findViewById(R.id.routes_list)

        btnLocate.setOnClickListener { locateAndSetOrigin() }
        btnClear.setOnClickListener { clearAll() }

        // Map tap: usamos MapEventsOverlay + MapEventsReceiver
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (p == null) return false
                if (origin == null) {
                    setOrigin(p)
                    hint.text = "Origen establecido. Pulsa para seleccionar destino."
                } else {
                    setDest(p)
                    hint.text = "Destino establecido. Calculando rutas..."
                    requestRoutes()
                }
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean {
                // no-op
                return false
            }
        }
        val overlay = MapEventsOverlay(mapEventsReceiver)
        map.overlays.add(overlay)

        // suggestions click
        suggestions.setOnItemClickListener { _, _, position, _ ->
            val item = suggestions.adapter.getItem(position) as JSONObject
            val lat = item.optDouble("lat")
            val lon = item.optDouble("lon")
            setDest(GeoPoint(lat, lon))
            suggestions.visibility = View.GONE
            destInput.setText(item.optString("display_name"))
            hint.text = "Destino establecido. Calculando rutas..."
            requestRoutes()
        }

        // dest input debounce simple
        destInput.addTextChangedListener(object : TextWatcher {
            private val runnable = Runnable {
                val q = destInput.text.toString().trim()
                if (q.length >= 3) searchNominatim(q)
            }
            override fun afterTextChanged(s: Editable?) {
                destInput.removeCallbacks(runnable)
                destInput.postDelayed(runnable, 300)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // iniciar geolocalización automática
        locateAndSetOrigin()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    // ---------- LOCATION ----------
    private fun locateAndSetOrigin() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQ_LOCATION)
            return
        }

        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val provider = LocationManager.GPS_PROVIDER
        try {
            val last = lm.getLastKnownLocation(provider) ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (last != null) {
                val gp = GeoPoint(last.latitude, last.longitude)
                setOrigin(gp)
                map.controller.animateTo(gp)
                map.controller.setZoom(14.0)
                hint.text = "Origen establecido desde tu ubicación. Pulsa en el mapa para marcar destino."
            } else {
                // solicitar single update
                lm.requestSingleUpdate(provider, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        val gp = GeoPoint(location.latitude, location.longitude)
                        setOrigin(gp)
                        map.controller.animateTo(gp)
                        map.controller.setZoom(14.0)
                        hint.text = "Origen establecido desde tu ubicación. Pulsa en el mapa para marcar destino."
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }, Looper.getMainLooper())
            }
        } catch (ex: SecurityException) {
            ex.printStackTrace()
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locateAndSetOrigin()
            } else {
                Toast.makeText(this, "Permiso de ubicación rechazado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setOrigin(gp: GeoPoint) {
        origin = gp
        if (originMarker == null) {
            originMarker = Marker(map)
            originMarker!!.title = "Origen"
            originMarker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            map.overlays.add(originMarker)
        }
        originMarker!!.position = gp
        map.invalidate()
    }

    private fun setDest(gp: GeoPoint) {
        dest = gp
        if (destMarker == null) {
            destMarker = Marker(map)
            destMarker!!.title = "Destino"
            destMarker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            map.overlays.add(destMarker)
        }
        destMarker!!.position = gp
        map.invalidate()
    }

    // ---------- Nominatim search ----------
    private fun searchNominatim(q: String) {
        val url = "https://nominatim.openstreetmap.org/search?format=jsonv2&q=${Uri.encode(q)}&addressdetails=1&limit=6"
        val req = Request.Builder()
            .url(url)
            .header("Accept-Language", "es")
            .header("User-Agent", packageName)
            .build()
        client.newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { /* ignore */ }
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) return
                    val body = it.body?.string() ?: return
                    val arr = JSONArray(body)
                    val items = mutableListOf<JSONObject>()
                    for (i in 0 until arr.length()) {
                        items.add(arr.getJSONObject(i))
                    }
                    runOnUiThread {
                        val adapter = object : ArrayAdapter<JSONObject>(this@MapActivity, android.R.layout.simple_list_item_1, items) {
                            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                                val v = super.getView(position, convertView, parent) as TextView
                                v.text = getItem(position)?.optString("display_name") ?: ""
                                return v
                            }
                        }
                        suggestions.adapter = adapter
                        suggestions.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
            }
        })
    }

    // ---------- OSRM routing ----------
    private fun requestRoutes() {
        val o = origin ?: return
        val d = dest ?: return

        clearRoutes()

        val coords = "${o.longitude},${o.latitude};${d.longitude},${d.latitude}"
        val url = "https://router.project-osrm.org/route/v1/driving/$coords?alternatives=true&overview=full&geometries=geojson"
        val req = Request.Builder().url(url).header("User-Agent", packageName).build()

        client.newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { hint.text = "Error calculando rutas: ${e.message}" }
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        runOnUiThread { hint.text = "Error en OSRM: ${it.code}" }
                        return
                    }
                    val body = it.body?.string() ?: ""
                    val json = JSONObject(body)
                    val arr = json.optJSONArray("routes") ?: JSONArray()
                    routesData.clear()
                    for (i in 0 until arr.length()) {
                        routesData.add(arr.getJSONObject(i))
                    }
                    runOnUiThread {
                        if (routesData.isEmpty()) {
                            hint.text = "No se encontraron rutas."
                        } else {
                            drawRoutes()
                            hint.text = "Selecciona una ruta para abrirla en Google Maps."
                        }
                    }
                }
            }
        })
    }

    private fun drawRoutes() {
        // limpiar
        routePolylines.forEach { map.overlays.remove(it) }
        routePolylines.clear()

        val listItems = mutableListOf<String>()
        val colors = arrayOf("#0078d4", "#ff8c00", "#2b8f7e", "#d9534f")

        for (i in routesData.indices) {
            val r = routesData[i]
            val geom = r.getJSONObject("geometry")
            val coords = geom.getJSONArray("coordinates")

            val pts = mutableListOf<GeoPoint>()
            for (j in 0 until coords.length()) {
                val pair = coords.getJSONArray(j)
                val lng = pair.getDouble(0)
                val lat = pair.getDouble(1)
                pts.add(GeoPoint(lat, lng))
            }

            val poly = Polyline()
            poly.setPoints(pts)
            poly.width = if (i == 0) 12f else 6f
            try {
                poly.color = android.graphics.Color.parseColor(colors[i % colors.size])
            } catch (ex: Exception) { /* ignore color parse error */ }

            poly.setOnClickListener { _, _, _ ->
                highlightRoute(i)
                openRouteInGoogleMaps(i)
                true
            }

            routePolylines.add(poly)
            map.overlays.add(poly)

            val dist = r.optDouble("distance", 0.0)
            val dur = r.optDouble("duration", 0.0)
            listItems.add("Ruta ${i+1}\n${formatDistance(dist)} · ${formatDuration(dur)}")
        }

        // ajustar vista a bound de todas las rutas
        if (routePolylines.isNotEmpty()) {
            val allPoints = routePolylines.flatMap { it.points }
            if (allPoints.isNotEmpty()) {
                val bb = BoundingBox.fromGeoPoints(allPoints)
                map.zoomToBoundingBox(bb, true, 50)
            }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, listItems)
        routesList.adapter = adapter
        routesList.setOnItemClickListener { _, _, position, _ ->
            highlightRoute(position)
            openRouteInGoogleMaps(position)
        }
    }

    private fun highlightRoute(index: Int) {
        for (i in routePolylines.indices) {
            val poly = routePolylines[i]
            poly.width = if (i == index) 12f else 6f
        }
        routesList.setItemChecked(index, true)
        map.invalidate()
    }

    private fun openRouteInGoogleMaps(index: Int) {
        val r = routesData.getOrNull(index) ?: return
        val o = origin ?: return
        val d = dest ?: return

        val geom = r.getJSONObject("geometry")
        val coords = geom.getJSONArray("coordinates")
        val waypoints = mutableListOf<String>()
        var lastLng = coords.getJSONArray(0).getDouble(0)
        var lastLat = coords.getJSONArray(0).getDouble(1)
        var accDist = 0.0
        for (i in 1 until coords.length()) {
            val lng = coords.getJSONArray(i).getDouble(0)
            val lat = coords.getJSONArray(i).getDouble(1)
            val dx = (lng - lastLng)
            val dy = (lat - lastLat)
            val approxKm = Math.sqrt(dx*dx + dy*dy) * 111.0
            accDist += approxKm
            if (accDist > 5.0) {
                waypoints.add("via:${lat},${lng}")
                accDist = 0.0
            }
            lastLng = lng
            lastLat = lat
            if (waypoints.size >= 10) break
        }

        val originStr = "${o.latitude},${o.longitude}"
        val destStr = "${d.latitude},${d.longitude}"
        val wayParam = if (waypoints.isNotEmpty()) "&waypoints=${Uri.encode(waypoints.joinToString("|"))}" else ""
        val url = "https://www.google.com/maps/dir/?api=1&origin=${Uri.encode(originStr)}&destination=${Uri.encode(destStr)}$wayParam&travelmode=driving"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun clearRoutes() {
        routePolylines.forEach { map.overlays.remove(it) }
        routePolylines.clear()
        routesData.clear()
        routesList.adapter = null
    }

    private fun clearAll() {
        clearRoutes()
        if (destMarker != null) {
            map.overlays.remove(destMarker)
            destMarker = null
            dest = null
        }
        if (originMarker != null) {
            map.overlays.remove(originMarker)
            originMarker = null
            origin = null
        }
        map.invalidate()
    }

    // ---------- Helpers ----------
    private fun formatDistance(meters: Double): String {
        return if (meters >= 1000) {
            String.format("%.1f km", meters / 1000.0)
        } else {
            "${meters.roundToInt()} m"
        }
    }

    private fun formatDuration(seconds: Double): String {
        val mins = (seconds / 60.0).roundToInt()
        return if (mins >= 60) {
            "${mins / 60} h ${mins % 60} min"
        } else {
            "$mins min"
        }
    }
}
