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

        // UI References
        val logoButton = findViewById<ImageButton>(R.id.logoButton)
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        val addCarButton = findViewById<Button>(R.id.botonAnadir)
        val gasofaButton = findViewById<ImageButton>(R.id.gasofaButton)
        carsContainer = findViewById(R.id.contenedorCoches)

        loadCarsFromUser()

        // Navegación
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

        // Botón Añadir
        addCarButton.setOnClickListener { showAddCarDialog() }
    }

    private fun showAddCarDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialogo_anadir_coche, null)

        // Referencias a inputs
        val tilYear = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilYear)
        val tilMake = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilMake)
        val tilModel = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilModel)

        val autoYear = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteYear)
        val autoMake = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteMake)
        val autoModel = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteModel)

        // Variables temporales
        var selectedYear = ""
        var selectedMake = ""
        var selectedModel = ""

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Añadir", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        // Estilos
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        // PASO 1: CARGAR AÑOS (Localmente, es instantáneo)
        val years = ApiHelper.getYearsList()
        val adapterYears = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, years)
        autoYear.setAdapter(adapterYears)

        // PASO 2: AL ELEGIR AÑO -> Cargar Marcas
        autoYear.setOnItemClickListener { parent, _, position, _ ->
            selectedYear = parent.getItemAtPosition(position) as String

            // Resetear siguientes
            selectedMake = ""
            selectedModel = ""
            autoMake.text.clear()
            autoModel.text.clear()
            tilModel.isEnabled = false
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

            // Activar y cargar Marcas
            tilMake.isEnabled = true
            autoMake.setText("Cargando...")
            autoMake.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                val makes = ApiHelper.getMakes(selectedYear)
                val adapter = ArrayAdapter(this@MisCochesActivity, android.R.layout.simple_dropdown_item_1line, makes)
                autoMake.setAdapter(adapter)

                autoMake.text.clear()
                autoMake.isEnabled = true
                autoMake.showDropDown()
            }
        }

        // PASO 3: AL ELEGIR MARCA -> Cargar Modelos
        autoMake.setOnItemClickListener { parent, _, position, _ ->
            selectedMake = parent.getItemAtPosition(position) as String

            // Resetear siguiente
            selectedModel = ""
            autoModel.text.clear()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

            // Activar y cargar Modelos
            tilModel.isEnabled = true
            autoModel.setText("Cargando...")
            autoModel.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                val models = ApiHelper.getModels(selectedYear, selectedMake)
                val adapter = ArrayAdapter(this@MisCochesActivity, android.R.layout.simple_dropdown_item_1line, models)
                autoModel.setAdapter(adapter)

                autoModel.text.clear()
                autoModel.isEnabled = true
                autoModel.showDropDown()
            }
        }

        // PASO 4: AL ELEGIR MODELO -> Activar Botón
        autoModel.setOnItemClickListener { parent, _, position, _ ->
            selectedModel = parent.getItemAtPosition(position) as String
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
        }

        // PASO 5: GUARDAR
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Buscando..."
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                // Llamamos a la API oficial para sacar el consumo
                val consumption = ApiHelper.getVehicleConsumption(selectedYear, selectedMake, selectedModel)

                // Si la API devuelve N/A, avisamos
                if (consumption.avg == "N/A" || consumption.avg == "0.0") {
                    Toast.makeText(this@MisCochesActivity, "Datos no disponibles para este modelo específico", Toast.LENGTH_LONG).show()
                }

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
        // ... (Este código es idéntico al que te pasé antes, el de la tarjeta negra) ...
        // ... Cópialo del mensaje anterior para mantener el formato completo ...
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