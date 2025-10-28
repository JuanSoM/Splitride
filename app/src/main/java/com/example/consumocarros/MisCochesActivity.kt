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
    private lateinit var editTextCar: EditText
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_coches)

        listView = findViewById(R.id.listViewCoches)
        buttonAdd = findViewById(R.id.buttonAdd)
        editTextCar = findViewById(R.id.editTextCar)

        val coches = CarStorage.getCars(this).toMutableList()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, coches)
        listView.adapter = adapter

        // ➕ Añadir coche
        buttonAdd.setOnClickListener {
            val carName = editTextCar.text.toString().trim()
            if (carName.isNotEmpty()) {
                CarStorage.addCar(this, carName)
                adapter.add(carName)
                editTextCar.text.clear()
            }
        }

        // 👆 Click corto → mostrar consumo
        listView.setOnItemClickListener { _, _, position, _ ->
            val car = adapter.getItem(position) ?: return@setOnItemClickListener

            val parts = car.split(" ")
            if (parts.size < 3) {
                showDialog("Formato inválido", "Usa el formato: 'Marca Modelo Año' (Ej: Toyota Corolla 2020)")
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

        // ✋ Click largo → eliminar coche
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val car = adapter.getItem(position) ?: return@setOnItemLongClickListener true

            AlertDialog.Builder(this)
                .setTitle("Eliminar coche")
                .setMessage("¿Deseas eliminar \"$car\" de tu lista?")
                .setPositiveButton("Sí") { _, _ ->
                    CarStorage.removeCar(this, car)
                    adapter.remove(car)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Coche eliminado", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()

            true
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

