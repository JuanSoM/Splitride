package com.example.consumocarros

import android.app.AlertDialog
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

class MisCochesActivity : AppCompatActivity() {

    private lateinit var carsContainer: LinearLayout
    private var usuario: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mis_coches)

        usuario = intent.getSerializableExtra("usuario") as? Usuario

        val logoButton = findViewById<ImageButton>(R.id.logoButton)
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        val addCarButton = findViewById<Button>(R.id.botonAnadir)
        carsContainer = findViewById(R.id.contenedorCoches)

        // Cargar coches del usuario
        loadCarsFromUser()

        // Toolbar
        logoButton.setOnClickListener { /* sin acción */ }
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Botón "Añadir coche"
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

        val dialog = AlertDialog.Builder(this)
            .setTitle("Añadir coche")
            .setView(dialogView)
            .setPositiveButton("Añadir", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        listaSugerencias.setOnItemClickListener { _, _, position, _ ->
            val seleccion = adapter.getItem(position)
            inputBusqueda.setText(seleccion)
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val texto = inputBusqueda.text.toString().trim()
            val partes = texto.split(" ")

            if (partes.size >= 3) {
                val year = partes.last()
                val model = partes.drop(1).dropLast(1).joinToString(" ")
                val brand = partes.first()

                val newCar = Usuario.Car(brand, model, year)
                usuario?.agregarCoche(newCar)
                addCarView(newCar)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Introduce un coche válido (marca modelo año)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addCarView(car: Usuario.Car) {
        val card = TextView(this)
        card.text = "${car.brand} ${car.model} ${car.year}"
        card.textSize = 20f
        card.setPadding(30)
        card.setBackgroundResource(R.drawable.rounded_menu)
        card.setTextColor(resources.getColor(android.R.color.white))

        // Click → ir a pantalla mapa
        card.setOnClickListener {
            val intent = Intent(this@MisCochesActivity, MapActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        // Mantener pulsado → eliminar coche
        card.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar coche")
                .setMessage("¿Quieres eliminar ${car.brand} ${car.model} ${car.year}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    usuario?.eliminarCoche(car)
                    carsContainer.removeView(card)
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }

        carsContainer.addView(card)
    }

    private fun loadCarsFromUser() {
        usuario?.getCoches()?.forEach { car ->
            addCarView(car)
        }
    }
}
