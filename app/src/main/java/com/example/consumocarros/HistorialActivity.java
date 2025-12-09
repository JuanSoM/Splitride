package com.example.consumocarros;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color; // ¡Nueva importación necesaria!
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistorialActivity extends AppCompatActivity {

    private Usuario usuario;
    private ListView listaViajes;
    private TextView textoVacio;
    private Button botonAbrirFiltro, botonLimpiarFiltros;

    private List<Usuario.Viaje> historialCompleto;
    private List<Usuario.Viaje> historialFiltrado;
    private ViajeAdapter adapter;

    private String cocheFiltroActual = null;
    private Date fechaFiltroActual = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        usuario = (Usuario) getIntent().getSerializableExtra("usuario");
        listaViajes = findViewById(R.id.listaViajes);
        textoVacio = findViewById(R.id.textoVacio);
        botonAbrirFiltro = findViewById(R.id.boton_abrir_filtro);
        botonLimpiarFiltros = findViewById(R.id.boton_limpiar_filtros);

        historialCompleto = (usuario != null && usuario.getViajes() != null) ? new ArrayList<>(usuario.getViajes()) : new ArrayList<>();
        Collections.reverse(historialCompleto);

        historialFiltrado = new ArrayList<>(historialCompleto);
        adapter = new ViajeAdapter(this, historialFiltrado);
        listaViajes.setAdapter(adapter);

        setupButtons();
        configurarListenersFiltro();
        actualizarUI();
    }

    private void configurarListenersFiltro() {
        botonAbrirFiltro.setOnClickListener(v -> mostrarDialogoFiltro());
        botonLimpiarFiltros.setOnClickListener(v -> limpiarFiltros());
    }

    private void mostrarDialogoFiltro() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filtro_viajes, null);

        Spinner spinnerCoche = dialogView.findViewById(R.id.spinner_dialog_coche);
        Button botonFecha = dialogView.findViewById(R.id.boton_dialog_fecha);

        final String[] tempCocheFiltro = {cocheFiltroActual};
        final Date[] tempFechaFiltro = {fechaFiltroActual};

        // --- Configuración del Spinner (Coche) ---
        Set<String> nombresCoches = new HashSet<>();
        for (Usuario.Viaje viaje : historialCompleto) {
            nombresCoches.add(viaje.getCocheUsado());
        }
        List<String> coches = new ArrayList<>();
        coches.add("Todos los Coches");
        coches.addAll(nombresCoches);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                coches);

        spinnerCoche.setAdapter(spinnerAdapter);

        if (cocheFiltroActual != null) {
            int pos = coches.indexOf(cocheFiltroActual);
            if (pos >= 0) spinnerCoche.setSelection(pos);
        }

        spinnerCoche.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String seleccionado = parent.getItemAtPosition(position).toString();
                tempCocheFiltro[0] = seleccionado.equals("Todos los Coches") ? null : seleccionado;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // --- Configuración del Botón de Fecha ---
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (fechaFiltroActual != null) {
            botonFecha.setText(sdf.format(fechaFiltroActual));
        } else {
            botonFecha.setText("Elegir Fecha");
        }

        botonFecha.setOnClickListener(v -> mostrarDatePicker(botonFecha, tempFechaFiltro));

        // --- Creación del Diálogo ---
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filtrar Historial");
        builder.setView(dialogView);

        builder.setPositiveButton("Aplicar", (dialog, which) -> {
            cocheFiltroActual = tempCocheFiltro[0];
            fechaFiltroActual = tempFechaFiltro[0];
            aplicarFiltros();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        // Mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();

        // **Añadido para forzar texto negro en los botones del AlertDialog**
        try {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        } catch (Exception e) {
            // Manejo de errores si los botones no existen inmediatamente
            e.printStackTrace();
        }
    }

    /**
     * Muestra el DatePickerDialog con el formato de scroll (ruedas).
     */
    private void mostrarDatePicker(Button botonFecha, final Date[] tempFechaFiltro) {
        final Calendar c = Calendar.getInstance();
        if (tempFechaFiltro[0] != null) {
            c.setTime(tempFechaFiltro[0]);
        }

        // Usamos un tema estándar del SDK que activa el estilo de rueda
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                android.R.style.Theme_DeviceDefault_Light_Dialog, // Tema robusto
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    tempFechaFiltro[0] = cal.getTime();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    botonFecha.setText(sdf.format(tempFechaFiltro[0]));
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void aplicarFiltros() {
        historialFiltrado.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaFiltroStr = fechaFiltroActual != null ? sdf.format(fechaFiltroActual) : null;

        for (Usuario.Viaje viaje : historialCompleto) {
            boolean cumpleCoche = (cocheFiltroActual == null || viaje.getCocheUsado().equals(cocheFiltroActual));
            boolean cumpleFecha = true;

            if (fechaFiltroStr != null) {
                String fechaViajeStr = sdf.format(new Date(viaje.getTimestamp()));
                if (!fechaViajeStr.equals(fechaFiltroStr)) {
                    cumpleFecha = false;
                }
            }

            if (cumpleCoche && cumpleFecha) {
                historialFiltrado.add(viaje);
            }
        }

        actualizarUI();
    }

    private void limpiarFiltros() {
        cocheFiltroActual = null;
        fechaFiltroActual = null;

        aplicarFiltros();
        Toast.makeText(this, "Filtros limpiados", Toast.LENGTH_SHORT).show();
    }

    private void actualizarUI() {
        adapter.notifyDataSetChanged();

        if (historialCompleto.isEmpty()) {
            // Texto actualizado a la versión original, pero mantenemos el '⚠️' visualmente
            textoVacio.setText("⚠️ No existen registros aún.");
            textoVacio.setVisibility(View.VISIBLE);
            listaViajes.setVisibility(View.GONE);
            botonAbrirFiltro.setEnabled(false);
            botonLimpiarFiltros.setEnabled(false);
        } else if (historialFiltrado.isEmpty()) {
            textoVacio.setText("Historial filtrado: Sin resultados.");
            textoVacio.setVisibility(View.VISIBLE);
            listaViajes.setVisibility(View.GONE);
            botonAbrirFiltro.setEnabled(true);
            botonLimpiarFiltros.setEnabled(true);
        } else {
            textoVacio.setVisibility(View.GONE);
            listaViajes.setVisibility(View.VISIBLE);
            botonAbrirFiltro.setEnabled(true);
            botonLimpiarFiltros.setEnabled(true);
        }
    }

    private void setupButtons() {
        findViewById(R.id.botoncuenta).setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(HistorialActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.profile_view) {
                    Toast.makeText(HistorialActivity.this, "Ver perfil (Próximamente)", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.profile_logout) {
                    logoutUser();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        findViewById(R.id.homeButton).setOnClickListener(v -> navigateTo(MainActivity.class));
        findViewById(R.id.logoButton).setOnClickListener(v -> navigateTo(MisCochesActivity.class));
        findViewById(R.id.gasofaButton).setOnClickListener(v -> navigateTo(DepositoActivity.class));
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.putExtra("usuario", usuario);
        startActivity(intent);
    }

    private void logoutUser() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

class ViajeAdapter extends ArrayAdapter<Usuario.Viaje> {

    public ViajeAdapter(Context context, List<Usuario.Viaje> viajes) {
        super(context, 0, viajes);
    }

    // Función auxiliar para truncar el texto hasta la primera coma.
    private String truncarTexto(String texto) {
        if (texto == null) return "";
        int indiceComa = texto.indexOf(',');
        if (indiceComa > 0) {
            // Si hay coma, devuelve la subcadena antes de la coma.
            return texto.substring(0, indiceComa).trim();
        }
        // Si no hay coma, devuelve el texto original.
        return texto.trim();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_viaje, parent, false);
        }

        Usuario.Viaje currentViaje = getItem(position);

        TextView origenDestino = listItemView.findViewById(R.id.viaje_origen_destino);
        TextView details = listItemView.findViewById(R.id.viaje_details);
        TextView cocheFecha = listItemView.findViewById(R.id.viaje_coche_fecha);

        if (currentViaje != null) {
            // 1. Preparar textos (Completo y Truncado)
            final String origenCompleto = currentViaje.getOrigen();
            final String destinoCompleto = currentViaje.getDestino();
            final String textoCompleto = String.format("%s → %s", origenCompleto, destinoCompleto);

            String origenCorto = truncarTexto(origenCompleto);
            String destinoCorto = truncarTexto(destinoCompleto);
            final String textoCorto = String.format("%s → %s", origenCorto, destinoCorto);

            // 2. Mostrar la versión corta por defecto
            origenDestino.setText(textoCorto);

            // 3. Almacenar el estado de expansión en el Tag del TextView
            // Usamos el Tag para guardar el texto completo (String) y si está expandido (Boolean)
            origenDestino.setTag(R.id.tag_key_full_text, textoCompleto);
            origenDestino.setTag(R.id.tag_key_is_expanded, false);

            // Comprobación de reciclaje de vista: si se recicló, restaurar el estado
            if (convertView != null) {
                // Si la vista ya tenía un estado, lo restauramos
                Boolean isExpanded = (Boolean) origenDestino.getTag(R.id.tag_key_is_expanded);
                if (isExpanded != null && isExpanded) {
                    origenDestino.setText(textoCompleto);
                }
            }


            // 4. Configurar el Listener para cambiar entre corto y completo
            // Ponemos el Listener en el TextView, no en la fila completa
            origenDestino.setOnClickListener(v -> {
                TextView tv = (TextView) v;
                // Obtener el estado actual y el texto completo
                String fullText = (String) tv.getTag(R.id.tag_key_full_text);
                Boolean isExpanded = (Boolean) tv.getTag(R.id.tag_key_is_expanded);

                if (isExpanded != null && isExpanded) {
                    // Si está expandido -> Truncar
                    tv.setText(textoCorto);
                    tv.setTag(R.id.tag_key_is_expanded, false);
                } else {
                    // Si está truncado -> Expandir
                    tv.setText(fullText);
                    tv.setTag(R.id.tag_key_is_expanded, true);
                }
            });


            // --- Otros detalles del viaje (sin cambios) ---
            String detailsText = String.format(Locale.US, "%.1f km • %.2f L",
                    currentViaje.getDistanciaKm(),
                    currentViaje.getLitrosConsumidos());
            details.setText(detailsText);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fecha = sdf.format(new Date(currentViaje.getTimestamp()));
            cocheFecha.setText(String.format("%s - %s", currentViaje.getCocheUsado(), fecha));

            // Asegurar que el listener de la fila completa se anula si se usa el listener del TextView
            listItemView.setOnClickListener(null); // Desactivamos el listener de la fila si existía
        }
        return listItemView;
    }
}