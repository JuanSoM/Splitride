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
    // Cache: año -> lista de marcas
    private val makeCache = mutableMapOf<Int, List<String>>()
    private val lock = Any()

    fun getSuggestions(query: String): List<String> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()

        val tokens = q.split("\\s+".toRegex())
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.lowercase() }

        if (tokens.isEmpty()) return emptyList()

        val suggestions = mutableListOf<String>()
        val seen = mutableSetOf<String>() // evitar duplicados

        // Rango razonable: últimos 11 años
        for (year in 2025 downTo 2015) {
            if (suggestions.size >= 10) break

            val makes = getMakesForYear(year) ?: continue

            for (make in makes) {
                val makeLower = make.lowercase()

                // 1. La marca debe contener el primer token
                if (!containsToken(makeLower, tokens[0])) continue

                // Si solo hay marca → sugerir modelos populares
                if (tokens.size == 1) {
                    addPopularModels(make, year, suggestions, seen)
                    continue
                }

                // 2. Buscar modelos que contengan el segundo token
                val models = getModelsForMakeYear(make, year) ?: continue
                for (model in models) {
                    val modelLower = model.lowercase()
                    if (!containsToken(modelLower, tokens[1])) continue

                    // Caso: marca + modelo → sugerir años
                    if (tokens.size == 2) {
                        addYearsForModel(make, model, year, tokens, suggestions, seen)
                        continue
                    }

                    // Caso: marca + modelo + año → filtrar años
                    if (tokens.size >= 3) {
                        val yearToken = tokens[2]
                        val years = getYearsForMakeModel(make, model) ?: continue
                        for (y in years) {
                            if (y.startsWith(yearToken, ignoreCase = true)) {
                                addSuggestion("$make $model $y", suggestions, seen)
                            }
                        }
                    }
                }
            }
        }

        // Filtro final: debe contener TODOS los tokens
        return suggestions
            .filter { sug ->
                val lower = sug.lowercase()
                tokens.all { token -> lower.contains(token) }
            }
            .take(10)
    }

    // --- FUNCIONES AUXILIARES ---

    private fun getMakesForYear(year: Int): List<String>? = synchronized(lock) {
        makeCache[year] ?: run {
            val url = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/make?year=$year"
            try {
                val doc = getXmlDocument(url)
                val nodes = doc.getElementsByTagName("value")
                val list = mutableListOf<String>()
                for (i in 0 until nodes.length.coerceAtMost(50)) { // limitar
                    nodes.item(i)?.textContent?.let { list.add(it) }
                }
                makeCache[year] = list
                list
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun getModelsForMakeYear(make: String, year: Int): List<String>? {
        val url = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/model?year=$year&make=$make"
        return try {
            val doc = getXmlDocument(url)
            val nodes = doc.getElementsByTagName("value")
            val list = mutableListOf<String>()
            for (i in 0 until nodes.length.coerceAtMost(30)) {
                nodes.item(i)?.textContent?.let { list.add(it) }
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    private fun getYearsForMakeModel(make: String, model: String): List<String>? {
        val url = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/year?make=$make&model=$model"
        return try {
            val doc = getXmlDocument(url)
            val nodes = doc.getElementsByTagName("value")
            val list = mutableListOf<String>()
            for (i in 0 until nodes.length) {
                nodes.item(i)?.textContent?.let { list.add(it) }
            }
            list.sortedDescending() // más nuevos primero
        } catch (e: Exception) {
            null
        }
    }

    private fun addPopularModels(make: String, year: Int, suggestions: MutableList<String>, seen: MutableSet<String>) {
        val models = getModelsForMakeYear(make, year) ?: return
        // Tomar hasta 3 modelos (puedes ordenar por popularidad si tienes datos)
        models.take(3).forEach { model ->
            addSuggestion("$make $model $year", suggestions, seen)
        }
    }

    private fun addYearsForModel(make: String, model: String, currentYear: Int, tokens: List<String>, suggestions: MutableList<String>, seen: MutableSet<String>) {
        val years = getYearsForMakeModel(make, model) ?: return
        // Filtrar años que contengan el token (si hay) o tomar más recientes
        val filtered = if (tokens.size > 2) {
            years.filter { it.startsWith(tokens[2], ignoreCase = true) }
        } else {
            years.sortedDescending(). distinct().take(5)
        }
        filtered.forEach { y ->
            addSuggestion("$make $model $y", suggestions, seen)
        }
    }

    private fun addSuggestion(text: String, suggestions: MutableList<String>, seen: MutableSet<String>) {
        if (text !in seen && suggestions.size < 15) { // buffer
            seen.add(text)
            suggestions.add(text)
        }
    }

    private fun containsToken(text: String, token: String): Boolean {
        return text.startsWith(token) || fuzzyMatch(text, token)
    }

    // Búsqueda difusa simple: permite 1 error
    private fun fuzzyMatch(text: String, token: String): Boolean {
        if (token.length > text.length + 1) return false
        var errors = 0
        var i = 0
        var j = 0
        while (i < token.length && j < text.length) {
            if (token[i].lowercaseChar() != text[j].lowercaseChar()) {
                errors++
                if (errors > 1) return false
                j++ // permite omitir un carácter
            } else {
                i++
                j++
            }
        }
        return i == token.length
    }


}