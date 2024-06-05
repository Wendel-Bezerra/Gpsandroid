package com.ucsalpdm.gps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class ConfiguracoesActivity extends PreferenceActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carregar as preferências da tela de configurações
        addPreferencesFromResource(R.xml.activity_configuracoes);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("configuracoes", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Configurar a preferência para Unidade de Velocidade
        Preference velocidadePreference = findPreference("velocidade_preference");
        velocidadePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Salvar a unidade de velocidade escolhida
                editor.putString("velocidade", (String) newValue);
                editor.commit(); // Salva as mudanças imediatamente
                return true;
            }
        });

        // Configurar a preferência para Formato das Coordenadas
        Preference coordenadasPreference = findPreference("coordenadas_preference");
        coordenadasPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Salvar o formato das coordenadas escolhido
                editor.putString("coordenadas", (String) newValue);
                editor.commit(); // Salva as mudanças imediatamente
                return true;
            }
        });

        // Configurar a preferência para Orientação do Mapa
        Preference orientacaoPreference = findPreference("orientacao_preference");
        orientacaoPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Salvar a orientação do mapa escolhida
                editor.putString("orientacao", (String) newValue);
                editor.commit(); // Salva as mudanças imediatamente
                return true;
            }
        });

        // Configurar a preferência para Tipo do Mapa
        Preference tipoMapaPreference = findPreference("tipo_mapa_preference");
        tipoMapaPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Salvar o tipo do mapa escolhido
                editor.putString("tipo_mapa", (String) newValue);
                editor.commit(); // Salva as mudanças imediatamente
                return true;
            }
        });

        // Configurar o botão "Salvar"
        Preference saveButton = findPreference("save_button");
        saveButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Exibir uma mensagem indicando que as configurações foram salvas
                Toast.makeText(ConfiguracoesActivity.this, "Configurações salvas!", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        // Configurar o botão "Limpar dados"
        Preference clearDataButton = findPreference("clear_data");
        clearDataButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Coloque a lógica para limpar dados aqui
                Toast.makeText(ConfiguracoesActivity.this, "Dados limpos!", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

}
