package com.example.consumocarros;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem; 
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu; 
import android.widget.ScrollView; 
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast; 

public class MainActivity extends AppCompatActivity {

    public int capacidadcombustible;
    public Usuario usuarioconectado;

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
        Button botoncombustible = findViewById(R.id.botoncombustible);
        ImageButton logoButton = findViewById(R.id.logoButton);
        ImageButton homeButton = findViewById(R.id.homeButton);
        
        // --- CORRECCIÓN SCROLL ---
        ScrollView scrollView = findViewById(R.id.scrollView2);
        
        // Hacemos que el título sea "enfocable" para atraer la vista arriba
        textoincio.setFocusable(true);
        textoincio.setFocusableInTouchMode(true);

        // Forzamos el scroll y el foco al inicio
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_UP);
                scrollView.smoothScrollTo(0,0);
                textoincio.requestFocus(); // Esto asegura que la pantalla suba
                textoincio.clearFocus(); // Quitamos el foco visual si es necesario
            }
        });
        // --- FIN CORRECCIÓN ---

        capacidadcombustible = 50;

        // Mostrar saludo
        if (usuarioconectado != null) {
            textoincio.setText("Hola " + usuarioconectado.nombre);
        }

        // --- INICIO DE CÓDIGO NUEVO: Lógica del botón de cuenta ---
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
        // --- FIN DE CÓDIGO NUEVO ---

        // Acción del botón Ride
        botonride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MisCochesActivity.class);
                intent.putExtra("usuario", usuarioconectado);
                startActivity(intent);
            }
        });

        // Acción del botón depositivo
        botoncombustible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DepositoActivity.class);
                intent.putExtra("usuario", usuarioconectado);
                startActivity(intent);
            }
        });

        // Acción del botón del logo
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

    // --- INICIO DE CÓDIGO NUEVO: Función para cerrar sesión ---
    private void logoutUser() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    // --- FIN DE CÓDIGO NUEVO ---
}
