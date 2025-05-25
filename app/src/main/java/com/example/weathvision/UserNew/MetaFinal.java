package com.example.weathvision.UserNew;




import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.Metas;
import com.example.weathvision.Api.Class.UsuarioResponse;
import com.example.weathvision.LoginActivity;
import com.example.weathvision.MainActivity;
import com.example.weathvision.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MetaFinal extends Fragment {
    private Button siguiente;
    private EditText metaFinal, fechaFinal;
    private SharedPreferences sharedPreferences;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.meta_final, container, false);
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE); // Initialize here
        siguiente = view.findViewById(R.id.siguiente);
        metaFinal = view.findViewById(R.id.metaFinal);
        fechaFinal = view.findViewById(R.id.fechaFinal);
        siguiente.setOnClickListener( v -> cambiarFragment(getContext()));
        return  view;
    }
    private void cambiarFragment(Context context) {
        sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("id_usuario", -1);
        if (idUsuario == -1) {
            Toast.makeText(context, "Error: No se encontró el ID del usuario", Toast.LENGTH_SHORT).show();

        }

        Metas metaEjemplo = (Metas) getArguments().getSerializable("metaData");
        if (metaEjemplo == null) {
            Toast.makeText(context, "Error: Meta data not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String metaFinalText = metaFinal.getText().toString().trim();
        String fechaFinalIntroducida = fechaFinal.getText().toString().trim();
        if (metaFinalText.isEmpty() || fechaFinalIntroducida.isEmpty()) {
            Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double metaObjetivo;
        try {
            metaObjetivo = Double.parseDouble(metaFinalText);
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Meta final debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        Metas metas = new Metas();
        metas.setIdUsuario(idUsuario);
        metas.setTitulo(metaEjemplo.getTitulo());
        metas.setMontoObjetivo(metaObjetivo);
        metas.setMonto_actual(metaEjemplo.getMonto_actual());
        metas.setFechaLimite(fechaFinalIntroducida);


        System.out.println(metas.toString());

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Metas> call = apiService.postMetasRegister(metas);
        call.enqueue(new Callback<Metas>() {
            @Override
            public void onResponse(Call<Metas> call, Response<Metas> response) {
                Toast.makeText(getContext(), "¡Has creado tu primera Meta!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<Metas> call, Throwable t) {

            }
        });


    }





}