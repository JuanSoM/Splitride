package com.example.consumocarros;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public int capacidadcombustible;
    public Usuario usuarioconectado;

    //HAY QUE INICIALIZAR EL USUARIO DE MANERA REALISTA

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usuarioconectado = (Usuario) getIntent().getSerializableExtra("usuario");

        TextView textoincio = findViewById(R.id.textViewNombre);
        textoincio.setText("Hola " + usuarioconectado.nombre);

        Button botonride = findViewById(R.id.botonRide);
        Button botoncombustible = findViewById(R.id.botoncombustible);
        Button botoncuenta = findViewById(R.id.botoncuenta);

        //Toolbar toolbar = findViewById(R.id.toolbar);
        capacidadcombustible = 50;

        botonride.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });
    }
}
