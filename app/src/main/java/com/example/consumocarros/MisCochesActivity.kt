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

        // Botón Home (con la corrección de sesión)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("usuario", usuario)
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

        // --- INICIO DE LA MODIFICACIÓN (GUARDADO PERMANENTE) ---
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val texto = inputBusqueda.text.toString().trim()
            val partes = texto.split(" ")

            if (partes.size >= 3) {
                val year = partes.last()
                val model = partes.drop(1).dropLast(1).joinToString(" ")
                val brand = partes.first()

                val newCar = Usuario.Car(brand, model, year)

                // 1. Añade el coche al objeto en RAM
                usuario?.agregarCoche(newCar)

                // 2. Actualiza la vista (UI)
                addCarView(newCar)

                // 3. Guarda el cambio permanentemente
                if (usuario != null) {
                    // Carga la lista completa de usuarios del disco
                    val listaCompleta = LoginActivity.cargarUsuarios(this)

                    // Busca al usuario actual en esa lista
                    val usuarioIndex = listaCompleta.indexOfFirst { it.usuario == usuario!!.usuario }

                    if (usuarioIndex != -1) {
                        // Reemplaza el usuario antiguo por el usuario modificado (con el coche nuevo)
                        listaCompleta[usuarioIndex] = usuario

                        // Guarda la lista completa actualizada en el disco
                        LoginActivity.guardarUsuarios(this, listaCompleta)
                    }
                }

                dialog.dismiss()
            } else {
                Toast.makeText(this, "Introduce un coche válido (marca modelo año)", Toast.LENGTH_SHORT).show()
            }
        }
        // --- FIN DE LA MODIFICACIÓN ---
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

        // Mantener pulsado → eliminar coche (¡Esto también necesitaría guardar!)
        card.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar coche")
                .setMessage("¿Quieres eliminar ${car.brand} ${car.model} ${car.year}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    // 1. Elimina de la RAM
                    usuario?.eliminarCoche(car)
                    // 2. Elimina de la UI
                    carsContainer.removeView(card)

                    // 3. ¡Guarda el cambio! (Igual que al añadir)
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

    private fun loadCarsFromUser() {
        // Limpia la vista antes de cargar, por si acaso
        carsContainer.removeAllViews()
        usuario?.getCoches()?.forEach { car ->
            addCarView(car)
        }
    }
}