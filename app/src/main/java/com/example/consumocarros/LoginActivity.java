package com.example.consumocarros;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.*;

import androidx.core.content.res.ResourcesCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView title = findViewById(R.id.textView);
        EditText username = findViewById(R.id.TextUsername);
        EditText password = findViewById(R.id.TextPassword);
        Button iniciar_sesion = findViewById(R.id.button_inicarsesion);
        Button registrarse = findViewById(R.id.button_registrarse);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.lexend_giga_medium);
        Typeface typeface2 = ResourcesCompat.getFont(this, R.font.lexend_giga_thin);

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

            Usuario usuario = validarLogin(usuarioInput, contrasenaInput);
            if (usuario != null) {
                Toast.makeText(this, "Login exitoso. Bienvenido " + usuario.getNombre(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }
        });

        // El botón de "registrarse" puedes implementarlo después
        registrarse.setOnClickListener(v ->
                Toast.makeText(this, "Función de registro aún no implementada", Toast.LENGTH_SHORT).show()
        );
    }

    private Usuario validarLogin(String usuarioInput, String contrasenaInput) {
        List<Usuario> usuarios = cargarUsuarios();
        for (Usuario u : usuarios) {
            if (u.getUsuario().equals(usuarioInput) && u.getContrasena().equals(contrasenaInput)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Carga los usuarios desde usuarios.json
     * y también los coches asociados a cada uno.
     */
    public List<Usuario> cargarUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        try {
            InputStream is = getAssets().open("usuarios.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Usuario usuario = new Usuario(
                        obj.getString("contrasena"),
                        obj.getString("usuario"),
                        obj.getString("nombre"),
                        obj.getString("apellidos")
                );

                // ✅ Ahora los coches son objetos con marca, modelo, año
                JSONArray cochesArray = obj.optJSONArray("coches");
                if (cochesArray != null) {
                    for (int j = 0; j < cochesArray.length(); j++) {
                        JSONObject cocheObj = cochesArray.getJSONObject(j);
                        String brand = cocheObj.optString("brand", "");
                        String model = cocheObj.optString("model", "");
                        String year = cocheObj.optString("year", "");
                        Usuario.Car car = new Usuario.Car(brand, model, year);
                        usuario.agregarCoche(car);
                    }
                }

                usuarios.add(usuario);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usuarios;
    }
}
