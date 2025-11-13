package com.example.consumocarros;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem; // --- NUEVO: Import para el menú
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu; // --- NUEVO: Import para el menú desplegable
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast; // --- NUEVO: Import para el Toast

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
        Button botoncuenta = findViewById(R.id.botoncuenta); // Botón de cuenta
        View botonride = findViewById(R.id.botonRide);
        Button botoncombustible = findViewById(R.id.botoncombustible);
        ImageButton logoButton = findViewById(R.id.logoButton);
        ImageButton homeButton = findViewById(R.id.homeButton);

        capacidadcombustible = 50;

        // Mostrar saludo
        if (usuarioconectado != null) {
            textoincio.setText("Hola " + usuarioconectado.nombre);
        }

        // --- INICIO DE CÓDIGO NUEVO: Lógica del botón de cuenta ---
        botoncuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Crear el PopupMenu y anclarlo al botón (v)
                PopupMenu popup = new PopupMenu(MainActivity.this, v);

                // 2. "Inflar" (cargar) el menú XML que creamos
                popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

                // 3. Añadir el listener para saber qué opción se ha pulsado
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();

                        if (id == R.id.profile_view) {
                            // Acción para "Ver perfil" (de momento un aviso)
                            Toast.makeText(MainActivity.this, "Ver perfil (Próximamente)", Toast.LENGTH_SHORT).show();
                            return true;

                        } else if (id == R.id.profile_logout) {
                            // Acción para "Cerrar sesión"
                            logoutUser();
                            return true;
                        }
                        return false;
                    }
                });

                // 4. Mostrar el menú
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
    /**
     * Cierra la sesión y redirige al Login.
     * Limpia el historial de "atrás" para que el usuario no pueda volver.
     */
    private void logoutUser() {
        // Creamos el Intent para ir a LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);

        // --- ¡ESTA ES LA PARTE MÁS IMPORTANTE! ---
        // Estas "flags" limpian el historial de pantallas (la "pila de activities").
        // Esto evita que el usuario pulse "Atrás" en la pantalla de Login
        // y vuelva a entrar a MainActivity sin loguearse.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Iniciamos LoginActivity
        startActivity(intent);

        // Cerramos MainActivity
        finish();
    }
    // --- FIN DE CÓDIGO NUEVO ---
}