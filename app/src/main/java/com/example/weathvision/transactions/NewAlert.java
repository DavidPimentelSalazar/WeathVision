package com.example.weathvision.transactions;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.Metas;
import com.example.weathvision.BarActivity;
import com.example.weathvision.MainActivity;
import com.example.weathvision.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class NewAlert extends Fragment {

    private static final String TAG = "NewAlert";
    private Context context;
    private ApiService apiService;
    private ImageButton calendar;
    private TextView selectedDateTextView;
    private MaterialButton btnToday, btnYesterday;
    private MaterialButtonToggleGroup dateToggleGroup;
    private EditText inputTitulo, inputObjetivo, inputDescription;
    private String selectedDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_alert, container, false);

        // Initialize views
        calendar = view.findViewById(R.id.calendar);
        selectedDateTextView = view.findViewById(R.id.selectedDate);
        btnToday = view.findViewById(R.id.btn_yes);
        btnYesterday = view.findViewById(R.id.btn_no);
        dateToggleGroup = view.findViewById(R.id.radio_buttons_SiNoNvNp);
        inputTitulo = view.findViewById(R.id.titulo);
        inputObjetivo = view.findViewById(R.id.objetivo);
        inputDescription = view.findViewById(R.id.inputDescription);

        // Initialize ApiService
        apiService = ApiClient.getClient().create(ApiService.class);

        // Get user ID from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("id_usuario", -1);
        if (idUsuario == -1) {
            Toast.makeText(context, "Error: Usuario no identificado", Toast.LENGTH_LONG).show();
            return view;
        }





        // Set default date to today
        selectedDate = dateFormat.format(new Date());
        selectedDateTextView.setText("Hoy: " + selectedDate);

        // Calendar button click
        calendar.setOnClickListener(v -> showDatePicker());

        // Today button
        btnToday.setOnClickListener(v -> {
            selectedDate = dateFormat.format(new Date());
            selectedDateTextView.setText("Hoy: " + selectedDate);
            dateToggleGroup.check(R.id.btn_yes);
        });

        // Yesterday button
        btnYesterday.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            selectedDate = dateFormat.format(calendar.getTime());
            selectedDateTextView.setText("Ayer: " + selectedDate);
            dateToggleGroup.check(R.id.btn_no);
        });

        // Registrar button
        view.findViewById(R.id.gastar).setOnClickListener(v -> saveMeta(idUsuario));

        return view;
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona fecha")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = dateFormat.format(new Date(selection));
            selectedDateTextView.setText("Fecha: " + selectedDate);
            dateToggleGroup.clearChecked();
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void saveMeta(int idUsuario) {
        // Validate inputs
        String titulo = inputTitulo.getText().toString().trim();
        String objetivo = inputObjetivo.getText().toString().trim();
        String descripcion = inputDescription.getText().toString().trim();

        if (titulo.isEmpty() || objetivo.isEmpty()) {
            Toast.makeText(context, "Complete los campos de título y objetivo", Toast.LENGTH_SHORT).show();
            return;
        }

        double montoObjetivo;
        try {
            montoObjetivo = Double.parseDouble(objetivo);
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Objetivo inválido, ingrese un número", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        Double montoActual = (double) sharedPreferences.getFloat("patrimonioActual", 0.0f);

        // Create Metas object
        Metas meta = new Metas();
        meta.setIdUsuario(idUsuario);
        meta.setTitulo(titulo);
        meta.setMontoObjetivo(montoObjetivo);
        meta.setMonto_actual(montoActual); // Use the passed patrimonioActual
        meta.setFechaLimite(selectedDate);
        meta.setComentario(descripcion.isEmpty() ? "" : descripcion);

        // Log the Metas object
        Log.d(TAG, "Metas object: " + meta.toString());

        // Log the JSON payload
        Gson gson = new Gson();
        String jsonPayload = gson.toJson(meta);
        Log.d(TAG, "JSON payload: " + jsonPayload);

        // Make API call to save meta
        Call<Metas> call = apiService.postMetasRegister(meta);
        call.enqueue(new Callback<Metas>() {
            @Override
            public void onResponse(Call<Metas> call, Response<Metas> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(context, "Meta registrada: " + titulo + ", Fecha: " + selectedDate, Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getContext(), BarActivity.class);
                    startActivity(intent);
                    resetForm();
                } else {
                    Log.w(TAG, "Failed to save meta, response code: " + response.code() + ", message: " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            Log.w(TAG, "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }
                    }
                    Toast.makeText(context, "Error al registrar meta: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Metas> call, Throwable t) {
                Log.e(TAG, "Error saving meta: " + t.getMessage(), t);
                Toast.makeText(context, "Error al registrar meta: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resetForm() {
        inputTitulo.setText("");
        inputObjetivo.setText("");
        inputDescription.setText("");
        selectedDate = dateFormat.format(new Date());
        selectedDateTextView.setText("Hoy: " + selectedDate);
        dateToggleGroup.check(R.id.btn_yes);
    }
}