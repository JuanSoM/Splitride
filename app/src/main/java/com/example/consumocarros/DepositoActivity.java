package com.example.consumocarros;

import android.animation.ValueAnimator;
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

    private Usuario usuarioconectado;
    private int capacidad;
    private int llenado; // porcentaje actual
    private Usuario.Car cocheseleccionado;
    private CircularStateView circle;
    private TextView textoporcentaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposito);

        // Referencias UI
        circle = findViewById(R.id.circleState);
        textoporcentaje = findViewById(R.id.textViewporcentaje);

        Button boton_llenado = findViewById(R.id.button_full);
        ImageButton logoButton = findViewById(R.id.logoButton);
        ImageButton homeButton = findViewById(R.id.homeButton);

        // Usuario conectado
        usuarioconectado = (Usuario) getIntent().getSerializableExtra("usuario");
        cocheseleccionado = cochepordefecto();

        // Botón logo
        logoButton.setOnClickListener(view -> {
            Intent intent = new Intent(DepositoActivity.this, MisCochesActivity.class);
            intent.putExtra("usuario", usuarioconectado);
            startActivity(intent);
        });

        // Botón Home
        homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(DepositoActivity.this, MainActivity.class);
            intent.putExtra("usuario", usuarioconectado);
            startActivity(intent);
        });

        // Botón llenar al 100%
        boton_llenado.setOnClickListener(v -> {
            int anterior = llenado;
            llenado = 100;
            cocheseleccionado.setCapacidadactual(llenado);
            animarCircularYTexto(anterior, llenado);
        });

        // Coche por defecto

        if (cocheseleccionado != null && cocheseleccionado.getcapacidaddeposito() == -1) {
            pedirCapacidad(cocheseleccionado);
        }else{
            llenado = cocheseleccionado.getCapacidadactual();
        }

        // Animación inicial
        animarCircularYTexto(0, llenado);
    }

    // Obtener coche más usado
    private Usuario.Car cochepordefecto() {
        if (usuarioconectado != null) {
            return usuarioconectado.cochemasusado();
        }
        return null;
    }

    // Pedir capacidad del depósito
    private void pedirCapacidad(Usuario.Car coche) {
        View view = getLayoutInflater().inflate(R.layout.dialog_input, null);
        TextView title = view.findViewById(R.id.dialogTitle);
        EditText input = view.findViewById(R.id.dialogInput);

        SpannableString spannableTitle = new SpannableString("Introduce la capacidad del depósito (L)");
        spannableTitle.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spannableTitle.length(), 0);
        title.setText(spannableTitle);
        input.setHint("Capacidad en litros");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .create();

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(android.R.color.white));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(android.R.color.white));

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String valor = input.getText().toString();
            if (!valor.isEmpty()) {
                int capacidadIngresada = Integer.parseInt(valor);
                coche.setCapacidaddeposito(capacidadIngresada);
                capacidad = capacidadIngresada;
                dialog.dismiss();
                pedirPorcentaje();
            }
        });
    }

    // Pedir porcentaje actual
    private void pedirPorcentaje() {
        View view = getLayoutInflater().inflate(R.layout.dialog_input, null);
        TextView title = view.findViewById(R.id.dialogTitle);
        EditText input = view.findViewById(R.id.dialogInput);

        SpannableString spannableTitle = new SpannableString("Porcentaje actual del depósito (%)");
        spannableTitle.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spannableTitle.length(), 0);
        title.setText(spannableTitle);
        input.setHint("0 - 100%");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Aceptar", null)
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .create();

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(android.R.color.white));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(android.R.color.white));

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String texto = input.getText().toString();
            if (!texto.isEmpty()) {
                int porcentaje = Integer.parseInt(texto);
                if (porcentaje < 0) porcentaje = 0;
                if (porcentaje > 100) porcentaje = 100;

                int anterior = llenado;
                llenado = porcentaje;
                animarCircularYTexto(anterior, llenado);
                dialog.dismiss();
            }
        });
    }

    // Animación sincronizada de círculo y TextView
    private void animarCircularYTexto(int inicio, int fin) {
        ValueAnimator animator = ValueAnimator.ofInt(inicio, fin);
        animator.setDuration(800);

        animator.addUpdateListener(animation -> {
            int valor = (int) animation.getAnimatedValue();

            // Actualizar círculo
            circle.setState(valor, false); // false: animación interna desactivada

            // Calcular color interpolado rojo-verde
            int color = interpolateColor(Color.RED, Color.GREEN, valor / 100f);

            // Actualizar TextView
            textoporcentaje.setText(valor + "%");
            textoporcentaje.setTextColor(color);
        });

        animator.start();
    }

    // Interpolación de color
    private int interpolateColor(int start, int end, float fraction) {
        int r = Color.red(start) + Math.round((Color.red(end) - Color.red(start)) * fraction);
        int g = Color.green(start) + Math.round((Color.green(end) - Color.green(start)) * fraction);
        int b = Color.blue(start) + Math.round((Color.blue(end) - Color.blue(start)) * fraction);
        return Color.rgb(r, g, b);
    }
}
