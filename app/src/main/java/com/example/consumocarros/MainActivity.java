package com.example.consumocarros;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;  // 👈 importante para abrir otra Activity
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;  // 👈 importante si usas ImageButton

public class MainActivity extends AppCompatActivity {
    public int capacidadcombustible;
    public Usuario usuarioconectado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usuarioconectado = (Usuario) getIntent().getSerializableExtra("usuario");
        TextView textoincio = findViewById(R.id.textViewNombre);
        Button botoncuenta = findViewById(R.id.botoncuenta);
        Button botonride = findViewById(R.id.botonRide);
        Button botoncombustible = findViewById(R.id.botoncombustible);
        ImageButton logoButton = findViewById(R.id.logoButton);  // 👈 añadimos esto

        capacidadcombustible = 50;

        textoincio.setText("Hola " + usuarioconectado.nombre);

        botonride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // pasar a la selección de coche
            }
        });

        // 👇 Aquí añadimos el evento del logo
        logoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MisCochesActivity.class);
                startActivity(intent);
            }
        });
    }
}
