package com.example.consumocarros

import org.w3c.dom.Document
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.round

object ApiHelper {

    fun getVehicleData(make: String, model: String, year: String): String {
        return try {
            val optionsUrl = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/options?year=$year&make=$make&model=$model"
            val vehicleId = getFirstVehicleId(optionsUrl)
                ?: return "No se encontró información del vehículo."

            val dataUrl = "https://www.fueleconomy.gov/ws/rest/vehicle/$vehicleId"
            val doc = getXmlDocument(dataUrl)

            val cityMpg = doc.getElementsByTagName("city08").item(0)?.textContent?.toDoubleOrNull() ?: 0.0
            val highwayMpg = doc.getElementsByTagName("highway08").item(0)?.textContent?.toDoubleOrNull() ?: 0.0
            val avgMpg = (cityMpg + highwayMpg) / 2

            val cityKmpl = mpgToKmpl(cityMpg)
            val highwayKmpl = mpgToKmpl(highwayMpg)
            val avgKmpl = mpgToKmpl(avgMpg)

            """
                Consumo estimado de $make $model $year:
                - Ciudad: ${"%.2f".format(cityKmpl)} km/L
                - Autovía: ${"%.2f".format(highwayKmpl)} km/L
                - Promedio: ${"%.2f".format(avgKmpl)} km/L
            """.trimIndent()

        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun getXmlDocument(urlStr: String): Document {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        val inputStream = conn.inputStream
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
        conn.disconnect()
        return doc
    }

    private fun getFirstVehicleId(urlStr: String): String? {
        val doc = getXmlDocument(urlStr)
        val nodeList = doc.getElementsByTagName("value")
        return if (nodeList.length > 0) nodeList.item(0).textContent else null
    }

    private fun mpgToKmpl(mpg: Double): Double {
        return mpg * 1.60934 / 3.78541
    }
    fun getSuggestions(query: String): List<String> {
        val suggestions = mutableListOf<String>()
        val q = query.trim()
        if (q.isEmpty()) return suggestions

        try {
            // Buscamos en años recientes hacia atrás (ajusta rango si quieres)
            for (year in 2025 downTo 2005) {
                // Obtener marcas disponibles (por año)
                val makesUrl = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/make?year=$year"
                val makesDoc = getXmlDocument(makesUrl)
                val makesNodes = makesDoc.getElementsByTagName("value")

                for (i in 0 until makesNodes.length) {
                    val make = makesNodes.item(i).textContent ?: continue

                    // Obtener modelos para esa marca y año
                    val modelsUrl = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/model?year=$year&make=$make"
                    val modelsDoc = try {
                        getXmlDocument(modelsUrl)
                    } catch (e: Exception) {
                        continue
                    }
                    val modelNodes = modelsDoc.getElementsByTagName("value")

                    for (j in 0 until modelNodes.length) {
                        val model = modelNodes.item(j).textContent ?: continue

                        val full = "$make $model $year"
                        if (full.contains(q, ignoreCase = true)) {
                            suggestions.add(full)
                            if (suggestions.size >= 5) return suggestions
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignorar errores de red aquí; devolvemos lo que tengamos
        }
        return suggestions
    }



}
