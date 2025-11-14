package com.example.consumocarros;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class DepositoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposito); // <-- CAMBIA ESTO

        // Obtener la vista del XML
        CircularStateView circle = findViewById(R.id.circleState);

        // Valor de prueba: 33
        circle.setState(33);
    }
}
