package com.example.weathvision.UserNew;


import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;


import com.example.weathvision.R;

import java.util.Locale;

public class Ajustes extends Fragment {
    private Spinner spinner, spinnerTemporizador;
    private Switch switcher, switchSystemDarkMode;
    private boolean nightMODE;
    private SharedPreferences sharedPreferences;
    private boolean isUserInteraction = false; // Bandera para detectar interacción del usuario
    private Button aceptarTemporizador;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ajustes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        spinner = view.findViewById(R.id.spinner);
        spinnerTemporizador = view.findViewById(R.id.spinnerTemporizador);
        switcher = view.findViewById(R.id.switcher);
        switchSystemDarkMode = view.findViewById(R.id.switchSystemDarkMode);

        aceptarTemporizador = view.findViewById(R.id.aceptarTemporizador);

        traducir();

        modoOscuroSistema();
        modoOscuro();

        if (switchSystemDarkMode.isChecked()) {
            switcher.setEnabled(false);
        } else {
            switcher.setEnabled(true);
        }


        System.out.println("nightMODE al principio: " + nightMODE);

    }

    private void traducir() {
        // Configurar el adaptador del Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.idiomas,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Cargar la preferencia guardada o usar el idioma del dispositivo como predeterminado
        SharedPreferences prefs = requireActivity().getPreferences(MODE_PRIVATE);
        String savedLanguage = prefs.getString("language", Locale.getDefault().getLanguage());

        // Establecer la selección inicial basada en la preferencia guardada (sin disparar el listener)
        if (savedLanguage.equals("en")) {
            spinner.setSelection(1, false); // Inglés, sin animación ni disparo del listener
        } else {
            spinner.setSelection(0, false); // Español, sin animación ni disparo del listener
        }

        // Listener para cambios en la selección del Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Solo procesar si es una interacción del usuario
                if (!isUserInteraction) {
                    return; // Ignorar si no es una selección manual
                }

                String selectedItem = parent.getItemAtPosition(position).toString();
                String languageCode = "";

                // Determinar el código de idioma según la selección
                if (selectedItem.equals("Inglés") || selectedItem.equals("English")) {
                    languageCode = "en";
                } else if (selectedItem.equals("Español") || selectedItem.equals("Espanish")) {
                    languageCode = "es";
                }

                // Aplicar el cambio de idioma solo si es diferente al actual
                if (!Locale.getDefault().getLanguage().equals(languageCode)) {
                    // Guardar la preferencia de idioma
                    SharedPreferences prefs = requireActivity().getPreferences(MODE_PRIVATE);
                    prefs.edit().putString("language", languageCode).apply();

                    // Cambiar el idioma y recrear la actividad
                    setLanguage(languageCode);
                    requireActivity().recreate();
                }

                // Resetear la bandera después de procesar la selección
                isUserInteraction = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        // Detectar interacción del usuario con el Spinner
        spinner.setOnTouchListener((v, event) -> {
            isUserInteraction = true; // Marcar como interacción del usuario
            return false; // Permitir que el evento siga su curso
        });
    }



    private void modoOscuroSistema() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MODE", MODE_PRIVATE);
        boolean sistema = sharedPreferences.getBoolean("sistema", true);

        switchSystemDarkMode.setChecked(sistema);
        switchSystemDarkMode.setOnClickListener(v -> {
            // Obtener el estado actual del Switch
            boolean isSistemadarkMode = switchSystemDarkMode.isChecked();

            // Aplicar el modo oscuro/claro según el estado del Switch
            if (isSistemadarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                switcher.setEnabled(false);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                switcher.setEnabled(true);
            }

            // Guardar el nuevo estado en SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("sistema", isSistemadarkMode).apply();

            // Opcional: Recrear la actividad para aplicar el tema inmediatamente
            getActivity().recreate();
        });
    }

    private void modoOscuro() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MODE", MODE_PRIVATE);
        nightMODE = sharedPreferences.getBoolean("night", false);

        switcher.setChecked(nightMODE);
        switcher.setOnClickListener(v -> {
            // Obtener el estado actual del Switch
            boolean isNightMode = switcher.isChecked();

            // Aplicar el modo oscuro/claro según el estado del Switch
            if (isNightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            // Guardar el nuevo estado en SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("night", isNightMode).apply();

            // Actualizar la variable local
            nightMODE = isNightMode;

            // Opcional: Recrear la actividad para aplicar el tema inmediatamente
            getActivity().recreate();
        });
    }

    private void setLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        requireContext().getResources().updateConfiguration(config,
                requireContext().getResources().getDisplayMetrics());
    }
}