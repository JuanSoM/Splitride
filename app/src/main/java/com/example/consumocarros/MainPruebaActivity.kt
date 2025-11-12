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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainprueba)

        editTextMake = findViewById(R.id.editTextMake)
        editTextModel = findViewById(R.id.editTextModel)
        editTextYear = findViewById(R.id.editTextYear)
        buttonBuscar = findViewById(R.id.buttonBuscar)
        textViewResultado = findViewById(R.id.textViewResultado)

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
    }
}