package com.example.consumocarros

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding

class MisCochesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mis_coches)

        val logoButton = findViewById<ImageButton>(R.id.logoButton)
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        val addCarButton = findViewById<Button>(R.id.botonAnadir)
        val carsContainer = findViewById<LinearLayout>(R.id.contenedorCoches)

        // Toolbar
        logoButton.setOnClickListener { /* no action needed here */ }
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Al pulsar "Añadir coche"
        addCarButton.setOnClickListener {
            showAddCarDialog(carsContainer)
        }
    }

    private fun showAddCarDialog(container: LinearLayout) {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialogo_anadir_coche, null)
        val brandInput = dialogView.findViewById<EditText>(R.id.inputMarca)
        val modelInput = dialogView.findViewById<EditText>(R.id.inputModelo)
        val yearInput = dialogView.findViewById<EditText>(R.id.inputAño)

        AlertDialog.Builder(this)
            .setTitle("Añadir coche")
            .setView(dialogView)
            .setPositiveButton("Añadir") { _, _ ->
                val brand = brandInput.text.toString()
                val model = modelInput.text.toString()
                val year = yearInput.text.toString()
                if (brand.isNotBlank() && model.isNotBlank() && year.isNotBlank()) {
                    addCar(container, brand, model, year)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addCar(container: LinearLayout, brand: String, model: String, year: String) {
        val card = TextView(this)
        card.text = "$brand $model $year"
        card.textSize = 20f
        card.setPadding(30)
        card.setBackgroundResource(R.drawable.rounded_menu)
        card.setTextColor(resources.getColor(android.R.color.white))
        card.setOnClickListener(object : View.OnClickListener {
            private var lastClick = 0L
            override fun onClick(v: View?) {
                val now = System.currentTimeMillis()
                if (now - lastClick < 400) {
                    // Doble clic → consultar consumo
                    Thread {
                        val result = ApiHelper.getVehicleData(brand, model, year)
                        runOnUiThread {
                            showResult(result)
                        }
                    }.start()
                }
                lastClick = now
            }
        })

        container.addView(card)
    }

    private fun showResult(text: String) {
        AlertDialog.Builder(this)
            .setTitle("Consumo del vehículo")
            .setMessage(text)
            .setPositiveButton("OK", null)
            .show()
    }
}
