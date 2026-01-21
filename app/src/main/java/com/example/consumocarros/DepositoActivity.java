package com.example.consumocarros;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class DepositoActivity extends AppCompatActivity {

    private Usuario usuarioconectado;
    private int llenado; // porcentaje actual
    private Usuario.Car cocheseleccionado;
    private CircularStateView circle;
    private TextView textoporcentaje;
    private SaveManager saveManager;
    
    // Variable para controlar la visualización (true = %, false = Litros)
    private boolean mostrarEnPorcentaje = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposito);

        saveManager = new SaveManager(this);

        // --- Referencias UI ---
        circle = findViewById(R.id.circleState);
        textoporcentaje = findViewById(R.id.textViewporcentaje);
        Button boton_llenado = findViewById(R.id.button_full);
        Spinner spinnercoches = findViewById(R.id.spinnercoches);
        
        // --- Barra de navegación ---
        ImageButton logoButton = findViewById(R.id.logoButton);
        ImageButton homeButton = findViewById(R.id.homeButton);
        ImageButton gasofaButton = findViewById(R.id.gasofaButton);
        ImageButton historialButton = findViewById(R.id.historialButton);

        // Usuario y coche
        usuarioconectado = (Usuario) getIntent().getSerializableExtra("usuario");
        cocheseleccionado = cochepordefecto();

        // --- Listener para cambiar entre % y Litros al pulsar el texto ---
        textoporcentaje.setOnClickListener(v -> {
            mostrarEnPorcentaje = !mostrarEnPorcentaje; // Alternar modo
            actualizarTextoDisplay(llenado); // Actualizar visualización inmediatamente
        });

        if (usuarioconectado != null && usuarioconectado.getCoches() != null) {
            List<Usuario.Car> listaCoches = usuarioconectado.getCoches();

            ArrayAdapter<Usuario.Car> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, listaCoches);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnercoches.setAdapter(adapter);

            int indicePorDefecto = 0;
            for (int i = 0; i < listaCoches.size(); i++) {
                if (listaCoches.get(i).equals(cocheseleccionado)) {
                    indicePorDefecto = i;
                    break;
                }
            }
            spinnercoches.setSelection(indicePorDefecto);

            spinnercoches.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    cocheseleccionado = listaCoches.get(position);
                    if (cocheseleccionado.getcapacidaddeposito() == -1) {
                        pedirCapacidad(cocheseleccionado);
                    } else {
                        llenado = cocheseleccionado.getCapacidadactual();
                        animarCircularYTexto(0, llenado);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        // --- Listeners de Navegación ---
        logoButton.setOnClickListener(view -> navigateTo(MisCochesActivity.class));
        homeButton.setOnClickListener(view -> navigateTo(MainActivity.class));
        gasofaButton.setOnClickListener(view -> { /* Ya estamos aquí */ });
        historialButton.setOnClickListener(view -> navigateTo(HistorialActivity.class));

        // Botón llenar al 100%
        boton_llenado.setOnClickListener(v -> {
            int anterior = llenado;
            llenado = 100;
            cocheseleccionado.setCapacidadactual(llenado);
            saveManager.actualizarUsuario(usuarioconectado);
            animarCircularYTexto(anterior, llenado);
        });

        animarCircularYTexto(0, llenado);
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(DepositoActivity.this, destination);
        intent.putExtra("usuario", usuarioconectado);
        startActivity(intent);
    }

    private Usuario.Car cochepordefecto() {
        if (usuarioconectado != null) {
            return usuarioconectado.cochemasusado();
        }
        return null;
    }

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
                cocheseleccionado.setCapacidaddeposito(capacidadIngresada);

                if (cocheseleccionado.getCapacidadactual() == -1) {
                    cocheseleccionado.setCapacidadactual(0);
                    llenado = 0;
                } else {
                    llenado = cocheseleccionado.getCapacidadactual();
                }

                saveManager.actualizarUsuario(usuarioconectado);
                dialog.dismiss();
                pedirPorcentaje();
            }
        });
    }

    private void pedirPorcentaje() {
        View view = getLayoutInflater().inflate(R.layout.dialog_input, null);
        TextView title = view.findViewById(R.id.dialogTitle);
        EditText input = view.findViewById(R.id.dialogInput);

        // 4️⃣ Regla: Modificado para permitir añadir gasolina compensando déficit
        SpannableString spannableTitle = new SpannableString("¿Cuánto porcentaje quieres añadir? (%)");
        spannableTitle.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spannableTitle.length(), 0);
        title.setText(spannableTitle);
        input.setHint("Cantidad a repostar");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Repostar", null)
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .create();

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(android.R.color.white));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(android.R.color.white));

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String texto = input.getText().toString();
            if (!texto.isEmpty()) {
                int añadido = Integer.parseInt(texto);
                int anterior = llenado;
                
                // 4️⃣ Regla: El nuevo nivel suma lo añadido al nivel actual (que puede ser negativo)
                llenado = anterior + añadido;
                if (llenado > 100) llenado = 100; // El tope máximo sigue siendo 100%

                cocheseleccionado.setCapacidadactual(llenado);

                saveManager.actualizarUsuario(usuarioconectado);
                animarCircularYTexto(anterior, llenado);
                dialog.dismiss();
            }
        });
    }

    private void animarCircularYTexto(int inicio, int fin) {
        ValueAnimator animator = ValueAnimator.ofInt(inicio, fin);
        animator.setDuration(800);

        animator.addUpdateListener(animation -> {
            int valor = (int) animation.getAnimatedValue();
            // Clamp para el gráfico circular (no suele soportar negativos visualmente)
            int valorGrafico = Math.max(0, valor);
            circle.setState(valorGrafico, false);
            
            // Actualizar color y texto usando la nueva lógica
            actualizarTextoDisplay(valor);
        });

        animator.start();
    }

    // Nuevo método para gestionar qué se muestra (% o Litros)
    private void actualizarTextoDisplay(int porcentaje) {
        // Clamp del color para evitar valores RGB inválidos con porcentajes negativos
        float fraction = Math.max(0f, Math.min(1f, porcentaje / 100f));
        int color = interpolateColor(Color.RED, Color.GREEN, fraction);
        textoporcentaje.setTextColor(color);

        if (mostrarEnPorcentaje) {
            textoporcentaje.setText(porcentaje + "%");
        } else {
            if (cocheseleccionado != null && cocheseleccionado.getcapacidaddeposito() > 0) {
                // Calcular litros: (Porcentaje / 100) * Capacidad Total
                double litros = (porcentaje / 100.0) * cocheseleccionado.getcapacidaddeposito();
                textoporcentaje.setText(String.format("%.1f L", litros));
            } else {
                textoporcentaje.setText("0 L");
            }
        }
    }

    private int interpolateColor(int start, int end, float fraction) {
        int r = Color.red(start) + Math.round((Color.red(end) - Color.red(start)) * fraction);
        int g = Color.green(start) + Math.round((Color.green(end) - Color.green(start)) * fraction);
        int b = Color.blue(start) + Math.round((Color.blue(end) - Color.blue(start)) * fraction);
        return Color.rgb(r, g, b);
    }
}
