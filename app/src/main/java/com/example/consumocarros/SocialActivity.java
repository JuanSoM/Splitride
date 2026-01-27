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

    private void mostrarDialogoBusqueda() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_search_user, null);
        builder.setView(dialogView);

        EditText etSearch = dialogView.findViewById(R.id.etSearchUser);
        ListView listView = dialogView.findViewById(R.id.lvSearchResults);
        Button btnCerrar = dialogView.findViewById(R.id.btnCloseDialog);

        AlertDialog dialog = builder.create();

        // Lista que se actualizará con los resultados de búsqueda
        List<Usuario> usuariosEncontrados = new ArrayList<>();
        ArrayAdapter<Usuario> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usuariosEncontrados);
        listView.setAdapter(adapter);

        // Lógica de búsqueda en tiempo real
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                
                if (query.length() >= 2) {
                    // Buscar usuarios en la API
                    saveManager.buscarUsuarios(query, usuarioconectado.getIdUsuario(), new SaveManager.SearchCallback() {
                        @Override
                        public void onSuccess(List<Usuario> usuarios) {
                            usuariosEncontrados.clear();
                            usuariosEncontrados.addAll(usuarios);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(String mensaje) {
                            Toast.makeText(SocialActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    usuariosEncontrados.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Click en un resultado
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Usuario usuarioDestino = usuariosEncontrados.get(position);

            // Confirmar envío de solicitud con colores personalizados
            AlertDialog alertDialog = new AlertDialog.Builder(SocialActivity.this)
                    .setTitle("Enviar solicitud")
                    .setMessage("¿Quieres agregar como amigo a " + usuarioDestino.getUsuario() + "?")
                    .setPositiveButton("Agregar", (dialogInterface, i) -> {
                        agregarAmigo(usuarioDestino);
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancelar", null)
                    .create();
            
            alertDialog.show();
            
            // Cambiar color de los botones después de mostrar el diálogo
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.black));
        });

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    /**
     * Agregar amigo usando la API (bidireccional)
     */
    private void agregarAmigo(Usuario nuevoAmigo) {
        if (usuarioconectado == null) return;

        // Verificar si ya son amigos
        if (usuarioconectado.getListaAmigosIds().contains(nuevoAmigo.getIdUsuario())) {
            Toast.makeText(this, "Ya sois amigos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Llamar a la API para agregar amigo
        saveManager.agregarAmigo(usuarioconectado.getIdUsuario(), nuevoAmigo.getIdUsuario(), 
            new SaveManager.AddFriendCallback() {
                @Override
                public void onSuccess(String mensaje) {
                    // Actualizar la lista local del usuario actual
                    usuarioconectado.agregarAmigo(nuevoAmigo.getIdUsuario());
                    Toast.makeText(SocialActivity.this, mensaje, Toast.LENGTH_LONG).show();
                    
                    // Aquí podrías actualizar tu RecyclerView principal para mostrar el nuevo amigo
                }

                @Override
                public void onError(String mensaje) {
                    Toast.makeText(SocialActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
}
