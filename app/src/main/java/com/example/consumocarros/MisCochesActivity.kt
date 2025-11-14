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
            intent.putExtra("usuario", usuario)
            startActivity(intent)
            finish()
        }

        addCarButton.setOnClickListener { showAddCarDialog() }
    }

    /**
     * MODIFICADO: Ahora llama a la API dentro de una Coroutine.
     */
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
            .setPositiveButton("Añadir", null) // El listener se pone después de .show()
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        listaSugerencias.setOnItemClickListener { _, _, position, _ ->
            val seleccion = adapter.getItem(position)
            inputBusqueda.setText(seleccion)
            listaSugerencias.adapter = null // Ocultar lista al seleccionar
        }

        // Obtenemos el botón de "Añadir"
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        positiveButton.setOnClickListener {
            val texto = inputBusqueda.text.toString().trim()
            val partes = texto.split(" ")

            if (partes.size >= 3) {
                // Deshabilitamos los botones para que el usuario no toque nada
                positiveButton.isEnabled = false
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = false
                inputBusqueda.isEnabled = false
                positiveButton.text = "Cargando..." // Feedback visual

                val year = partes.last()
                val model = partes.drop(1).dropLast(1).joinToString(" ")
                val brand = partes.first()

                // --- INICIO DE LÓGICA ASÍNCRONA ---
                CoroutineScope(Dispatchers.Main).launch {
                    // 1. Llamamos a la API en el hilo de IO
                    val consumption = ApiHelper.getVehicleConsumption(brand, model, year)

                    // 2. Creamos el coche con los datos (o "N/A")
                    val newCar = Usuario.Car(
                        brand, model, year,
                        consumption.city,
                        consumption.highway,
                        consumption.avg
                    )

                    // 3. Añadimos a la RAM
                    usuario?.agregarCoche(newCar)

                    // 4. Añadimos a la UI
                    addCarView(newCar)

                    // 5. Guardamos en disco
                    if (usuario != null) {
                        val listaCompleta = LoginActivity.cargarUsuarios(this@MisCochesActivity)
                        val usuarioIndex = listaCompleta.indexOfFirst { it.usuario == usuario!!.usuario }
                        if (usuarioIndex != -1) {
                            listaCompleta[usuarioIndex] = usuario
                            LoginActivity.guardarUsuarios(this@MisCochesActivity, listaCompleta)
                        }
                    }

                    // 6. Cerramos el diálogo
                    dialog.dismiss()
                }
                // --- FIN DE LÓGICA ASÍNCRONA ---

            } else {
                Toast.makeText(this, "Introduce un coche válido (marca modelo año)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * MODIFICADO: Muestra los datos de consumo.
     */
    private fun addCarView(car: Usuario.Car) {
        val card = TextView(this)

        // Texto con el consumo
        val carText = "${car.brand} ${car.model} ${car.year}\n" +
                "Consumo (C/A/P): ${car.cityKmpl} / ${car.highwayKmpl} / ${car.avgKmpl} km/L"

        card.text = carText
        card.textSize = 18f // Un poco más pequeño para que quepan dos líneas
        card.setPadding(30)
        card.setBackgroundResource(R.drawable.rounded_menu)
        card.setTextColor(resources.getColor(android.R.color.white))

        // Ajuste de márgenes
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 16) // 16dp de margen inferior
        }
        card.layoutParams = params

        // Click → ir a pantalla mapa
        card.setOnClickListener {
            val intent = Intent(this@MisCochesActivity, MapActivity::class.java)
            intent.putExtra("usuario", usuario)
            // TODO: Quizás quieras pasar el consumo del coche al MapActivity? (si)
            // intent.putExtra("car_consumption", car.getAvgKmpl())
            startActivity(intent)
        }

        // Mantener pulsado → eliminar coche
        card.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar coche")
                // Mensaje modificado
                .setMessage("¿Quieres eliminar ${car.brand} ${car.model} (${car.avgKmpl} km/L)?")
                .setPositiveButton("Eliminar") { _, _ ->
                    // 1. Elimina de la RAM
                    usuario?.eliminarCoche(car)
                    // 2. Elimina de la UI
                    carsContainer.removeView(card)

                    // 3. Guarda el cambio
                    if (usuario != null) {
                        val listaCompleta = LoginActivity.cargarUsuarios(this)
                        val usuarioIndex = listaCompleta.indexOfFirst { it.usuario == usuario!!.usuario }
                        if (usuarioIndex != -1) {
                            listaCompleta[usuarioIndex] = usuario
                            LoginActivity.guardarUsuarios(this, listaCompleta)
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }

        carsContainer.addView(card)
    }

    /**
     * MODIFICADO: Limpia la vista antes de recargar.
     */
    private fun loadCarsFromUser() {
        carsContainer.removeAllViews() // Evita duplicados al volver a esta pantalla
        usuario?.getCoches()?.forEach { car ->
            addCarView(car)
        }
    }
}