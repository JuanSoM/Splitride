package com.example.consumocarros

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
        logoButton.setOnClickListener { /* no action */ }
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

    // Mostrar diálogo para añadir coche
    private fun showAddCarDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialogo_anadir_coche, null)
        val brandInput = dialogView.findViewById<EditText>(R.id.inputMarca)
        val modelInput = dialogView.findViewById<EditText>(R.id.inputModelo)
        val yearInput = dialogView.findViewById<EditText>(R.id.inputAño)

        AlertDialog.Builder(this)
            .setTitle("Añadir coche")
            .setView(dialogView)
            .setPositiveButton("Añadir") { _, _ ->
                val brand = brandInput.text.toString()
                val model = modelInput.text.toString()
                val year = yearInput.text.toString()

                if (brand.isNotBlank() && model.isNotBlank() && year.isNotBlank()) {
                    val newCar = Car(brand, model, year)
                    carsList.add(newCar)
                    addCarView(newCar)
                    saveCars()
                } else {
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Crear la vista visual del coche
    private fun addCarView(car: Car) {
        val card = TextView(this)
        card.text = "${car.brand} ${car.model} ${car.year}"
        card.textSize = 20f
        card.setPadding(30)
        card.setBackgroundResource(R.drawable.rounded_menu)
        card.setTextColor(resources.getColor(android.R.color.white))

        // --- DOBLE CLIC → mostrar consumo ---
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

        // --- MANTENER PULSADO → eliminar coche ---
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

    // Mostrar resultado de consumo
    private fun showResult(text: String) {
        AlertDialog.Builder(this)
            .setTitle("Consumo del vehículo")
            .setMessage(text)
            .setPositiveButton("OK", null)
            .show()
    }

    // --- GUARDAR Y CARGAR COCHES ---

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

