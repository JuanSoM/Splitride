package com.example.consumocarros;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {

    private static final String FILENAME = "usuarios_guardados.json";

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

        registrarse.setOnClickListener(v ->
                Toast.makeText(this, "Función de registro aún no implementada", Toast.LENGTH_SHORT).show()
        );
    }

    private Usuario validarLogin(String usuarioInput, String contrasenaInput) {
        List<Usuario> usuarios = cargarUsuarios(this);
        for (Usuario u : usuarios) {
            if (u.getUsuario().equals(usuarioInput) && u.getContrasena().equals(contrasenaInput)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Parsea un String JSON a una Lista de Usuarios.
     * MODIFICADO: Lee los nuevos campos de consumo.
     */
    private static List<Usuario> parseUsuariosJson(String json) {
        List<Usuario> usuarios = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Usuario usuario = new Usuario(
                        obj.getString("contrasena"),
                        obj.getString("usuario"),
                        obj.getString("nombre"),
                        obj.getString("apellidos")
                );

                JSONArray cochesArray = obj.optJSONArray("coches");
                if (cochesArray != null) {
                    for (int j = 0; j < cochesArray.length(); j++) {
                        JSONObject cocheObj = cochesArray.getJSONObject(j);
                        String brand = cocheObj.optString("brand", "");
                        String model = cocheObj.optString("model", "");
                        String year = cocheObj.optString("year", "");

                        // --- LECTURA DE CAMPOS NUEVOS ---
                        // Usamos optString con "N/A" como valor por defecto
                        String city = cocheObj.optString("cityKmpl", "N/A");
                        String highway = cocheObj.optString("highwayKmpl", "N/A");
                        String avg = cocheObj.optString("avgKmpl", "N/A");

                        // Pasamos los nuevos campos al constructor
                        Usuario.Car car = new Usuario.Car(brand, model, year, city, highway, avg);
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

    /**
     * Guarda la lista completa de usuarios en el almacenamiento interno.
     * MODIFICADO: Escribe los nuevos campos de consumo.
     */
    public static void guardarUsuarios(Context context, List<Usuario> usuarios) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (Usuario u : usuarios) {
                JSONObject obj = new JSONObject();
                obj.put("usuario", u.getUsuario());
                obj.put("contrasena", u.getContrasena());
                obj.put("nombre", u.getNombre());
                obj.put("apellidos", u.getApellidos());

                JSONArray cochesArray = new JSONArray();
                for (Usuario.Car car : u.getCoches()) {
                    JSONObject cocheObj = new JSONObject();
                    cocheObj.put("brand", car.getBrand());
                    cocheObj.put("model", car.getModel());
                    cocheObj.put("year", car.getYear());

                    // --- ESCRITURA DE CAMPOS NUEVOS ---
                    cocheObj.put("cityKmpl", car.getCityKmpl());
                    cocheObj.put("highwayKmpl", car.getHighwayKmpl());
                    cocheObj.put("avgKmpl", car.getAvgKmpl());

                    cochesArray.put(cocheObj);
                }
                obj.put("coches", cochesArray);
                jsonArray.put(obj);
            }

            FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(jsonArray.toString(4)); // indentado
            writer.close();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Carga usuarios (lógica sin cambios, depende de parseUsuariosJson)
     */
    public static List<Usuario> cargarUsuarios(Context context) {
        File file = context.getFileStreamPath(FILENAME);
        String json;

        try {
            if (!file.exists()) {
                InputStream is = context.getAssets().open("usuarios.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, StandardCharsets.UTF_8);

                List<Usuario> usuarios = parseUsuariosJson(json);
                guardarUsuarios(context, usuarios);
                return usuarios;

            } else {
                FileInputStream fis = context.openFileInput(FILENAME);
                int size = fis.available();
                byte[] buffer = new byte[size];
                fis.read(buffer);
                fis.close();
                json = new String(buffer, StandardCharsets.UTF_8);

                return parseUsuariosJson(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}