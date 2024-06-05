package com.ucsalpdm.gps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ucsalpdm.gps.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    private LocationManager locationManager;
    private boolean isTracking = false;

    public static final int PERMISSION_REQUEST_LOCATION = 1001;
    private List<LatLng> trilhaPoints = new ArrayList<>();
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Polyline polyline;
    private Marker currentMarker;

    // Adiciona a instância do LocationDatabaseHelper
    private LocationDatabaseHelper dbHelper;
    private LatLng startLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa a instância do LocationDatabaseHelper
        dbHelper = new LocationDatabaseHelper(this);

        // Obtém uma referência ao SupportMapFragment
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);

        // Inicia o processo de inicialização do mapa
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Button btnRegistrarTrilha = findViewById(R.id.btnRegistrarTrilha);
        Button btnGerenciarTrilha = findViewById(R.id.btnGerenciarTrilha);
        Button btnCompartilharTrilha = findViewById(R.id.btnCompartilharTrilha);
        Button btnConfiguracao = findViewById(R.id.btnConfiguracao);
        Button btnSobre = findViewById(R.id.btnSobre);

        btnRegistrarTrilha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTracking) {
                    startTracking();
                } else {
                    stopTracking();
                }
            }
        });

        btnGerenciarTrilha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Recupera as trilhas salvas do banco de dados
                List<LocationDatabaseHelper.Trilha> trilhas = dbHelper.getAllTrilhas();

                // Cria uma string para exibir as trilhas salvas
                StringBuilder trilhasSalvas = new StringBuilder();
                for (int i = 0; i < trilhas.size(); i++) {
                    LocationDatabaseHelper.Trilha trilha = trilhas.get(i);
                    trilhasSalvas.append("Trilha ").append(i + 1).append(": Início (Lat: ")
                            .append(trilha.getStart().latitude).append(", Lng: ").append(trilha.getStart().longitude)
                            .append("), Fim (Lat: ").append(trilha.getEnd().latitude).append(", Lng: ")
                            .append(trilha.getEnd().longitude).append(")\n");
                }

                // Crie um AlertDialog para exibir as trilhas salvas
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Trilhas Salvas")
                        .setMessage(trilhasSalvas.toString())
                        .setPositiveButton("Fechar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss(); // Fecha o AlertDialog
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        btnCompartilharTrilha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verifica se há trilhas para compartilhar
                if (trilhaPoints.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Não há trilhas para compartilhar", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Crie uma string para representar as trilhas
                StringBuilder trilhasString = new StringBuilder();
                for (int i = 0; i < trilhaPoints.size(); i++) {
                    LatLng trilhaPoint = trilhaPoints.get(i);
                    trilhasString.append("Trilha ").append(i + 1).append(": Latitude ").append(trilhaPoint.latitude)
                            .append(", Longitude ").append(trilhaPoint.longitude).append("\n");
                }

                // Crie um intent para compartilhar a trilha
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Trilhas Salvas");
                intent.putExtra(Intent.EXTRA_TEXT, trilhasString.toString());

                // Inicia a atividade de compartilhamento
                startActivity(Intent.createChooser(intent, "Compartilhar Trilhas"));
            }
        });

        btnConfiguracao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar para a tela de configurações
                Intent intent = new Intent(MainActivity.this, ConfiguracoesActivity.class);
                startActivity(intent);
            }
        });

        btnSobre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar para a tela de sobre
                Intent intent = new Intent(MainActivity.this, CreditosActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startTracking() {
        trilhaPoints.clear();
        isTracking = true;
        startLatLng = null;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 15, locationListener);
        Toast.makeText(MainActivity.this, "Iniciando o rastreamento da trilha", Toast.LENGTH_SHORT).show();
    }

    private void stopTracking() {
        isTracking = false;
        locationManager.removeUpdates(locationListener);

        if (!trilhaPoints.isEmpty() && startLatLng != null) {
            LatLng endLatLng = trilhaPoints.get(trilhaPoints.size() - 1);
            // Salva a trilha (início e fim) no banco de dados
            dbHelper.addTrilha(startLatLng, endLatLng);
            Toast.makeText(MainActivity.this, "Trilha salva com sucesso", Toast.LENGTH_SHORT).show();
        }

        trilhaPoints.clear();
        polyline.remove();
        currentMarker.remove();
        Toast.makeText(MainActivity.this, "Rastreamento da trilha parado", Toast.LENGTH_SHORT).show();
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            if (isTracking) {
                if (startLatLng == null) {
                    startLatLng = latLng; // Define o ponto inicial da trilha
                }

                trilhaPoints.add(latLng);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 25));

                // Atualiza o polígono no mapa
                if (polyline != null) {
                    polyline.remove();
                }
                PolylineOptions polylineOptions = new PolylineOptions().addAll(trilhaPoints).color(Color.BLUE).width(5);
                polyline = mMap.addPolyline(polylineOptions);

                // Atualiza o marcador atual
                if (currentMarker != null) {
                    currentMarker.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Posição Atual");
                currentMarker = mMap.addMarker(markerOptions);

                // Move a câmera para a nova localização
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão de localização concedida, iniciar o rastreamento da localização
                startTracking();
            } else {
                // Permissão de localização negada, exibir uma mensagem ou tomar outra ação apropriada
                Toast.makeText(MainActivity.this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Recuperar as preferências salvas
        SharedPreferences sharedPreferences = getSharedPreferences("configuracoes", MODE_PRIVATE);
        String tipoMapa = sharedPreferences.getString("tipo_mapa", "Vetorial");
        String orientacaoMapa = sharedPreferences.getString("orientacao", "Course Up");

        // Define o tipo de mapa de acordo com a preferência do usuário
        if (tipoMapa.equals("Vetorial")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (tipoMapa.equals("Satélite")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }

        // Define a orientação do mapa de acordo com a preferência do usuário
        if (orientacaoMapa.equals("North Up")) {
            mMap.getUiSettings().setMapToolbarEnabled(true); // Habilita a barra de ferramentas do mapa
            mMap.getUiSettings().setMyLocationButtonEnabled(true); // Habilita o botão de localização
            mMap.getUiSettings().setCompassEnabled(true); // Habilita a bússola
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(mMap.getCameraPosition().target)
                    .zoom(mMap.getCameraPosition().zoom)
                    .bearing(0)  // Norte do mapa alinhado com o topo do dispositivo
                    .tilt(0)
                    .build()));
        } else if (orientacaoMapa.equals("Course Up")) {
            mMap.getUiSettings().setMapToolbarEnabled(true); // Habilita a barra de ferramentas do mapa
            mMap.getUiSettings().setMyLocationButtonEnabled(true); // Habilita o botão de localização
            mMap.getUiSettings().setCompassEnabled(true); // Habilita a bússola
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(mMap.getCameraPosition().target)
                    .zoom(mMap.getCameraPosition().zoom)
                    .bearing(90)  // Topo do mapa alinhado com a direção do deslocamento
                    .tilt(0)
                    .build()));
        }

        // Configurar botões de zoom
        FloatingActionButton zoomInButton = findViewById(R.id.zoom_in_button);
        FloatingActionButton zoomOutButton = findViewById(R.id.zoom_out_button);

        // Definir listener para o botão de zoom in
        zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        // Definir listener para o botão de zoom out
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Atualizar as configurações do mapa quando houver alterações nas preferências
        if (key.equals("tipo_mapa") || key.equals("orientacao")) {
            // Recuperar as novas preferências
            String tipoMapa = sharedPreferences.getString("tipo_mapa", "Vetorial");
            String orientacaoMapa = sharedPreferences.getString("orientacao", "Course Up");

            // Atualizar o mapa de acordo com as novas preferências
            if (key.equals("tipo_mapa")) {
                if (tipoMapa.equals("Vetorial")) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (tipoMapa.equals("Satélite")) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
            } else if (key.equals("orientacao")) {
                if (orientacaoMapa.equals("North Up")) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(mMap.getCameraPosition().target)
                            .zoom(mMap.getCameraPosition().zoom)
                            .bearing(0)  // Norte do mapa alinhado com o topo do dispositivo
                            .tilt(0)
                            .build()));
                } else if (orientacaoMapa.equals("Course Up")) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(mMap.getCameraPosition().target)
                            .zoom(mMap.getCameraPosition().zoom)
                            .bearing(90)  // Topo do mapa alinhado com a direção do deslocamento
                            .tilt(0)
                            .build()));
                }
            }
        }
    }

}





