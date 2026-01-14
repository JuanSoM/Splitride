package com.example.consumocarros

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        // Referencias UI principales
        val logoButton = findViewById<ImageButton>(R.id.logoButton)
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        val addCarButton = findViewById<Button>(R.id.botonAnadir)
        val gasofaButton = findViewById<ImageButton>(R.id.gasofaButton)
        carsContainer = findViewById(R.id.contenedorCoches)

        // Cargar coches guardados al iniciar
        loadCarsFromUser()

        // --- NAVEGACIÓN ---
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
            finish()
        }
        gasofaButton.setOnClickListener {
            val intent = Intent(this@MisCochesActivity, DepositoActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }
        logoButton.setOnClickListener { /* Nada */ }

        // Botón para abrir el diálogo
        addCarButton.setOnClickListener { showAddCarDialog() }
    }

    private fun showAddCarDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialogo_anadir_coche, null)

        // Referencias a los campos del diálogo
        val tilMake = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilMake)
        val tilModel = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilModel)
        val tilYear = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilYear)

        val autoMake = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteMake)
        val autoModel = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteModel)
        val autoYear = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteYear)

        // Variables para guardar la selección
        var selectedMake = ""
        var selectedModel = ""
        var selectedYear = ""

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Añadir", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        // --- ESTILOS DEL DIÁLOGO ---
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE)

        // Estado inicial: Solo Marca habilitada
        tilModel.isEnabled = false
        tilYear.isEnabled = false

        // -----------------------------------------------------------
        // ZONA DE CAMBIOS Y DIAGNÓSTICO (1. CARGAR MARCAS)
        // -----------------------------------------------------------
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Mensaje 1: Iniciando conexión
                Toast.makeText(this@MisCochesActivity, "Conectando al servidor...", Toast.LENGTH_SHORT).show()

                val makes = ApiHelper.getMakes() // Llamada al servidor

                if (makes.isEmpty()) {
                    // Mensaje 2: Si la lista llega vacía
                    Toast.makeText(this@MisCochesActivity, "⚠ Recibida lista VACÍA", Toast.LENGTH_LONG).show()
                } else {
                    // Mensaje 3: Éxito
                    Toast.makeText(this@MisCochesActivity, "✅ Éxito: ${makes.size} marcas", Toast.LENGTH_SHORT).show()

                    // CAMBIO DE VISIBILIDAD: Usamos 'select_dialog_item' que suele verse mejor en fondo negro/blanco
                    val adapter = ArrayAdapter(this@MisCochesActivity, android.R.layout.select_dialog_item, makes)
                    autoMake.setAdapter(adapter)

                    // Aseguramos que salga con 1 letra
                    autoMake.threshold = 1
                }
            } catch (e: Exception) {
                // Mensaje 4: Error crítico
                Toast.makeText(this@MisCochesActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
        // -----------------------------------------------------------

        // 2. AL ELEGIR MARCA -> Cargar Modelos
        autoMake.setOnItemClickListener { parent, _, position, _ ->
            selectedMake = parent.getItemAtPosition(position) as String

            selectedModel = ""
            selectedYear = ""
            autoModel.text.clear()
            autoYear.text.clear()
            tilYear.isEnabled = false
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

            tilModel.isEnabled = true
            autoModel.setText("Cargando...")
            autoModel.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                val models = ApiHelper.getModels(selectedMake)
                // Aquí también aplicamos el cambio de diseño por si acaso
                val adapter = ArrayAdapter(this@MisCochesActivity, android.R.layout.select_dialog_item, models)
                autoModel.setAdapter(adapter)

                autoModel.text.clear()
                autoModel.isEnabled = true
                autoModel.showDropDown()
            }
        }

        // 3. AL ELEGIR MODELO -> Cargar Años
        autoModel.setOnItemClickListener { parent, _, position, _ ->
            selectedModel = parent.getItemAtPosition(position) as String

            selectedYear = ""
            autoYear.text.clear()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

            tilYear.isEnabled = true
            autoYear.setText("...")
            autoYear.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                val years = ApiHelper.getYears(selectedMake, selectedModel)
                val adapter = ArrayAdapter(this@MisCochesActivity, android.R.layout.select_dialog_item, years)
                autoYear.setAdapter(adapter)

                autoYear.text.clear()
                autoYear.isEnabled = true
                autoYear.showDropDown()
            }
        }

        // 4. AL ELEGIR AÑO -> Activar botón Añadir
        autoYear.setOnItemClickListener { parent, _, position, _ ->
            selectedYear = parent.getItemAtPosition(position) as String
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
        }

        // 5. BOTÓN AÑADIR -> Obtener Consumo y Guardar
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Consultando..."
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                // Obtenemos los datos finales desde MySQL
                val consumption = ApiHelper.getVehicleConsumption(selectedMake, selectedModel, selectedYear)

                val newCar = Usuario.Car(
                    selectedMake, selectedModel, selectedYear,
                    consumption.city,
                    consumption.highway,
                    consumption.avg
                )

                usuario?.agregarCoche(newCar)
                addCarView(newCar)
                usuario?.let { saveManager.actualizarUsuario(it) }
                dialog.dismiss()
            }
        }
    }

    private fun addCarView(car: Usuario.Car) {
        val card = TextView(this)

        val carText = "${car.brand} ${car.model} ${car.year}\n" +
                "Ciudad: ${car.cityKmpl} km/L\n" +
                "Carretera: ${car.highwayKmpl} km/L\n" +
                "-----------------\n" +
                "Promedio: ${car.avgKmpl} km/L"

        card.text = carText
        card.textSize = 16f
        card.setPadding(30, 30, 30, 30)

        card.setBackgroundResource(R.drawable.rounded_menu)
        card.setTextColor(resources.getColor(android.R.color.white, theme))

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
            val titleView = TextView(this)
            titleView.text = "Eliminar coche"
            titleView.textSize = 22f
            titleView.setTextColor(Color.WHITE)
            titleView.setPadding(50, 50, 50, 20)

            val dialog = AlertDialog.Builder(this)
                .setCustomTitle(titleView)
                .setMessage("¿Estás seguro de que quieres eliminar el ${car.brand} ${car.model}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    usuario?.eliminarCoche(car)
                    carsContainer.removeView(card)
                    usuario?.let { saveManager.actualizarUsuario(it) }
                }
                .setNegativeButton("Cancelar", null)
                .create()

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE)
            dialog.findViewById<TextView>(android.R.id.message)?.apply {
                setTextColor(Color.WHITE)
                textSize = 16f
            }
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