package com.example.consumocarros;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    public int capacidadcombustible;
    public Usuario usuarioconectado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recuperar usuario (si fue pasado desde otra Activity)
        usuarioconectado = (Usuario) getIntent().getSerializableExtra("usuario");

        // Referencias a los elementos del layout
        TextView textoincio = findViewById(R.id.textViewNombre);
        Button botoncuenta = findViewById(R.id.botoncuenta);

        // --- INICIO MODIFICACIÓN 1: Cambiado de 'Button' a 'View' ---
        View botonride = findViewById(R.id.botonRide);
        // --- FIN MODIFICACIÓN 1 ---

        Button botoncombustible = findViewById(R.id.botoncombustible);
        ImageButton logoButton = findViewById(R.id.logoButton);
        ImageButton homeButton = findViewById(R.id.homeButton);

        capacidadcombustible = 50;

        // Mostrar saludo si el usuario existe
        if (usuarioconectado != null) {
            textoincio.setText("Hola " + usuarioconectado.nombre);
        }

        // --- INICIO MODIFICACIÓN 2: Lógica del botón Ride actualizada ---
        // Acción del botón Ride (Ahora es el layout "Nuevo Viaje")
        botonride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Misma función que el logo: ir a Mis Coches
                Intent intent = new Intent(MainActivity.this, MisCochesActivity.class);
                intent.putExtra("usuario", usuarioconectado);
                startActivity(intent);
            }
        });
        // --- FIN MODIFICACIÓN 2 ---

        // Acción del botón del logo → abre la pantalla MisCoches
        logoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MisCochesActivity.class);
                intent.putExtra("usuario", usuarioconectado);
                startActivity(intent);
            }
        });

        // Acción del botón Home
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // No hace nada, ya estamos en Home
            }
        });
    }
}