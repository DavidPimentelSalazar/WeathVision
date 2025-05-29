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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.Categoria;
import com.example.weathvision.Api.Class.Transaction;
import com.example.weathvision.BarActivity;
import com.example.weathvision.MainActivity;
import com.example.weathvision.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Adapters.CategoriasAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewTransaction extends Fragment {

    private static final String TAG = "NewTransaction";
    private Context context;
    private ApiService apiService;
    private ImageButton calendar;
    private TextView selectedDateTextView;
    private MaterialButton btnToday, btnYesterday;
    private MaterialButtonToggleGroup dateToggleGroup;
    private EditText inputMonto, inputDescription;
    private Categoria selectedCategoria;
    private String selectedDate; // Store selected date in "yyyy-MM-dd" format
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_transaction, container, false);

        // Initialize views
        calendar = view.findViewById(R.id.calendar);
        selectedDateTextView = view.findViewById(R.id.selectedDate);
        btnToday = view.findViewById(R.id.btn_yes);
        btnYesterday = view.findViewById(R.id.btn_no);
        dateToggleGroup = view.findViewById(R.id.radio_buttons_SiNoNvNp);
        inputMonto = view.findViewById(R.id.inputMonto);
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

        // Gastar and Ingresar buttons
        view.findViewById(R.id.gastar).setOnClickListener(v -> saveTransaction(idUsuario, "Gasto"));
        view.findViewById(R.id.ingresar).setOnClickListener(v -> saveTransaction(idUsuario, "Ingreso"));

        // Load categories
        cargarCategorias(view, idUsuario);

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
            dateToggleGroup.clearChecked(); // Uncheck today/yesterday when custom date is selected
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void cargarCategorias(View view, int idUsuario) {
        RecyclerView recyclerCategorias = view.findViewById(R.id.Categoria);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerCategorias.setLayoutManager(layoutManager);

        List<Categoria> categoriasDialog = new ArrayList<>();
        CategoriasAdapter categoryAdapter = new CategoriasAdapter(context, categoriasDialog, categoria -> {
            selectedCategoria = categoria;
            Toast.makeText(context, "Selected: " + categoria.getNombre(), Toast.LENGTH_SHORT).show();
        });
        recyclerCategorias.setAdapter(categoryAdapter);

        // Load categories
        Call<List<Categoria>> call = apiService.getCategorias(idUsuario);
        call.enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoriasDialog.clear();
                    categoriasDialog.addAll(response.body());
                    categoryAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + categoriasDialog.size() + " categories for user " + idUsuario);
                } else {
                    Log.w(TAG, "Failed to load categories, response code: " + response.code() + ", message: " + response.message());
                    Toast.makeText(context, "No se encontraron categorías", Toast.LENGTH_LONG).show();
                    categoriasDialog.clear();
                    categoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Log.e(TAG, "Error loading categories: " + t.getMessage(), t);
                Toast.makeText(context, "Error al cargar categorías: " + t.getMessage(), Toast.LENGTH_LONG).show();
                categoriasDialog.clear();
                categoryAdapter.notifyDataSetChanged();
            }
        });
    }

    private void saveTransaction(int idUsuario, String tipo) {
        // Validate inputs
        if (selectedCategoria == null) {
            Toast.makeText(context, "Selecciona una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        String descripcion = inputDescription.getText().toString().trim();
        String monto = inputMonto.getText().toString().trim();

        if (descripcion.isEmpty() || monto.isEmpty()) {
            Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        float montoValue;
        try {
            montoValue = Float.parseFloat(monto);
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Monto inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Transaction object
        Transaction nueva = new Transaction();
        nueva.setIdUsuario(idUsuario);
        nueva.setTipo(tipo);
        nueva.setMonto(montoValue);
        nueva.setCategoria(selectedCategoria.getNombre());
        nueva.setDescripcion(descripcion);
        nueva.setFecha(selectedDate);

        // Make API call to save transaction
        Call<Transaction> call = apiService.postTransaccion(nueva);
        call.enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(context, "Transacción guardada: " + tipo + ", Fecha: " + selectedDate, Toast.LENGTH_LONG).show();

                   Intent intent = new Intent(getContext(), BarActivity.class);
                   startActivity(intent);

                    resetForm();
                } else {
                    Log.w(TAG, "Failed to save transaction, response code: " + response.code() + ", message: " + response.message());
                    Toast.makeText(context, "Error al guardar transacción: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {
                Log.e(TAG, "Error saving transaction: " + t.getMessage(), t);
                Toast.makeText(context, "Error al guardar transacción: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }

    private void resetForm() {
        inputMonto.setText("");
        inputDescription.setText("");
        selectedCategoria = null;
        selectedDate = dateFormat.format(new Date());
        selectedDateTextView.setText("Hoy: " + selectedDate);
        dateToggleGroup.check(R.id.btn_yes);
    }
}