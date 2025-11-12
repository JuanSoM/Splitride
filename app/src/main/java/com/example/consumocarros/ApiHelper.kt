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
    /**
     * Devuelve hasta 5 sugerencias reales en formato "Make Model Year"
     * que respetan el orden Marca -> Modelo -> Año y usan prefijos (startsWith).
     * Ejemplos de query:
     *  "Vo" -> marcas que empiezan por "Vo" (Volkswagen...)
     *  "Volkswagen G" -> modelos que empiezan por "G" dentro de Volkswagen
     *  "Volkswagen Golf 2019" -> filtra también por año que empiece por "2019"
     */
    fun getSuggestions(query: String): List<String> {
        val suggestions = mutableListOf<String>()
        val q = query.trim()
        if (q.isEmpty()) return suggestions

        val tokens = q.split("\\s+".toRegex()).map { it.trim() }.filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return suggestions

        try {
            // Rango de años razonable (ajustable)
            for (year in 2025 downTo 2000) {
                val makesUrl = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/make?year=$year"
                val makesDoc = try { getXmlDocument(makesUrl) } catch (e: Exception) { continue }
                val makesNodes = makesDoc.getElementsByTagName("value")

                for (i in 0 until makesNodes.length) {
                    val make = makesNodes.item(i)?.textContent ?: continue
                    val brandToken = tokens[0]

                    // 🔹 Solo aceptar marcas que empiecen por el texto escrito
                    if (!make.startsWith(brandToken, ignoreCase = true)) continue

                    // Si solo se escribió la marca, mostramos algunos modelos populares
                    if (tokens.size == 1) {
                        val modelsUrl = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/model?year=$year&make=$make"
                        val modelsDoc = try { getXmlDocument(modelsUrl) } catch (e: Exception) { continue }
                        val modelNodes = modelsDoc.getElementsByTagName("value")

                        for (j in 0 until modelNodes.length) {
                            val model = modelNodes.item(j)?.textContent ?: continue
                            suggestions.add("$make $model $year")
                            if (suggestions.size >= 10) return suggestions
                        }
                        continue
                    }

                    // 🔹 Obtener modelos de la marca coincidente
                    val modelsUrl = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/model?year=$year&make=$make"
                    val modelsDoc = try { getXmlDocument(modelsUrl) } catch (e: Exception) { continue }
                    val modelNodes = modelsDoc.getElementsByTagName("value")

                    for (j in 0 until modelNodes.length) {
                        val model = modelNodes.item(j)?.textContent ?: continue
                        val modelToken = if (tokens.size >= 2) tokens[1] else ""

                        // Solo aceptar modelos que empiecen con el texto exacto del modelo escrito
                        if (modelToken.isNotEmpty() && !model.startsWith(modelToken, ignoreCase = true)) continue

                        // Si solo hay marca + modelo, añadir todas las combinaciones válidas de año
                        if (tokens.size == 2) {
                            val yearsUrl = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/year?make=$make&model=$model"
                            val yearsDoc = try { getXmlDocument(yearsUrl) } catch (e: Exception) { continue }
                            val yearNodes = yearsDoc.getElementsByTagName("value")

                            for (k in 0 until yearNodes.length) {
                                val y = yearNodes.item(k)?.textContent ?: continue
                                suggestions.add("$make $model $y")
                                if (suggestions.size >= 10) return suggestions
                            }
                            continue
                        }

                        // 🔹 Si se incluye el año (o parte del año)
                        if (tokens.size >= 3) {
                            val yearToken = tokens[2]
                            val yearsUrl = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/year?make=$make&model=$model"
                            val yearsDoc = try { getXmlDocument(yearsUrl) } catch (e: Exception) { continue }
                            val yearNodes = yearsDoc.getElementsByTagName("value")

                            for (k in 0 until yearNodes.length) {
                                val y = yearNodes.item(k)?.textContent ?: continue
                                // 🔹 Coincidencia estricta: el año debe empezar igual
                                if (y.startsWith(yearToken, ignoreCase = true)) {
                                    suggestions.add("$make $model $y")
                                    if (suggestions.size >= 10) return suggestions
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Devolver lo que se haya podido obtener
        }

        // 🔹 Filtro final: devolver solo sugerencias que comiencen realmente con el texto escrito
        // Esto elimina falsos positivos (como Golf R 2025 cuando escribes Golf 2)
        return suggestions.filter {
            it.lowercase().startsWith(q.lowercase())
        }.take(10)
    }




}