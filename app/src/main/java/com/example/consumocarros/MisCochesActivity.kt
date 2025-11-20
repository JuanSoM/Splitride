package com.example.consumocarros

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MisCochesActivity : AppCompatActivity() {

    private lateinit var carsContainer: LinearLayout
    private var usuario: Usuario? = null
    private lateinit var saveManager: SaveManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mis_coches)

        saveManager = SaveManager(this)
        usuario = intent.getSerializableExtra("usuario") as? Usuario

        val logoButton = findViewById<ImageButton>(R.id.logoButton)
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        val addCarButton = findViewById<Button>(R.id.botonAnadir)
        carsContainer = findViewById(R.id.contenedorCoches)

        loadCarsFromUser()

        logoButton.setOnClickListener { /* sin acción */ }

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
            finish()
        }

        addCarButton.setOnClickListener { showAddCarDialog() }
    }

    private fun showAddCarDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialogo_anadir_coche, null)
        val inputBusqueda = dialogView.findViewById<EditText>(R.id.inputBusqueda)
        val listaSugerencias = dialogView.findViewById<ListView>(R.id.listaSugerencias)

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mutableListOf())
        listaSugerencias.adapter = adapter

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

        val dialog = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
            .setTitle("Añadir coche")
            .setView(dialogView)
            .setPositiveButton("Añadir", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        listaSugerencias.setOnItemClickListener { _, _, position, _ ->
            val seleccion = adapter.getItem(position)
            inputBusqueda.setText(seleccion)
            listaSugerencias.adapter = null
        }

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        positiveButton.setOnClickListener {
            val texto = inputBusqueda.text.toString().trim()
            val partes = texto.split(" ")

            if (partes.size >= 3) {
                positiveButton.isEnabled = false
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = false
                inputBusqueda.isEnabled = false
                positiveButton.text = "Cargando..."

                val year = partes.last()
                val model = partes.drop(1).dropLast(1).joinToString(" ")
                val brand = partes.first()

                CoroutineScope(Dispatchers.Main).launch {
                    val consumption = ApiHelper.getVehicleConsumption(brand, model, year)

                    // --- CORRECCIÓN DEFINITIVA ---
                    // ApiHelper ya devuelve km/L con punto decimal. Solo parseamos.
                    val cityKmpl = consumption.city.toDoubleOrNull() ?: 0.0
                    val highwayKmpl = consumption.highway.toDoubleOrNull() ?: 0.0
                    val avgKmpl = consumption.avg.toDoubleOrNull() ?: 0.0

                    val newCar = Usuario.Car(
                        brand, model, year,
                        String.format(Locale.US, "%.1f", cityKmpl),
                        String.format(Locale.US, "%.1f", highwayKmpl),
                        String.format(Locale.US, "%.1f", avgKmpl)
                    )

                    usuario?.agregarCoche(newCar)
                    addCarView(newCar)

                    if (usuario != null) {
                        saveManager.actualizarUsuario(usuario)
                    }

                    dialog.dismiss()
                }

            } else {
                Toast.makeText(this, "Introduce un coche válido (marca modelo año)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addCarView(car: Usuario.Car) {
        val card = TextView(this)

        val carText = "${car.brand} ${car.model} ${car.year}\n" +
                "Consumo (C/A/P): ${car.cityKmpl} / ${car.highwayKmpl} / ${car.avgKmpl} km/L"

        card.text = carText
        card.textSize = 18f
        card.setPadding(30)
        card.setBackgroundResource(R.drawable.rounded_menu)
        card.setTextColor(resources.getColor(android.R.color.white))

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 16)
        }
        card.layoutParams = params

        card.setOnClickListener {
            val intent = Intent(this@MisCochesActivity, MapActivity::class.java)
            intent.putExtra("usuario", usuario)
            intent.putExtra("selected_car", car)
            startActivity(intent)
        }

        card.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar coche")
                .setMessage("¿Quieres eliminar ${car.brand} ${car.model} (${car.avgKmpl} km/L)?")
                .setPositiveButton("Eliminar") { _, _ ->
                    usuario?.eliminarCoche(car)
                    carsContainer.removeView(card)

                    if (usuario != null) {
                        saveManager.actualizarUsuario(usuario)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }

        carsContainer.addView(card)
    }

    private fun loadCarsFromUser() {
        carsContainer.removeAllViews()
        usuario?.getCoches()?.forEach { car ->
            addCarView(car)
        }
    }
}
