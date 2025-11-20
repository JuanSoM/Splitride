package com.example.consumocarros;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem; 
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu; 
import android.widget.ScrollView; 
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast; 

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public int capacidadcombustible;
    public Usuario usuarioconectado;

    // --- Referencias para previews ---
    private CircularStateView circlePreview;
    private TextView textPercentPreview;
    private TextView textListaCochesPreview;
    // --- FIN ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recuperar usuario
        usuarioconectado = (Usuario) getIntent().getSerializableExtra("usuario");

        // Referencias a los elementos
        TextView textoincio = findViewById(R.id.textViewNombre); 
        Button botoncuenta = findViewById(R.id.botoncuenta); 
        View botonride = findViewById(R.id.botonRide);
        View botoncombustible = findViewById(R.id.botoncombustible);
        View botonMisCoches = findViewById(R.id.button3); // Ahora es un Layout
        ImageButton logoButton = findViewById(R.id.logoButton);
        ImageButton homeButton = findViewById(R.id.homeButton);
        ImageButton gasofaButton = findViewById(R.id.gasofaButton);

        // --- Referencias internas de los previews ---
        circlePreview = findViewById(R.id.mainCircleState);
        textPercentPreview = findViewById(R.id.mainPercentText);
        textListaCochesPreview = findViewById(R.id.listaCochesPreview);
        // --- FIN ---
        
        // --- CORRECCIÓN SCROLL ---
        ScrollView scrollView = findViewById(R.id.scrollView2);
        
        textoincio.setFocusable(true);
        textoincio.setFocusableInTouchMode(true);

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_UP);
                scrollView.smoothScrollTo(0,0);
                textoincio.requestFocus(); 
                textoincio.clearFocus(); 
            }
        });
        // --- FIN CORRECCIÓN ---

        if (usuarioconectado != null) {
            textoincio.setText("Hola " + usuarioconectado.nombre);
            
            // Actualizar previews
            actualizarGraficoCombustible();
            actualizarListaCochesPreview();
        }

        botoncuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, v);
                popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.profile_view) {
                            Toast.makeText(MainActivity.this, "Ver perfil (Próximamente)", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (id == R.id.profile_logout) {
                            logoutUser();
                            return true;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });

        botonride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MisCochesActivity.class);
                intent.putExtra("usuario", usuarioconectado);
                startActivity(intent);
            }
        });

        botoncombustible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DepositoActivity.class);
                intent.putExtra("usuario", usuarioconectado);
                startActivity(intent);
            }
        });

        // Click en la preview de "Mis Coches"
        botonMisCoches.setOnClickListener(new View.OnClickListener() {
             @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MisCochesActivity.class);
                intent.putExtra("usuario", usuarioconectado);
                startActivity(intent);
            }
        });

        gasofaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DepositoActivity.class);
                intent.putExtra("usuario", usuarioconectado);
                startActivity(intent);
            }
        });

        logoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MisCochesActivity.class);
                intent.putExtra("usuario", usuarioconectado);
                startActivity(intent);
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // No hace nada, ya estamos en Home
            }
        });
    }

    // --- MÉTODOS PARA ACTUALIZAR PREVIEWS ---
    private void actualizarGraficoCombustible() {
        if (usuarioconectado == null || circlePreview == null || textPercentPreview == null) return;

        Usuario.Car coche = usuarioconectado.cochemasusado();

        if (coche != null) {
            int porcentaje = coche.getCapacidadactual();
            if (porcentaje == -1) porcentaje = 0;

            circlePreview.setState(porcentaje, false);
            int color = interpolateColor(Color.RED, Color.GREEN, porcentaje / 100f);

            textPercentPreview.setText(porcentaje + "%");
            textPercentPreview.setTextColor(color);
        } else {
            textPercentPreview.setText("--%");
            circlePreview.setState(0, false);
        }
    }

    private void actualizarListaCochesPreview() {
        if (usuarioconectado == null || textListaCochesPreview == null) return;

        List<Usuario.Car> coches = usuarioconectado.getCoches();

        if (coches.isEmpty()) {
            textListaCochesPreview.setText("No tienes coches añadidos");
            return;
        }

        StringBuilder sb = new StringBuilder();
        // Mostramos un máximo de 3 coches para que quepa
        for (int i = 0; i < Math.min(coches.size(), 3); i++) {
            Usuario.Car coche = coches.get(i);
            sb.append("• ").append(coche.getBrand()).append(" ").append(coche.getModel()).append("\n");
        }
        if(coches.size() > 3){
            sb.append("...");
        }

        textListaCochesPreview.setText(sb.toString().trim());
    }

    private int interpolateColor(int start, int end, float fraction) {
        int r = Color.red(start) + Math.round((Color.red(end) - Color.red(start)) * fraction);
        int g = Color.green(start) + Math.round((Color.green(end) - Color.green(start)) * fraction);
        int b = Color.blue(start) + Math.round((Color.blue(end) - Color.blue(start)) * fraction);
        return Color.rgb(r, g, b);
    }
    // --- FIN ---

    private void logoutUser() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
