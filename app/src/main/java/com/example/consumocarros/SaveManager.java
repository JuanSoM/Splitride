package com.example.consumocarros;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SaveManager {

    private static final String FILE_NAME = "usuarios.json";
    private final Context context;
    private final Gson gson;

    public SaveManager(Context context) {
        this.context = context;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        // Inicializa usuarios por defecto si el archivo no existe
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            inicializarUsuariosPorDefecto();
        }
    }

    // Guardar lista completa de usuarios
    public void guardarUsuarios(List<Usuario> usuarios) {
        String json = gson.toJson(usuarios);

        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {

            writer.write(json);
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Cargar lista completa de usuarios
    public List<Usuario> cargarUsuarios() {
        try (FileInputStream fis = context.openFileInput(FILE_NAME);
             InputStreamReader reader = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(reader)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            Usuario[] arrayUsuarios = gson.fromJson(sb.toString(), Usuario[].class);
            if (arrayUsuarios != null) {
                return new ArrayList<>(Arrays.asList(arrayUsuarios));
            }
            return new ArrayList<>();

        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Actualiza un usuario en el JSON usando idUsuario como criterio.
     * Si no se encuentra, lo agrega al final.
     */
    public void actualizarUsuario(Usuario usuarioActualizado) {
        List<Usuario> usuarios = cargarUsuarios();
        boolean encontrado = false;

        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            if (u.getIdUsuario().equals(usuarioActualizado.getIdUsuario())) {
                usuarios.set(i, usuarioActualizado);
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            usuarios.add(usuarioActualizado);
        }

        guardarUsuarios(usuarios);
    }

    public Usuario obtenerUsuario(String nombreUsuario, String contrasena) {
        List<Usuario> usuarios = cargarUsuarios();

        for (Usuario u : usuarios) {
            if (u.getUsuario().equals(nombreUsuario) && u.getContrasena().equals(contrasena)) {
                return u;
            }
        }

        return null;
    }

    public void inicializarUsuariosPorDefecto() {
        List<Usuario> usuarios = cargarUsuarios();

        if (usuarios.isEmpty()) {
            Usuario daniel = new Usuario("12345", "daniel123", "Daniel", "ApellidoDaniel");
            Usuario marta = new Usuario("12345", "marta123", "Marta", "ApellidoMarta");
            Usuario juan = new Usuario("12345", "juan123", "Juan", "ApellidoJuan");

            usuarios.add(daniel);
            usuarios.add(marta);
            usuarios.add(juan);

            guardarUsuarios(usuarios);
        }
    }
}
