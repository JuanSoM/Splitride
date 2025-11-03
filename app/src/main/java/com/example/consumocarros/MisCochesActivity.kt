package com.example.consumocarros

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MisCochesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mis_coches)

        // Referencias a los botones del toolbar
        val logoButton = findViewById<ImageButton>(R.id.logoButton)
        val homeButton = findViewById<ImageButton>(R.id.homeButton)

        // 🔹 Logo → no hace nada porque ya estamos en "Mis Coches"
        logoButton.setOnClickListener {
            // No hacemos nada o podrías recargar la pantalla si quisieras
        }

        // 🔹 Home → volver a la actividad principal
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // opcional: cierra esta pantalla para que el botón atrás no regrese aquí
        }
    }
}
