package com.example.consumocarros

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MisCochesActivity : AppCompatActivity() {

    private lateinit var carsContainer: LinearLayout
    private val carsList = mutableListOf<Car>()

    data class Car(val brand: String, val model: String, val year: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mis_coches)

        val logoButton = findViewById<ImageButton>(R.id.logoButton)
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        val addCarButton = findViewById<Button>(R.id.botonAnadir)
        carsContainer = findViewById(R.id.contenedorCoches)

        // Cargar coches guardados
        loadCars()

        // Toolbar
        logoButton.setOnClickListener { /* sin acción */ }
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Botón "Añadir coche"
        addCarButton.setOnClickListener {
            showAddCarDialog()
        }
    }

    /**
     * Diálogo inteligente para añadir coches
     */
    private fun showAddCarDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialogo_anadir_coche, null)
        val inputBusqueda = dialogView.findViewById<EditText>(R.id.inputBusqueda)
        val listaSugerencias = dialogView.findViewById<ListView>(R.id.listaSugerencias)

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mutableListOf())
        listaSugerencias.adapter = adapter

        // Actualizar sugerencias mientras escribe
        inputBusqueda.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.length >= 2) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val suggestions = ApiHelper.getSuggestions(query)
                        withContext(Dispatchers.Main) {
                            adapter.clear()
                            adapter.addAll(suggestions.take(5))
                        }
                    }
                } else {
                    adapter.clear()
                }
            }
        })

        val dialog = AlertDialog.Builder(this)
            .setTitle("Añadir coche")
            .setView(dialogView)
            .setPositiveButton("Añadir", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        // Al pulsar sobre una sugerencia
        listaSugerencias.setOnItemClickListener { _, _, position, _ ->
            val seleccion = adapter.getItem(position)
            inputBusqueda.setText(seleccion)
        }

        // Al pulsar "Añadir"
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val texto = inputBusqueda.text.toString().trim()
            val partes = texto.split(" ")

            if (partes.size >= 3) {
                val year = partes.last()
                val model = partes.drop(1).dropLast(1).joinToString(" ")
                val brand = partes.first()

                val newCar = Car(brand, model, year)
                carsList.add(newCar)
                addCarView(newCar)
                saveCars()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Introduce un coche válido (marca modelo año)", Toast.LENGTH_SHORT).show()
            }
        }
    }


    /**
     * Crear la vista visual de un coche
     */
    private fun addCarView(car: Car) {
        val card = TextView(this)
        card.text = "${car.brand} ${car.model} ${car.year}"
        card.textSize = 20f
        card.setPadding(30)
        card.setBackgroundResource(R.drawable.rounded_menu)
        card.setTextColor(resources.getColor(android.R.color.white))

        // Doble clic → mostrar consumo
        card.setOnClickListener(object : View.OnClickListener {
            private var lastClick = 0L
            override fun onClick(v: View?) {
                val now = System.currentTimeMillis()
                if (now - lastClick < 400) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = ApiHelper.getVehicleData(car.brand, car.model, car.year)
                        withContext(Dispatchers.Main) {
                            showResult(result)
                        }
                    }
                }
                lastClick = now
            }
        })

        // Mantener pulsado → eliminar coche
        card.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar coche")
                .setMessage("¿Quieres eliminar ${car.brand} ${car.model} ${car.year}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    carsList.remove(car)
                    carsContainer.removeView(card)
                    saveCars()
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }

        carsContainer.addView(card)
    }

    /**
     * Mostrar consumo del vehículo
     */
    private fun showResult(text: String) {
        AlertDialog.Builder(this)
            .setTitle("Consumo del vehículo")
            .setMessage(text)
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Guardar lista de coches
     */
    private fun saveCars() {
        val sharedPrefs = getSharedPreferences("MyCarsPrefs", Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (car in carsList) {
            val obj = JSONObject()
            obj.put("brand", car.brand)
            obj.put("model", car.model)
            obj.put("year", car.year)
            jsonArray.put(obj)
        }
        sharedPrefs.edit().putString("cars", jsonArray.toString()).apply()
    }

    /**
     * Cargar lista de coches guardados
     */
    private fun loadCars() {
        val sharedPrefs = getSharedPreferences("MyCarsPrefs", Context.MODE_PRIVATE)
        val jsonString = sharedPrefs.getString("cars", null)
        if (jsonString != null) {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val car = Car(
                    obj.getString("brand"),
                    obj.getString("model"),
                    obj.getString("year")
                )
                carsList.add(car)
                addCarView(car)
            }
        }
    }
}