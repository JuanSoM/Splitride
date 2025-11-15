package com.example.consumocarros;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DepositoActivity extends AppCompatActivity {

    Usuario usuarioconectado;
    int capacidad;
    int llenado;//en porcentaje
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposito);
        Button boton_llenado = findViewById(R.id.button_full);
        ImageButton logoButton = findViewById(R.id.logoButton);
        ImageButton homeButton = findViewById(R.id.homeButton);



        // Acción del botón del logo
        logoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DepositoActivity.this, MisCochesActivity.class);
                intent.putExtra("usuario", usuarioconectado);
                startActivity(intent);
            }
        });

        // Acción del botón Home
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DepositoActivity.this, MainActivity.class);
                intent.putExtra("usuario", usuarioconectado);
                startActivity(intent);
            }
        });

        boton_llenado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llenado = 100;
                actualizarimagen();

            }
        });

        usuarioconectado = (Usuario) getIntent().getSerializableExtra("usuario");
        Usuario.Car cochepordefecto = cochepordefceto();
        if(cochepordefecto.getcapacidaddeposito() == -1){
            pedirCapacidad(cochepordefecto);
        }
        // Obtener la vista del XML
        CircularStateView circle = findViewById(R.id.circleState);

        // Valor de prueba: 33
        circle.setState(llenado);
    }

    public Usuario.Car cochepordefceto(){
        return usuarioconectado.cochemasusado();
    }

    private void pedirCapacidad(Usuario.Car coche) {

        // Inflar diseño personalizado
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_input, null);

        TextView title = view.findViewById(R.id.dialogTitle);
        EditText input = view.findViewById(R.id.dialogInput);

        SpannableString spannableTitle = new SpannableString("Introduce la capacidad del depósito (L)");
        spannableTitle.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spannableTitle.length(), 0);

        title.setText(spannableTitle);


        input.setHint("Capacidad en litros");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Guardar", (d, w) -> {
                    String valor = input.getText().toString();
                    if (!valor.isEmpty()) {
                        int capacidadIngresada = Integer.parseInt(valor);

                        coche.setCapacidaddeposito(capacidadIngresada);
                        capacidad = capacidadIngresada;

                        pedirPorcentaje();
                    }
                })
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .create();

        dialog.show();

        // botones negros
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(android.R.color.white));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(android.R.color.white));
    }

    private void pedirPorcentaje() {

        // Inflar layout personalizado
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_input, null);

        TextView title = view.findViewById(R.id.dialogTitle);
        EditText input = view.findViewById(R.id.dialogInput);

        // Asignar título en blanco usando SpannableString
        SpannableString spannableTitle = new SpannableString("Porcentaje actual del depósito (%)");
        spannableTitle.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spannableTitle.length(), 0);
        title.setText(spannableTitle);

        // Placeholder del input
        input.setHint("0 - 100%");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Aceptar", (d, w) -> {
                    String texto = input.getText().toString();
                    if (!texto.isEmpty()) {
                        int porcentaje = Integer.parseInt(texto);
                        llenado = porcentaje;
                        CircularStateView circle = findViewById(R.id.circleState);
                        circle.setState(porcentaje);
                    }
                })
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .create();

        dialog.show();

        // Fondo redondeado negro
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        // Botones blancos
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(android.R.color.white));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(android.R.color.white));
    }

    private void actualizarimagen(){
        CircularStateView circle = findViewById(R.id.circleState);

        // Valor de prueba: 33
        circle.setState(llenado);
    }



}
