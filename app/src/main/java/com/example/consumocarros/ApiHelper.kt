package com.example.consumocarros

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class ConsumptionData(val city: String, val highway: String, val avg: String)

object ApiHelper {

    // --- ¡CONFIRMA QUE ESTA SIGUE SIENDO TU URL DE NGROK ACTUAL! ---
    // Si cerraste la terminal negra, esta URL habrá cambiado.
    private const val BASE_URL_ROOT = "https://touristic-aniyah-nonsyntonically.ngrok-free.dev/api"

    suspend fun getMakes(): List<String> = withContext(Dispatchers.IO) {
        fetchList("$BASE_URL_ROOT/makes")
    }

    suspend fun getModels(make: String): List<String> = withContext(Dispatchers.IO) {
        val encodedMake = URLEncoder.encode(make, "UTF-8")
        fetchList("$BASE_URL_ROOT/models?make=$encodedMake")
    }

    suspend fun getYears(make: String, model: String): List<String> = withContext(Dispatchers.IO) {
        val encodedMake = URLEncoder.encode(make, "UTF-8")
        val encodedModel = URLEncoder.encode(model, "UTF-8")
        fetchList("$BASE_URL_ROOT/years?make=$encodedMake&model=$encodedModel")
    }

    suspend fun getVehicleConsumption(make: String, model: String, year: String): ConsumptionData = withContext(Dispatchers.IO) {
        // En consumo dejamos el try-catch suave, pero añadimos los headers por si acaso
        try {
            val encMake = URLEncoder.encode(make, "UTF-8")
            val encModel = URLEncoder.encode(model, "UTF-8")
            val encYear = URLEncoder.encode(year, "UTF-8")

            val urlStr = "$BASE_URL_ROOT/consumption?make=$encMake&model=$encModel&year=$encYear"
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000

            // HEADERS ANTI-BLOQUEO
            conn.setRequestProperty("ngrok-skip-browser-warning", "true")
            conn.setRequestProperty("User-Agent", "ConsumoApp")

            if (conn.responseCode == 200) {
                val stream = conn.inputStream
                val reader = BufferedReader(InputStreamReader(stream))
                val jsonStr = reader.readText()
                reader.close()
                val json = JSONObject(jsonStr)

                if (json.has("found") && json.getBoolean("found")) {
                    val cityVal = json.optDouble("city_mpg", 0.0)
                    val hwyVal = json.optDouble("highway_mpg", 0.0)
                    val avgVal = json.optDouble("avg_mpg", 0.0)
                    return@withContext ConsumptionData(
                        String.format(Locale.US, "%.2f", cityVal),
                        String.format(Locale.US, "%.2f", hwyVal),
                        String.format(Locale.US, "%.2f", avgVal)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext ConsumptionData("0.0", "0.0", "0.0")
    }

    // --- FUNCIÓN CHIVATA (SIN TRY-CATCH SILENCIOSO) ---
    private fun fetchList(urlStr: String): List<String> {
        // 1. Abrir conexión
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 8000 // Damos un poco más de tiempo

        // 2. HEADERS MAGICOS
        conn.setRequestProperty("ngrok-skip-browser-warning", "true")
        conn.setRequestProperty("User-Agent", "ConsumoApp-Android") // A veces cambiar el User-Agent ayuda

        // 3. VERIFICAR RESPUESTA HTTP
        if (conn.responseCode != 200) {
            // Si no es 200, lanzamos error para verlo en el Toast
            throw Exception("HTTP Error: ${conn.responseCode}")
        }

        // 4. LEER TEXTO
        val text = conn.inputStream.bufferedReader().readText()

        // 5. INTENTAR PARSEAR JSON
        try {
            val jsonArray = JSONArray(text)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            return list
        } catch (e: Exception) {
            // Si falla aquí, es que NO recibimos JSON (probablemente recibimos HTML de Ngrok)
            // Lanzamos una excepción con los primeros 50 caracteres para ver qué nos mandaron
            val preview = if (text.length > 50) text.substring(0, 50) else text
            throw Exception("No es JSON. Recibido: '$preview'")
        }
    }
}