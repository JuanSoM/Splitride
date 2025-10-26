package com.example.consumocarros;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    public int capacidadcombustible;
    public Usuario usuarioconectado;

    //HAY QUE INICIALIZAR EL USUARIO DE MANERA REALISTA

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        usuarioconectado = new Usuario("12345","admin","Admin", "Adminez");
        TextView textoincio = findViewById(R.id.textViewNombre);
        Button botoncuenta = findViewById(R.id.botoncuenta);
        Button botonride = findViewById(R.id.botonRide);
        Button botoncombustible = findViewById(R.id.botoncombustible);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        capacidadcombustible = 50;

        textoincio.setText("Hola " + usuarioconectado.nombre);
        botonride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pasar a la selección de coche
            }
        });
    }
}
