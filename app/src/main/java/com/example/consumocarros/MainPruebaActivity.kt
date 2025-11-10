package com.example.consumocarros

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity


class MainPruebaActivity : ComponentActivity() {

    private lateinit var editTextMake: EditText
    private lateinit var editTextModel: EditText
    private lateinit var editTextYear: EditText
    private lateinit var buttonBuscar: Button
    private lateinit var textViewResultado: TextView
    private lateinit var buttonMapa: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) //HAY QUE PONER AQUI EL LAYOUT DE JUAN

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
