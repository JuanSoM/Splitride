package com.example.consumocarros

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

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

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_mis_coches -> {
                    startActivity(Intent(this, MisCochesActivity::class.java))
                    drawerLayout.closeDrawers()
                }
            }
            true
        }

        editTextMake = findViewById(R.id.editTextMake)
        editTextModel = findViewById(R.id.editTextModel)
        editTextYear = findViewById(R.id.editTextYear)
        buttonBuscar = findViewById(R.id.buttonBuscar)
        textViewResultado = findViewById(R.id.textViewResultado)
        buttonMapa = findViewById(R.id.buttonMapa)

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

        buttonMapa.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }
}
