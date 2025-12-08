package com.example.consumocarros

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import android.text.SpannableString
import android.text.style.ForegroundColorSpan

data class RouteInfo(val title: String, val details: String, val color: Int)

class MapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var btnLocate: Button
    private lateinit var btnClear: Button
    private lateinit var originInput: EditText
    private lateinit var destInput: EditText
    private lateinit var suggestions: ListView
    private lateinit var hint: TextView
    private lateinit var routesList: ListView
    private lateinit var clearOrigin: ImageButton
    private lateinit var clearDest: ImageButton

    private var origin: GeoPoint? = null
    private var originMarker: Marker? = null
    private var dest: GeoPoint? = null
    private var destMarker: Marker? = null
    private val routePolylines = mutableListOf<Polyline>()
    private val routesData = mutableListOf<JSONObject>()

    // --- VARIABLES DE CONSUMO ---
    private var consumptionCityKmpl: Double = 10.0
    private var consumptionHighwayKmpl: Double = 14.2
    private var usuario: Usuario? = null
    private var selectedCar: Usuario.Car? = null
    private lateinit var saveManager: SaveManager
    // --- FIN DE VARIABLES ---

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var blockTextWatcher = false

    companion object {
        const val REQ_LOCATION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        setContentView(R.layout.activity_map)

        saveManager = SaveManager(this)

        usuario = intent.getSerializableExtra("usuario") as? Usuario

        // --- RECUPERAR DATOS DEL COCHE ---
        selectedCar = intent.getSerializableExtra("selected_car") as? Usuario.Car
        selectedCar?.let {
            it.cityKmpl.toDoubleOrNull()?.let { city -> consumptionCityKmpl = city }
            it.highwayKmpl.toDoubleOrNull()?.let { highway -> consumptionHighwayKmpl = highway }
        }
        
        checkCarConfiguration()

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            val intent = Intent(this, MisCochesActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
            finish()
        }

        map = findViewById(R.id.map)
        btnLocate = findViewById(R.id.btn_locate)
        btnClear = findViewById(R.id.btn_clear)
        originInput = findViewById(R.id.origin_input)
        destInput = findViewById(R.id.dest_input)
        clearOrigin = findViewById(R.id.clear_origin)
        clearDest = findViewById(R.id.clear_dest)
        suggestions = findViewById(R.id.suggestions)
        hint = findViewById(R.id.hint)
        routesList = findViewById(R.id.routes_list)

        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        map.setMultiTouchControls(true)
        map.controller.setZoom(13.0)
        map.controller.setCenter(GeoPoint(40.4168, -3.7038))

        btnLocate.setOnClickListener { locateAndSetOrigin() }
        btnClear.setOnClickListener { clearAll() }

        originInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { clearOrigin.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        destInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { clearDest.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        clearOrigin.setOnClickListener {
            originInput.setText("")
            origin = null
            originMarker?.let { map.overlays.remove(it) }
            originMarker = null
            clearRoutes()
            map.invalidate()
            hint.text = "Selecciona un nuevo origen en el mapa."
        }

        clearDest.setOnClickListener {
            destInput.setText("")
            dest = null
            destMarker?.let { map.overlays.remove(it) }
            destMarker = null
            suggestions.visibility = View.GONE
            map.invalidate()
        }

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (p == null) return false
                suggestions.visibility = View.GONE

                when {
                    origin == null -> {
                        setOrigin(p)
                        hint.text = "Origen establecido. Pulsa para seleccionar destino."
                    }
                    dest == null -> {
                        setDest(p)
                        hint.text = "Destino establecido. Calculando rutas..."
                        requestRoutes()
                    }
                    else -> {
                        setDest(p)
                        hint.text = "Destino cambiado. Calculando nuevas rutas..."
                        requestRoutes()
                    }
                }
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        }
        map.overlays.add(MapEventsOverlay(mapEventsReceiver))

        suggestions.setOnItemClickListener { _, _, position, _ ->
            val item = suggestions.adapter.getItem(position) as JSONObject
            setDestFromSuggestion(item)
        }

        destInput.addTextChangedListener(object : TextWatcher {
            private val runnable = Runnable {
                if (!blockTextWatcher) {
                    val q = destInput.text.toString().trim()
                    if (q.length >= 3) searchNominatim(q) else suggestions.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) { destInput.removeCallbacks(runnable); destInput.postDelayed(runnable, 400) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        destInput.setOnEditorActionListener { _, _, _ ->
            suggestions.visibility = View.GONE
            hideKeyboard(destInput)
            destInput.clearFocus()
            true
        }
    }

    private fun checkCarConfiguration() {
        selectedCar?.let { car ->
            if (car.getcapacidaddeposito() == -1) {
                pedirCapacidad(car)
            }
            else if (car.getCapacidadactual() == -1) {
                pedirPorcentaje(car)
            }
        }
    }

    private fun pedirCapacidad(coche: Usuario.Car) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_input, null)
        val title = view.findViewById<TextView>(R.id.dialogTitle)
        val input = view.findViewById<EditText>(R.id.dialogInput)

        val spannableTitle = SpannableString("Falta configurar: Capacidad del depósito (L)")
        spannableTitle.setSpan(ForegroundColorSpan(Color.WHITE), 0, spannableTitle.length, 0)
        title.text = spannableTitle
        input.hint = "Ej: 50"

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .setCancelable(false)
            .create()

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE)

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val valor = input.text.toString()
            if (valor.isNotEmpty()) {
                val capacidad = valor.toIntOrNull()
                if (capacidad != null && capacidad > 0) {
                    coche.setCapacidaddeposito(capacidad)
                    if (coche.getCapacidadactual() == -1) {
                         coche.setCapacidadactual(0)
                    }
                    saveUserData()
                    dialog.dismiss()
                    if (coche.getCapacidadactual() == 0 || coche.getCapacidadactual() == -1) {
                        pedirPorcentaje(coche)
                    }
                } else {
                    Toast.makeText(this, "Introduce un número válido", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun pedirPorcentaje(coche: Usuario.Car) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_input, null)
        val title = view.findViewById<TextView>(R.id.dialogTitle)
        val input = view.findViewById<EditText>(R.id.dialogInput)

        val spannableTitle = SpannableString("Falta configurar: Porcentaje actual (%)")
        spannableTitle.setSpan(ForegroundColorSpan(Color.WHITE), 0, spannableTitle.length, 0)
        title.text = spannableTitle
        input.hint = "0 - 100"

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Omitir", null)
            .create()

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE)

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val texto = input.text.toString()
            if (texto.isNotEmpty()) {
                var porcentaje = texto.toIntOrNull()
                if (porcentaje != null) {
                    if (porcentaje < 0) porcentaje = 0
                    if (porcentaje > 100) porcentaje = 100
                    coche.setCapacidadactual(porcentaje)
                    saveUserData()
                    dialog.dismiss()
                } else {
                     Toast.makeText(this, "Introduce un número válido", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveUserData() {
        if (usuario != null && selectedCar != null) {
            val lista = usuario!!.coches
            for (i in lista.indices) {
                val c = lista[i]
                if (c.brand == selectedCar!!.brand && c.model == selectedCar!!.model && c.year == selectedCar!!.year) {
                    lista[i] = selectedCar!!
                    break
                }
            }
            saveManager.actualizarUsuario(usuario!!)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setDestFromSuggestion(item: JSONObject) {
        val lat = item.optDouble("lat")
        val lon = item.optDouble("lon")
        setDest(GeoPoint(lat, lon))
        suggestions.visibility = View.GONE
        blockTextWatcher = true
        destInput.setText(item.optString("display_name"))
        destInput.clearFocus()
        hideKeyboard(destInput)
        blockTextWatcher = false
        hint.text = "Destino establecido. Calculando rutas..."
        requestRoutes()
    }

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause() { super.onPause(); map.onPause() }

    private fun locateAndSetOrigin() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQ_LOCATION)
            return
        }
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            val last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (last != null) {
                val gp = GeoPoint(last.latitude, last.longitude)
                setOrigin(gp)
                map.controller.animateTo(gp, 14.0, 500)
            } else {
                lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        val gp = GeoPoint(location.latitude, location.longitude)
                        setOrigin(gp)
                        map.controller.animateTo(gp, 14.0, 500)
                    }
                }, Looper.getMainLooper())
            }
        } catch (ex: SecurityException) {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locateAndSetOrigin()
        } else {
            Toast.makeText(this, "Permiso de ubicación rechazado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setOrigin(gp: GeoPoint) {
        origin = gp
        if (originMarker == null) {
            originMarker = Marker(map).apply { title = "Origen"; setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) }
            map.overlays.add(originMarker)
        }
        originMarker!!.position = gp
        reverseSearchNominatim(gp, isOrigin = true)
        map.invalidate()
        if (dest != null) { clearRoutes(); requestRoutes() }
    }

    private fun setDest(gp: GeoPoint) {
        dest = gp
        if (destMarker == null) {
            destMarker = Marker(map).apply { title = "Destino"; setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) }
            map.overlays.add(destMarker)
        }
        destMarker!!.position = gp
        suggestions.visibility = View.GONE
        destInput.clearFocus()
        destInput.isCursorVisible = false
        hideKeyboard(destInput)
        reverseSearchNominatim(gp, isOrigin = false)
        map.invalidate()
    }

    private fun searchNominatim(q: String) {
        val url = "https://nominatim.openstreetmap.org/search?format=jsonv2&q=${Uri.encode(q)}&addressdetails=1&limit=6"
        val req = Request.Builder().url(url).header("Accept-Language", "es").header("User-Agent", packageName).build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) return
                    val items = (JSONArray(it.body?.string() ?: "[]")).let { 0.until(it.length()).map { i -> it.getJSONObject(i) } }
                    runOnUiThread {
                        val adapter = object : ArrayAdapter<JSONObject>(this@MapActivity, android.R.layout.simple_list_item_1, items) {
                            override fun getView(pos: Int, view: View?, parent: ViewGroup) = (super.getView(pos, view, parent) as TextView).apply {
                                text = getItem(pos)?.optString("display_name") ?: ""
                            }
                        }
                        suggestions.adapter = adapter
                        suggestions.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
            }
        })
    }

    private fun reverseSearchNominatim(gp: GeoPoint, isOrigin: Boolean) {
        val url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${gp.latitude}&lon=${gp.longitude}"
        val req = Request.Builder().url(url).header("Accept-Language", "es").header("User-Agent", packageName).build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) return
                    val displayName = JSONObject(it.body?.string() ?: "{}").optString("display_name", "Ubicación desconocida")
                    runOnUiThread {
                        blockTextWatcher = true
                        if (isOrigin) originInput.setText(displayName) else destInput.setText(displayName)
                        blockTextWatcher = false
                    }
                }
            }
        })
    }

    private fun requestRoutes() {
        val o = origin ?: return
        val d = dest ?: return
        clearRoutes()
        val coords = "${o.longitude},${o.latitude};${d.longitude},${d.latitude}"
        val url = "https://router.project-osrm.org/route/v1/driving/$coords?alternatives=true&overview=full&geometries=geojson"
        val req = Request.Builder().url(url).header("User-Agent", packageName).build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { runOnUiThread { hint.text = "Error calculando rutas: ${e.message}" } }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) { runOnUiThread { hint.text = "Error en OSRM: ${it.code}" }; return }
                    routesData.clear()
                    val routes = JSONObject(it.body?.string() ?: "{}").optJSONArray("routes") ?: JSONArray()
                    (0 until routes.length()).forEach { i -> routesData.add(routes.getJSONObject(i)) }
                    runOnUiThread {
                        if (routesData.isEmpty()) hint.text = "No se encontraron rutas." else {
                            drawRoutes()
                            hint.text = "Selecciona una ruta para abrirla en Google Maps."
                        }
                    }
                }
            }
        })
    }

    private fun calculateConsumption(distanceMeters: Double, durationSeconds: Double): Double {
        if (durationSeconds <= 0 || (consumptionCityKmpl <= 0 && consumptionHighwayKmpl <= 0)) {
            return 0.0
        }

        val speedKph = (distanceMeters / 1000.0) / (durationSeconds / 3600.0)

        val kmpl = if (speedKph > 60 && consumptionHighwayKmpl > 0) {
            consumptionHighwayKmpl
        } else if (consumptionCityKmpl > 0) {
            consumptionCityKmpl
        } else {
            consumptionHighwayKmpl 
        }
        return (distanceMeters / 1000.0) / kmpl
    }

    private fun drawRoutes() {
        routePolylines.forEach { map.overlays.remove(it) }
        routePolylines.clear()

        val colors = arrayOf("#0078d4", "#ff8c00", "#2b8f7e", "#d9534f")
        val routeInfoList = mutableListOf<RouteInfo>()

        for (i in routesData.indices) {
            val r = routesData[i]
            val geom = r.getJSONObject("geometry")
            val coords = geom.getJSONArray("coordinates")
            val pts = (0 until coords.length()).map { j ->
                coords.getJSONArray(j).let { GeoPoint(it.getDouble(1), it.getDouble(0)) }
            }

            val poly = Polyline(map)
            poly.setPoints(pts)
            poly.color = Color.parseColor(colors[i % colors.size])
            poly.width = if (i == 0) 10f else 6f

            poly.setOnClickListener { _, _, _ ->
                highlightRoute(i)
                openRouteInGoogleMaps(i)
                true
            }

            map.overlays.add(poly)
            routePolylines.add(poly)

            val dist = r.optDouble("distance", 0.0)
            val dur = r.optDouble("duration", 0.0)

            val consumoLitros = calculateConsumption(dist, dur)
            routeInfoList.add(RouteInfo("Ruta ${i + 1}", "${formatDistance(dist)} · ${formatDuration(dur)} · %.2f L".format(consumoLitros), poly.color))
        }

        val allPoints = routePolylines.flatMap { it.points }
        if (allPoints.isNotEmpty()) map.zoomToBoundingBox(BoundingBox.fromGeoPoints(allPoints), true, 100)

        routesList.adapter = RouteAdapter(this, routeInfoList)
        routesList.visibility = View.VISIBLE
        routesList.setOnItemClickListener { _, _, pos, _ ->
            highlightRoute(pos)
            openRouteInGoogleMaps(pos)
        }
        map.invalidate()
    }

    private fun highlightRoute(index: Int) {
        routePolylines.forEachIndexed { i, poly -> poly.width = if (i == index) 12f else 6f }
        map.invalidate()
    }

    private fun openRouteInGoogleMaps(index: Int) {
        if (index >= 0 && index < routesData.size) {
            val r = routesData[index]
            val dist = r.optDouble("distance", 0.0)
            val dur = r.optDouble("duration", 0.0)
            val litersConsumed = calculateConsumption(dist, dur)

            if (usuario != null && selectedCar != null) {
                val cocheEnLista = usuario!!.coches.find {
                    it.brand == selectedCar!!.brand && it.model == selectedCar!!.model && it.year == selectedCar!!.year
                }

                if (cocheEnLista != null) {
                    val capacidadTotal = cocheEnLista.getcapacidaddeposito()
                    val porcentajeActual = cocheEnLista.getCapacidadactual()

                    if (capacidadTotal > 0 && porcentajeActual >= 0) {
                        val litrosActuales = (porcentajeActual.toDouble() / 100.0) * capacidadTotal
                        var nuevosLitros = litrosActuales - litersConsumed
                        if (nuevosLitros < 0) nuevosLitros = 0.0
                        val nuevoPorcentaje = ((nuevosLitros / capacidadTotal) * 100).roundToInt()
                        cocheEnLista.setCapacidadactual(nuevoPorcentaje)
                        Toast.makeText(this, "Combustible actualizado: -${String.format("%.2f", litersConsumed)} L", Toast.LENGTH_SHORT).show()
                    }
                    
                    val origenStr = originInput.text.toString()
                    val destinoStr = destInput.text.toString()
                    val viaje = Usuario.Viaje(
                        origenStr,
                        destinoStr,
                        dist / 1000.0, 
                        litersConsumed,
                        cocheEnLista.toString()
                    )
                    usuario!!.agregarViaje(viaje)

                    saveManager.actualizarUsuario(usuario!!)
                } else {
                     Toast.makeText(this, "Configura el depósito para descontar combustible", Toast.LENGTH_LONG).show()
                }
            }
        }

        val o = origin ?: return
        val d = dest ?: return
        val url = "https://www.google.com/maps/dir/?api=1&origin=${o.latitude},${o.longitude}&destination=${d.latitude},${d.longitude}&travelmode=driving"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun clearRoutes() {
        routePolylines.forEach { map.overlays.remove(it) }
        routePolylines.clear()
        routesList.adapter = null
        routesList.visibility = View.GONE
        map.invalidate()
    }

    private fun clearAll() {
        clearRoutes()
        destMarker?.let { map.overlays.remove(it) }; destMarker = null; dest = null
        originMarker?.let { map.overlays.remove(it) }; originMarker = null; origin = null
        originInput.setText("")
        destInput.setText("")
        map.invalidate()
    }

    private fun formatDistance(m: Double) = if (m >= 1000) "%.1f km".format(m / 1000.0) else "${m.roundToInt()} m"
    private fun formatDuration(s: Double) = (s / 60.0).roundToInt().let { if (it >= 60) "${it / 60} h ${it % 60} min" else "$it min" }

    private class RouteAdapter(ctx: Context, routes: List<RouteInfo>) : ArrayAdapter<RouteInfo>(ctx, 0, routes) {
        override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
            val v = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_route, parent, false)
            getItem(pos)?.let {
                v.findViewById<TextView>(R.id.route_title).apply { text = it.title; setTextColor(it.color) }
                v.findViewById<TextView>(R.id.route_details).text = it.details
            }
            return v
        }
    }
}
