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
        Button botonride = findViewById(R.id.botonRide);
        Button botoncombustible = findViewById(R.id.botoncombustible);
        ImageButton logoButton = findViewById(R.id.logoButton);
        ImageButton homeButton = findViewById(R.id.homeButton);  // 👈 nuevo botón

        capacidadcombustible = 50;

        // Mostrar saludo si el usuario existe
        if (usuarioconectado != null) {
            textoincio.setText("Hola " + usuarioconectado.nombre);
        }

        // Acción del botón Ride
        botonride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Aquí podrías pasar a la selección de coche
            }
        });

        // 👇 Acción del botón del logo → abre la pantalla MisCoches
        logoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MisCochesActivity.class);
                intent.putExtra("usuario", usuarioconectado);
                startActivity(intent);
            }
        });

        // 👇 Acción del botón Home → ya estamos en MainActivity, así que no hace falta cambiar de pantalla
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Si quisieras refrescar la pantalla, podrías hacer algo como:
                // recreate();
                // Pero normalmente aquí no se hace nada
            }
        });
    }
}