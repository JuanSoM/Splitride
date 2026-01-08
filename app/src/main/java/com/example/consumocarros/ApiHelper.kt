package com.example.consumocarros

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

data class ConsumptionData(val city: String, val highway: String, val avg: String)

object ApiHelper {

    // --- 1. OBTENER AÑOS (Generamos la lista localmente) ---
    // La API cubre desde 1984 hasta hoy.
    fun getYearsList(): List<String> {
        val currentYear = 2025 // Puedes actualizar esto dinámicamente
        val years = mutableListOf<String>()
        for (y in currentYear downTo 1984) {
            years.add(y.toString())
        }
        return years
    }

    // --- 2. OBTENER MARCAS (Desde API Oficial) ---
    // Endpoint: https://www.fueleconomy.gov/ws/rest/vehicle/menu/make?year=2015
    suspend fun getMakes(year: String): List<String> = withContext(Dispatchers.IO) {
        val url = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/make?year=$year"
        parseXmlList(url)
    }

    // --- 3. OBTENER MODELOS (Desde API Oficial) ---
    // Endpoint: https://www.fueleconomy.gov/ws/rest/vehicle/menu/model?year=2015&make=Honda
    suspend fun getModels(year: String, make: String): List<String> = withContext(Dispatchers.IO) {
        // Encodeamos la marca por si tiene espacios (ej: Aston Martin -> Aston%20Martin)
        val encMake = java.net.URLEncoder.encode(make, "UTF-8").replace("+", "%20")
        val url = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/model?year=$year&make=$encMake"
        parseXmlList(url)
    }

    // --- 4. OBTENER CONSUMO (Lógica compleja de ID) ---
    suspend fun getVehicleConsumption(year: String, make: String, model: String): ConsumptionData = withContext(Dispatchers.IO) {
        try {
            val encMake = java.net.URLEncoder.encode(make, "UTF-8").replace("+", "%20")
            val encModel = java.net.URLEncoder.encode(model, "UTF-8").replace("+", "%20")

            // PASO A: Obtener el ID del vehículo (Vehicle ID)
            // A veces un modelo tiene varias versiones (Automático, Manual...). Cogemos la primera opción.
            val optionsUrl = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/options?year=$year&make=$encMake&model=$encModel"
            val idDoc = getXmlDocument(optionsUrl)

            // Buscamos la etiqueta <value> que contiene el ID
            val idNode = idDoc?.getElementsByTagName("value")?.item(0)
            val vehicleId = idNode?.textContent

            if (vehicleId == null) {
                return@withContext ConsumptionData("N/A", "N/A", "N/A")
            }

            // PASO B: Obtener detalles con el ID
            val dataUrl = "https://www.fueleconomy.gov/ws/rest/vehicle/$vehicleId"
            val doc = getXmlDocument(dataUrl) ?: return@withContext ConsumptionData("N/A", "N/A", "N/A")

            // Extraemos MPG (Millas por Galón)
            val cityMpg = doc.getElementsByTagName("city08").item(0)?.textContent?.toDoubleOrNull() ?: 0.0
            val highwayMpg = doc.getElementsByTagName("highway08").item(0)?.textContent?.toDoubleOrNull() ?: 0.0
            val avgMpg = (cityMpg + highwayMpg) / 2

            // Convertimos a Km/L
            val cityKmpl = mpgToKmpl(cityMpg)
            val highwayKmpl = mpgToKmpl(highwayMpg)
            val avgKmpl = mpgToKmpl(avgMpg)

            // Formateamos
            val cStr = String.format(Locale.US, "%.2f", cityKmpl)
            val hStr = String.format(Locale.US, "%.2f", highwayKmpl)
            val aStr = String.format(Locale.US, "%.2f", avgKmpl)

            return@withContext ConsumptionData(cStr, hStr, aStr)

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext ConsumptionData("0.0", "0.0", "0.0")
        }
    }

    // --- AUXILIARES ---

    // Descarga y parsea una lista simple de opciones XML (<menuItem><text>...</text><value>...</value></menuItem>)
    private fun parseXmlList(urlStr: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val doc = getXmlDocument(urlStr) ?: return emptyList()
            val nodes = doc.getElementsByTagName("value") // En los menús, 'value' es el nombre (Make/Model)

            for (i in 0 until nodes.length) {
                nodes.item(i)?.textContent?.let { list.add(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // Conexión HTTP genérica que devuelve un Documento XML
    private fun getXmlDocument(urlStr: String): Document? {
        return try {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000

            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(conn.inputStream)
            conn.disconnect()
            doc
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun mpgToKmpl(mpg: Double): Double {
        return mpg * 0.425144
    }
}