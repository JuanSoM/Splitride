package com.example.consumocarros

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MisCochesActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var buttonAdd: Button
    private lateinit var buttonDelete: Button
    private lateinit var editTextCar: EditText
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_coches)

        listView = findViewById(R.id.listViewCoches)
        buttonAdd = findViewById(R.id.buttonAdd)
        buttonDelete = findViewById(R.id.buttonDelete)
        editTextCar = findViewById(R.id.editTextCar)

        val coches = CarStorage.getCars(this).toMutableList()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, coches)
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        listView.adapter = adapter

        // Añadir coche
        buttonAdd.setOnClickListener {
            val carName = editTextCar.text.toString().trim()
            if (carName.isNotEmpty()) {
                CarStorage.addCar(this, carName)
                adapter.add(carName)
                editTextCar.text.clear()
            }
        }

        // Borrar coche seleccionado
        buttonDelete.setOnClickListener {
            val position = listView.checkedItemPosition
            if (position != ListView.INVALID_POSITION) {
                val car = adapter.getItem(position)
                CarStorage.removeCar(this, car!!)
                adapter.remove(car)
                listView.clearChoices()
                adapter.notifyDataSetChanged()
            }
        }

        // 🔹 Nuevo: cuando haces clic en un coche, consulta su consumo
        listView.setOnItemClickListener { _, _, position, _ ->
            val car = adapter.getItem(position) ?: return@setOnItemClickListener

            // Intentamos extraer make, model y year del nombre
            val parts = car.split(" ")
            if (parts.size < 3) {
                showDialog("Formato inválido", "El coche debe tener formato 'Marca Modelo Año'. Ejemplo: Toyota Corolla 2020")
                return@setOnItemClickListener
            }

            val make = parts[0]
            val model = parts[1]
            val year = parts.last()

            CoroutineScope(Dispatchers.IO).launch {
                val result = ApiHelper.getVehicleData(make, model, year)
                withContext(Dispatchers.Main) {
                    showDialog("Consumo estimado", result)
                }
            }
        }
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}


