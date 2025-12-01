package com.example.consumocarros;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.consumocarros.databinding.ActivitySocialBinding;

import java.util.ArrayList;
import java.util.List;

public class SocialActivity extends AppCompatActivity {

    private ActivitySocialBinding binding;
    private SaveManager saveManager;

    // Simulamos el usuario logueado actual.
    // En una app real, obtendrías esto de SharedPreferences o del Intent.
    private Usuario usuarioconectado;
    private String currentUserId = "12345"; // ID de ejemplo (Daniel)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySocialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Inicializar SaveManager
        saveManager = new SaveManager(this);

        // 2. Cargar al usuario actual desde el JSON
        usuarioconectado = (Usuario) getIntent().getSerializableExtra("usuario");

        // Configurar botón de añadir amigo (Asegúrate de que tu ID en activity_social sea este)
        binding.buttonanadiramigo.setOnClickListener(v -> mostrarDialogoBusqueda());

        // Aquí podrías cargar la lista de amigos actual en un RecyclerView...
    }

    private void cargarUsuarioActual() {
        List<Usuario> usuarios = saveManager.cargarUsuarios();
        for (Usuario u : usuarios) {
            if (u.getIdUsuario().equals(currentUserId)) {
                usuarioconectado = u;
                break;
            }
        }

        if (usuarioconectado== null) {
            Toast.makeText(this, "Error: Usuario actual no encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoBusqueda() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_search_user, null);
        builder.setView(dialogView);

        EditText etSearch = dialogView.findViewById(R.id.etSearchUser);
        ListView listView = dialogView.findViewById(R.id.lvSearchResults);
        Button btnCerrar = dialogView.findViewById(R.id.btnCloseDialog);

        AlertDialog dialog = builder.create();

        // 1. Obtener TODOS los usuarios del JSON
        List<Usuario> todosLosUsuarios = saveManager.cargarUsuarios();

        // 2. FILTRAR LA LISTA: Quitamos al usuario que está haciendo la búsqueda
        List<Usuario> usuariosSugeridos = new ArrayList<>();

        if (usuarioconectado != null) {
            for (Usuario u : todosLosUsuarios) {
                // Comparamos IDs para no agregarnos a nosotros mismos
                if (!u.getIdUsuario().equals(usuarioconectado.getIdUsuario())) {
                    usuariosSugeridos.add(u);
                }
            }
        } else {
            // Fallback por seguridad si usuarioconectado es null
            usuariosSugeridos.addAll(todosLosUsuarios);
        }

        // 3. Configurar el Adaptador
        // IMPORTANTE: Para que solo salga el nombre, asegúrate de que en tu clase Usuario.java

        // Si no quieres tocar Usuario.java, el adaptador estándar usará toString().
        ArrayAdapter<Usuario> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usuariosSugeridos);
        listView.setAdapter(adapter);

        // 4. Lógica de filtrado en tiempo real (Buscador)
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 5. Click en un resultado
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Usuario usuarioDestino = adapter.getItem(position);

            // Confirmar envío de solicitud
            new AlertDialog.Builder(SocialActivity.this)
                    .setTitle("Enviar solicitud")
                    .setMessage("¿Quieres enviar una solicitud de amistad a " + usuarioDestino.getUsuario() + "?")
                    .setPositiveButton("Enviar", (dialogInterface, i) -> {
                        simularEnvioYAceptacion(usuarioDestino);
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    /**
     * Como no hay backend real, simulamos que enviamos la solicitud y
     * que el usuario la acepta, actualizando el JSON localmente.
     */
    private void simularEnvioYAceptacion(Usuario nuevoAmigo) {
        if (usuarioconectado == null) return;

        // Verificar si ya son amigos
        if (usuarioconectado.getListaAmigosIds().contains(nuevoAmigo.getIdUsuario())) {
            Toast.makeText(this, "Ya sois amigos", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Agregar ID del amigo al usuario actual
        usuarioconectado.agregarAmigo(nuevoAmigo.getIdUsuario());

        // 2. (Opcional) Agregar ID del usuario actual al amigo (Amistad bidireccional)
        nuevoAmigo.agregarAmigo(usuarioconectado.getIdUsuario());

        // 3. Guardar cambios en el JSON usando SaveManager
        // SaveManager.actualizarUsuario busca por ID y reemplaza
        saveManager.actualizarUsuario(usuarioconectado);
        saveManager.actualizarUsuario(nuevoAmigo);

        Toast.makeText(this, "¡Ahora eres amigo de " + nuevoAmigo.getUsuario() + "!", Toast.LENGTH_LONG).show();

        // Aquí podrías actualizar tu RecyclerView principal para mostrar el nuevo amigo
    }
}
