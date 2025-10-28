package com.example.consumocarros

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var editTextMake: EditText
    private lateinit var editTextModel: EditText
    private lateinit var editTextYear: EditText
    private lateinit var buttonBuscar: Button
    private lateinit var textViewResultado: TextView
    private lateinit var buttonMapa: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🔹 Vinculamos vistas
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 🔹 Activamos el botón del menú (hamburguesa)
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 🔹 Configuración del menú lateral
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_mis_coches -> {
                    startActivity(Intent(this, MisCochesActivity::class.java))
                    drawerLayout.closeDrawers()
                }
                R.id.nav_inicio -> {
                    drawerLayout.closeDrawers()
                }
                // Aquí puedes añadir más opciones del menú si las creas en el XML
            }
            true
        }

        // 🔹 Inicializamos vistas del contenido principal
        editTextMake = findViewById(R.id.editTextMake)
        editTextModel = findViewById(R.id.editTextModel)
        editTextYear = findViewById(R.id.editTextYear)
        buttonBuscar = findViewById(R.id.buttonBuscar)
        textViewResultado = findViewById(R.id.textViewResultado)
        buttonMapa = findViewById(R.id.buttonMapa)

        // 🔹 Lógica de búsqueda
        buttonBuscar.setOnClickListener {
            val make = editTextMake.text.toString().trim()
            val model = editTextModel.text.toString().trim()
            val year = editTextYear.text.toString().trim()

            if (make.isEmpty() || model.isEmpty() || year.isEmpty()) {
                textViewResultado.text = "Por favor completa todos los campos."
                return@setOnClickListener
            }

            Thread {
                val result = ApiHelper.getVehicleData(make, model, year)
                runOnUiThread {
                    textViewResultado.text = result
                }
            }.start()
        }

        // 🔹 Ir al mapa (si lo tienes)
        buttonMapa.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }

    // 🔹 Sincroniza el botón del menú con el Drawer
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    // 🔹 Maneja la apertura/cierre del menú al pulsar el botón “hamburguesa”
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
