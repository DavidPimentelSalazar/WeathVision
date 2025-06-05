package com.example.weathvision.UserNew;


import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;


import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.LoginActivity;
import com.example.weathvision.R;
import com.example.weathvision.contrasena.MainContrasena;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Ajustes extends Fragment {

    private Button eliminarCuenta, cambiarContrasena;
    private TextInputEditText confirmacion;

    private TextView nombreUsuario, correoUsuario;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ajustes, container, false);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String nombreUsuarioText = sharedPreferences.getString("nombre_usuario", "ValorPorDefecto");



        eliminarCuenta = view.findViewById(R.id.eliminarCuenta);
        eliminarCuenta.setOnClickListener(v -> mostrarAlerta());


        cambiarContrasena = view.findViewById(R.id.cambiarContrasena);
        cambiarContrasena.setOnClickListener(v -> cambiarContrasenaUsuario());

        nombreUsuario = view.findViewById(R.id.nombreUsuario);
        nombreUsuario.setText(nombreUsuarioText);

        correoUsuario = view.findViewById(R.id.correoUsuario);
        cargarCorreoUsuario();
        return view;

    }

    private void cargarCorreoUsuario() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("id_usuario", -1);

        if (idUsuario != -1) {
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<List<String>> call = apiService.obtenerCorreoUsuario(idUsuario);

            call.enqueue(new Callback<List<String>>() {
                @Override
                public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        correoUsuario.setText(response.body().get(0)); // Asigna el correo obtenido al TextView
                    } else {
                        correoUsuario.setText("Correo no encontrado");
                    }
                }

                @Override
                public void onFailure(Call<List<String>> call, Throwable t) {
                    correoUsuario.setText("Error al cargar correo");
                }
            });
        } else {
            correoUsuario.setText("ID de usuario no válido");
        }
    }

    private void mostrarAlerta() {
// Inflar la vista del diálogo
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.alert_view_contrasena, null);

// Buscar el EditText dentro del dialogView
        EditText confirmacion = dialogView.findViewById(R.id.confirmacion);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setView(dialogView)
                .setPositiveButton("Cerrar", (d, which) -> d.dismiss()) // "Cerrar" siempre cierra la alerta
                .setNegativeButton("Eliminar", null); // Se sobrescribirá después de que el diálogo se cree

// Crear y mostrar el diálogo
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_alert);
        }
        dialog.show();

// Sobrescribir el botón "Eliminar" para que haga la validación manualmente
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            String textoIngresado = confirmacion.getText().toString().trim();
            if (textoIngresado.equals("ELIMINAR")) {
                eliminarCuentaUsuario(); // Ejecutar la acción
                dialog.dismiss(); // Cerrar el diálogo solo si la acción fue exitosa
            } else {
                confirmacion.setError("Introduce 'ELIMINAR' para confirmar"); // Mostrar error sin cerrar el diálogo
            }
        });
    }

    private void cambiarContrasenaUsuario() {
        Intent intent = new Intent(getContext(), MainContrasena.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

    private void eliminarCuentaUsuario() {

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("id_usuario", -1);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.deleteUsuario(idUsuario);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }


}