package com.example.consumocarros;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SaveManager {

    // ⚠️ CONFIGURACIÓN IMPORTANTE: Cambia esta URL según tu entorno
    // Para emulador Android: http://10.0.2.2:8000/api/
    // Para dispositivo físico en red local: http://TU_IP:8000/api/
    private static final String BASE_URL = "http://10.0.2.2:8000/api/";
    private static final String TAG = "SaveManager";
    
    private final Context context;
    private final Gson gson;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public SaveManager(Context context) {
        this.context = context;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Guardar lista completa de usuarios - NO IMPLEMENTADO en versión API
     * En la versión API cada usuario se actualiza individualmente
     */
    public void guardarUsuarios(List<Usuario> usuarios) {
        Log.w(TAG, "guardarUsuarios() está deprecado. Usa actualizarUsuario() para cada usuario.");
    }

    /**
     * Cargar lista completa de usuarios desde la API
     */
    public List<Usuario> cargarUsuarios() {
        try {
            URL url = new URL(BASE_URL + "usuarios/all/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Usuario[] arrayUsuarios = gson.fromJson(response.toString(), Usuario[].class);
                if (arrayUsuarios != null) {
                    return new ArrayList<>(java.util.Arrays.asList(arrayUsuarios));
                }
            } else {
                Log.e(TAG, "Error al cargar usuarios: " + responseCode);
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Error en cargarUsuarios: " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * Actualiza un usuario en la API
     * Esta operación se ejecuta en segundo plano
     */
    public void actualizarUsuario(Usuario usuarioActualizado) {
        executor.execute(() -> {
            try {
                // Convertir usuario a JSON incluyendo todos sus datos anidados
                String jsonUsuario = gson.toJson(usuarioActualizado);
                
                URL url = new URL(BASE_URL + "usuarios/" + usuarioActualizado.getIdUsuario() + "/update/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                // Enviar datos
                OutputStream os = conn.getOutputStream();
                os.write(jsonUsuario.getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 200) {
                    Log.d(TAG, "Usuario actualizado correctamente: " + usuarioActualizado.getUsuario());
                    
                    // Actualizar coches
                    actualizarCoches(usuarioActualizado);
                    
                    // Actualizar viajes
                    actualizarViajes(usuarioActualizado);
                    
                } else {
                    Log.e(TAG, "Error al actualizar usuario: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error en actualizarUsuario: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Actualiza los coches del usuario
     */
    private void actualizarCoches(Usuario usuario) {
        try {
            if (usuario.getCoches() != null) {
                for (Usuario.Car coche : usuario.getCoches()) {
                    JsonObject carJson = new JsonObject();
                    carJson.addProperty("brand", coche.getBrand());
                    carJson.addProperty("model", coche.getModel());
                    carJson.addProperty("year", coche.getYear());
                    carJson.addProperty("cityKmpl", coche.getCityKmpl());
                    carJson.addProperty("highwayKmpl", coche.getHighwayKmpl());
                    carJson.addProperty("avgKmpl", coche.getAvgKmpl());
                    carJson.addProperty("capacidaddeposito", coche.getcapacidaddeposito());
                    carJson.addProperty("capacidadactual", coche.getCapacidadactual());
                    carJson.addProperty("vecesusado", coche.getvecesusado());
                    
                    // En una implementación real, deberías trackear si el coche es nuevo o existente
                    // Por simplicidad, aquí asumimos que todos son nuevos
                    enviarCoche(usuario.getIdUsuario(), carJson.toString());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar coches: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza los viajes del usuario
     */
    private void actualizarViajes(Usuario usuario) {
        try {
            if (usuario.getViajes() != null) {
                for (Usuario.Viaje viaje : usuario.getViajes()) {
                    JsonObject viajeJson = new JsonObject();
                    viajeJson.addProperty("origen", viaje.getOrigen());
                    viajeJson.addProperty("destino", viaje.getDestino());
                    viajeJson.addProperty("distanciaKm", viaje.getDistanciaKm());
                    viajeJson.addProperty("litrosConsumidos", viaje.getLitrosConsumidos());
                    viajeJson.addProperty("cocheUsado", viaje.getCocheUsado());
                    viajeJson.addProperty("timestamp", viaje.getTimestamp());
                    
                    enviarViaje(usuario.getIdUsuario(), viajeJson.toString());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar viajes: " + e.getMessage(), e);
        }
    }
    
    private void enviarCoche(String idUsuario, String carJson) {
        // Implementar si es necesario sincronizar coches individuales
    }
    
    private void enviarViaje(String idUsuario, String viajeJson) {
        // Implementar si es necesario sincronizar viajes individuales
    }

    /**
     * Interfaz para callback de login
     */
    public interface LoginCallback {
        void onSuccess(Usuario usuario);
        void onError(String mensaje);
    }

    /**
     * Interfaz para callback de búsqueda de usuarios
     */
    public interface SearchCallback {
        void onSuccess(List<Usuario> usuarios);
        void onError(String mensaje);
    }

    /**
     * Interfaz para callback de agregar amigo
     */
    public interface AddFriendCallback {
        void onSuccess(String mensaje);
        void onError(String mensaje);
    }

    /**
     * Obtener usuario por credenciales (Login) - Versión asíncrona
     */
    public void obtenerUsuario(String nombreUsuario, String contrasena, LoginCallback callback) {
        executor.execute(() -> {
            try {
                JsonObject loginJson = new JsonObject();
                loginJson.addProperty("usuario", nombreUsuario);
                loginJson.addProperty("contrasena", contrasena);

                URL url = new URL(BASE_URL + "usuarios/login/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                // Enviar credenciales
                OutputStream os = conn.getOutputStream();
                os.write(loginJson.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    Usuario usuario = gson.fromJson(response.toString(), Usuario.class);
                    conn.disconnect();
                    
                    // Ejecutar callback en el hilo principal
                    mainHandler.post(() -> callback.onSuccess(usuario));
                } else {
                    Log.e(TAG, "Error en login: " + responseCode);
                    mainHandler.post(() -> callback.onError("Error de autenticación"));
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error en obtenerUsuario: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("Error de conexión: " + e.getMessage()));
            }
        });
    }

    /**
     * Inicializar usuarios por defecto - NO NECESARIO en versión API
     * Los usuarios se crean desde el panel de administración de Django
     */
    public void inicializarUsuariosPorDefecto() {
        Log.i(TAG, "Usuarios por defecto deben crearse desde Django Admin");
    }
    
    /**
     * Método auxiliar para guardar un solo usuario (utilizado por LoginActivity)
     */
    public void guardarUsuario(Usuario usuario) {
        actualizarUsuario(usuario);
    }

    /**
     * Buscar usuarios por nombre de usuario (asíncrono)
     */
    public void buscarUsuarios(String query, String idUsuarioActual, SearchCallback callback) {
        executor.execute(() -> {
            try {
                String urlString = BASE_URL + "usuarios/buscar/?q=" + 
                    java.net.URLEncoder.encode(query, "UTF-8") + 
                    "&exclude=" + idUsuarioActual;
                
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // La respuesta tiene formato {"results": [...]}
                    JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                    JsonArray resultsArray = jsonResponse.getAsJsonArray("results");
                    
                    List<Usuario> usuarios = new ArrayList<>();
                    for (int i = 0; i < resultsArray.size(); i++) {
                        Usuario usuario = gson.fromJson(resultsArray.get(i), Usuario.class);
                        usuarios.add(usuario);
                    }
                    
                    conn.disconnect();
                    mainHandler.post(() -> callback.onSuccess(usuarios));
                } else {
                    Log.e(TAG, "Error en búsqueda: " + responseCode);
                    mainHandler.post(() -> callback.onError("Error al buscar usuarios"));
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error en buscarUsuarios: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("Error de conexión: " + e.getMessage()));
            }
        });
    }

    /**
     * Agregar amigo de forma bidireccional (asíncrono)
     */
    public void agregarAmigo(String idUsuario, String idAmigo, AddFriendCallback callback) {
        executor.execute(() -> {
            try {
                JsonObject requestJson = new JsonObject();
                requestJson.addProperty("amigo_id", idAmigo);

                URL url = new URL(BASE_URL + "usuarios/" + idUsuario + "/amigos/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                // Enviar datos
                OutputStream os = conn.getOutputStream();
                os.write(requestJson.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                    String mensaje = jsonResponse.has("mensaje") ? 
                        jsonResponse.get("mensaje").getAsString() : 
                        "Amigo agregado exitosamente";
                    
                    conn.disconnect();
                    mainHandler.post(() -> callback.onSuccess(mensaje));
                } else {
                    Log.e(TAG, "Error al agregar amigo: " + responseCode);
                    mainHandler.post(() -> callback.onError("Error al agregar amigo"));
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error en agregarAmigo: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("Error de conexión: " + e.getMessage()));
            }
        });
    }
}

