package com.ucsalpdm.gps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ucsalpdm.gps.databinding.ActivityMapsBinding;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private SharedPreferences sharedPreferences;
    private List<LatLng> latLngList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("configuracoes", Context.MODE_PRIVATE);

        // Recuperar os dados de localização do SharedPreferences
        latLngList = getLatLngListFromSharedPreferences();

        // Obter o SupportMapFragment e ser notificado quando o mapa estiver pronto para ser usado
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Verificar se há coordenadas salvas
        if (latLngList != null && !latLngList.isEmpty()) {
            drawPolyline(latLngList);
        } else {
            Toast.makeText(this, "Nenhuma trilha salva encontrada.", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawPolyline(List<LatLng> latLngList) {
        // Adicionar uma linha poligonal no mapa
        PolylineOptions polylineOptions = new PolylineOptions().addAll(latLngList);
        Polyline polyline = mMap.addPolyline(polylineOptions);

        // Ajustar a câmera para mostrar a trilha inteira
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngList) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        int padding = 100; // Padding adicional (em pixels) ao redor da borda da trilha
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    private List<LatLng> getLatLngListFromSharedPreferences() {
        List<LatLng> latLngList = new ArrayList<>();
        int size = sharedPreferences.getInt("latLngList_size", 0);

        for (int i = 0; i < size; i++) {
            double lat = Double.longBitsToDouble(sharedPreferences.getLong("lat_" + i, 0));
            double lng = Double.longBitsToDouble(sharedPreferences.getLong("lng_" + i, 0));
            latLngList.add(new LatLng(lat, lng));
        }

        return latLngList;
    }

    public static void saveLatLngListToSharedPreferences(Context context, List<LatLng> latLngList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("configuracoes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("latLngList_size", latLngList.size());

        for (int i = 0; i < latLngList.size(); i++) {
            editor.putLong("lat_" + i, Double.doubleToLongBits(latLngList.get(i).latitude));
            editor.putLong("lng_" + i, Double.doubleToLongBits(latLngList.get(i).longitude));
        }

        editor.apply();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MapsActivity.class);
        context.startActivity(intent);
    }
}
