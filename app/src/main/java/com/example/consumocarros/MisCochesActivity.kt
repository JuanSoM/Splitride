package com.example.consumocarros

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MisCochesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Esta línea conecta tu clase Kotlin con tu archivo XML
        setContentView(R.layout.mis_coches)
    }
}