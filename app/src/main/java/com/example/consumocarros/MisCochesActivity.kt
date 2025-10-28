package com.example.consumocarros

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MisCochesActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navView: NavigationView

    private lateinit var listView: ListView
    private lateinit var buttonAdd: Button
    private lateinit var editTextCar: EditText
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_coches)

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout_mis_coches)
        navView = findViewById(R.id.nav_view_mis_coches)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_mis_coches)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_inicio -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    drawerLayout.closeDrawers()
                }
                R.id.nav_mis_coches -> {
                    drawerLayout.closeDrawers() // ya estamos aquí
                }
            }
            true
        }

        // Views
        listView = findViewById(R.id.listViewCoches)
        buttonAdd = findViewById(R.id.buttonAdd)
        editTextCar = findViewById(R.id.editTextCar)

        val coches = CarStorage.getCars(this).toMutableList()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, coches)
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

        // Click largo para eliminar coche
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val car = adapter.getItem(position) ?: return@setOnItemLongClickListener true
            CarStorage.removeCar(this, car)
            adapter.remove(car)
            adapter.notifyDataSetChanged()
            true
        }

        // Click normal para consultar consumo
        listView.setOnItemClickListener { _, _, position, _ ->
            val car = adapter.getItem(position) ?: return@setOnItemClickListener
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
