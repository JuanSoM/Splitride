package com.example.consumocarros;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialActivity extends AppCompatActivity {

    private Usuario usuario;
    private ListView listaViajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        usuario = (Usuario) getIntent().getSerializableExtra("usuario");
        listaViajes = findViewById(R.id.listaViajes);

        // Configurar botones de navegación
        setupNavigationButtons();

        if (usuario != null) {
            // Invertimos la lista para mostrar los más recientes primero
            List<Usuario.Viaje> viajes = usuario.getViajes();
            Collections.reverse(viajes);
            
            ViajeAdapter adapter = new ViajeAdapter(this, viajes);
            listaViajes.setAdapter(adapter);
        }
    }

    private void setupNavigationButtons() {
        ImageButton homeButton = findViewById(R.id.homeButton);
        ImageButton logoButton = findViewById(R.id.logoButton);
        ImageButton gasofaButton = findViewById(R.id.gasofaButton);
        ImageButton historialButton = findViewById(R.id.historialButton);

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HistorialActivity.this, MainActivity.class);
            intent.putExtra("usuario", usuario);
            startActivity(intent);
        });

        gasofaButton.setOnClickListener(v -> {
            Intent intent = new Intent(HistorialActivity.this, DepositoActivity.class);
            intent.putExtra("usuario", usuario);
            startActivity(intent);
        });
        
        logoButton.setOnClickListener(v -> {
            Intent intent = new Intent(HistorialActivity.this, MisCochesActivity.class);
            intent.putExtra("usuario", usuario);
            startActivity(intent);
        });
        
        historialButton.setOnClickListener(v -> { 
            // Ya estamos aquí, no hace nada
        });
    }
}

class ViajeAdapter extends ArrayAdapter<Usuario.Viaje> {

    public ViajeAdapter(Context context, List<Usuario.Viaje> viajes) {
        super(context, 0, viajes);
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
            // Formatear origen y destino
            origenDestino.setText(String.format("%s → %s", currentViaje.getOrigen(), currentViaje.getDestino()));

            // Formatear detalles
            String detailsText = String.format(Locale.US, "%.1f km  •  %.2f L", 
                currentViaje.getDistanciaKm(), 
                currentViaje.getLitrosConsumidos());
            details.setText(detailsText);

            // Formatear fecha y coche
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fecha = sdf.format(new Date(currentViaje.getTimestamp()));
            cocheFecha.setText(String.format("%s - %s", currentViaje.getCocheUsado(), fecha));
        }

        return listItemView;
    }
}
