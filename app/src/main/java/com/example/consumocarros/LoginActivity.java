package com.example.consumocarros;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import java.util.List;

public class LoginActivity extends Activity {

    private SaveManager saveManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        saveManager = new SaveManager(this); // inicializar SaveManager

        TextView title = findViewById(R.id.textView);
        EditText username = findViewById(R.id.TextUsername);
        EditText password = findViewById(R.id.TextPassword);
        Button iniciar_sesion = findViewById(R.id.button_inicarsesion);
        Button registrarse = findViewById(R.id.button_registrarse);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.lexend_giga_medium);

        title.setTypeface(typeface);
        username.setTypeface(typeface);
        password.setTypeface(typeface);

        iniciar_sesion.setOnClickListener(v -> {
            String usuarioInput = username.getText().toString().trim();
            String contrasenaInput = password.getText().toString().trim();

            if (usuarioInput.isEmpty() || contrasenaInput.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Usuario usuario = saveManager.obtenerUsuario(usuarioInput,contrasenaInput);
            if (usuario != null) {
                Toast.makeText(this, "Login exitoso. Bienvenido " + usuario.getNombre(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }
        });

        registrarse.setOnClickListener(v ->
                Toast.makeText(this, "Función de registro aún no implementada", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Valida login buscando el usuario en el archivo JSON mediante SaveManager
     */


    /**
     * Guarda todos los cambios del usuario
     */
    /**public void guardarUsuario(Usuario usuario) {
        saveManager.guardarUsuario(usuario);
    }**/
}
