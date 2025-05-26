package com.example.weathvision;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.Categoria;
import com.example.weathvision.Api.Class.Metas;
import com.example.weathvision.Api.Class.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Adapters.CategoriasAdapter;
import Adapters.MetasAdapter;
import Adapters.TransactionAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Fragment {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView, recyclerMetas;
    private List<Transaction> transacciones;
    private List<Metas> metas;
    private List<Categoria> categorias; // Add a list to hold categories
    private TransactionAdapter adapter;
    private MetasAdapter metasAdapter;
    private ApiService apiService;
    private TextView textViewPatrimonio, textViewIngresos, textViewGastado;
    private Categoria selectedCategoria;

    private Button perfil;
    private Context context;

    public MainActivity() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);
        context = requireContext();

        perfil = view.findViewById(R.id.perfil);
        perfil.setOnClickListener(v -> irPerfil());

        // Initialize TextViews
        textViewPatrimonio = view.findViewById(R.id.patrimonio_actual);
        textViewIngresos = view.findViewById(R.id.ingresos);
        textViewGastado = view.findViewById(R.id.gastado);

        recyclerView = view.findViewById(R.id.recycler_transactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        transacciones = new ArrayList<>();
        categorias = new ArrayList<>(); // Initialize the categorias list
        adapter = new TransactionAdapter(context, transacciones, categorias); // Pass categorias to TransactionAdapter
        recyclerView.setAdapter(adapter);

        recyclerMetas = view.findViewById(R.id.recycler_metas);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerMetas.setLayoutManager(layoutManager);
        metas = new ArrayList<>();
        metasAdapter = new MetasAdapter(context, metas);
        recyclerMetas.setAdapter(metasAdapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("id_usuario", -1);

        if (idUsuario != -1) {
            loadCategorias(idUsuario); // Load categories first
            loadTransacciones(idUsuario);
            loadMetas(idUsuario);
        } else {
            Log.w(TAG, "No user logged in, idUsuario: " + idUsuario);
            Toast.makeText(context, "No se encontró el usuario logueado", Toast.LENGTH_LONG).show();
            // Set initial values to 0
            textViewPatrimonio.setText("0.00€");
            textViewIngresos.setText("0.00€");
            textViewGastado.setText("0.00€");
        }

        FloatingActionButton btnAdd = view.findViewById(R.id.btn_add_transaction);
        btnAdd.setOnClickListener(v -> mostrarMasBotones(view));

        return view;
    }

    private void irPerfil() {
        // Implement profile navigation if needed
    }

    private void mostrarMasBotones(View view) {
        FloatingActionButton btnAdd = view.findViewById(R.id.floating_button_2);
        FloatingActionButton btnAddMain = view.findViewById(R.id.btn_add_transaction);

        if (btnAdd.getVisibility() == VISIBLE) {
            showAddTransactionDialog();
        } else if (btnAdd.getVisibility() == GONE) {
            btnAdd.setVisibility(VISIBLE);
        }
    }

    private void loadCategorias(int idUsuario) {
        Call<List<Categoria>> call = apiService.getCategorias();
        call.enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categorias.clear();
                    categorias.addAll(response.body());
                    for (Categoria c : categorias) {
                        Log.d(TAG, "Category: nombre=" + c.getNombre() + ", imagen=" + c.getImagen());
                    }
                    adapter.updateCategorias(categorias); // Update adapter with new categories
                    Log.d(TAG, "Loaded " + categorias.size() + " categories");
                } else {
                    Log.w(TAG, "Failed to load categories, response code: " + response.code());
                    Toast.makeText(context, "No se encontraron categorías", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Log.e(TAG, "Error loading categories: " + t.getMessage());
                Toast.makeText(context, "Error al cargar categorías: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadMetas(int idUsuario) {
        Call<List<Metas>> call = apiService.getMetas(idUsuario);
        call.enqueue(new Callback<List<Metas>>() {
            @Override
            public void onResponse(Call<List<Metas>> call, Response<List<Metas>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    metas.clear();
                    metas.addAll(response.body());
                    metasAdapter.notifyDataSetChanged(); // Notify adapter to refresh with new category data
                    Log.d(TAG, "Loaded " + metas.size() + " metas");
                } else {
                    Log.w(TAG, "Failed to load metas, response code: " + response.code());
                    Toast.makeText(context, "No se encontraron metas", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Metas>> call, Throwable t) {
                Log.e(TAG, "Error loading metas: " + t.getMessage());
                Toast.makeText(context, "Error al cargar metas: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadTransacciones(int idUsuario) {
        Call<List<Transaction>> call = apiService.getTransacciones(idUsuario);
        call.enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transacciones.clear();
                    transacciones.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateTransactionSums();
                    Log.d(TAG, "Loaded " + transacciones.size() + " transactions");
                    for (Transaction t : transacciones) {
                        Log.d(TAG, "Transaction: id=" + t.getIdTransaccion() + ", tipo=" + t.getTipo() + ", monto=" + t.getMonto());
                    }
                } else {
                    Log.w(TAG, "Failed to load transactions, response code: " + response.code());
                    Toast.makeText(context, "No se encontraron transacciones", Toast.LENGTH_LONG).show();
                    // Set sums to 0 if no transactions
                    textViewPatrimonio.setText("0.00€");
                    textViewIngresos.setText("0.00€");
                    textViewGastado.setText("0.00€");
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Log.e(TAG, "Error loading transactions: " + t.getMessage());
                Toast.makeText(context, "Error al cargar transacciones: " + t.getMessage(), Toast.LENGTH_LONG).show();
                // Set sums to 0 on failure
                textViewPatrimonio.setText("0.00€");
                textViewIngresos.setText("0.00€");
                textViewGastado.setText("0.00€");
            }
        });
    }

    private void updateTransactionSums() {
        float totalIngresos = 0;
        float totalGastos = 0;

        for (Transaction transaction : transacciones) {
            if (transaction.getTipo() != null && transaction.getTipo().equalsIgnoreCase("Ingreso")) {
                totalIngresos += transaction.getMonto();
            } else if (transaction.getTipo() != null && transaction.getTipo().equalsIgnoreCase("Gasto")) {
                totalGastos += transaction.getMonto();
            }
        }

        // Calculate net balance (patrimonio)
        float patrimonio = totalIngresos - totalGastos;

        // Update TextViews with formatted sums
        textViewPatrimonio.setText(String.format("%.2f€", patrimonio));
        textViewIngresos.setText(String.format("%.2f€", totalIngresos));
        textViewGastado.setText(String.format("%.2f€", totalGastos));
        Log.d(TAG, "Updated sums: ingresos=" + totalIngresos + ", gastos=" + totalGastos + ", patrimonio=" + patrimonio);
    }

    private void showAddTransactionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog);
        builder.setTitle("Nueva transacción");

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.transaction_alert, null);
        builder.setView(dialogView);

        EditText inputDescripcion = dialogView.findViewById(R.id.Descripcion);
        EditText inputMonto = dialogView.findViewById(R.id.monto);
        Button btnGastar = dialogView.findViewById(R.id.btnGastar);
        Button btnIngresar = dialogView.findViewById(R.id.btnIngresar);
        RecyclerView recyclerCategorias = dialogView.findViewById(R.id.Categoria);

        recyclerCategorias.setLayoutManager(new LinearLayoutManager(context));

        List<Categoria> categoriasDialog = new ArrayList<>();
        CategoriasAdapter categoryAdapter = new CategoriasAdapter(context, categoriasDialog, categoria -> {
            selectedCategoria = categoria;
        });
        recyclerCategorias.setAdapter(categoryAdapter);

        // Fetch categories from API
        Call<List<Categoria>> call = apiService.getCategorias();
        call.enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoriasDialog.clear();
                    categoriasDialog.addAll(response.body());
                    categoryAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + categoriasDialog.size() + " categories for dialog");
                } else {
                    Log.w(TAG, "Failed to load categories, response code: " + response.code());
                    Toast.makeText(context, "No se encontraron categorías", Toast.LENGTH_LONG).show();
                    // Set a default empty adapter or handle the error
                    categoriasDialog.clear();
                    categoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Log.e(TAG, "Error loading categories: " + t.getMessage());
                Toast.makeText(context, "Error al cargar categorías: " + t.getMessage(), Toast.LENGTH_LONG).show();
                categoriasDialog.clear();
                categoryAdapter.notifyDataSetChanged();
            }
        });

        AlertDialog dialog = builder.create();
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("id_usuario", -1);

        btnGastar.setOnClickListener(v -> {
            String descripcion = inputDescripcion.getText().toString().trim();
            String monto = inputMonto.getText().toString().trim();
            if (!descripcion.isEmpty() && !monto.isEmpty() && selectedCategoria != null) {
                Transaction nueva = new Transaction();
                nueva.setIdUsuario(idUsuario);
                nueva.setTipo("Gasto");
                nueva.setMonto(Float.parseFloat(monto));
                nueva.setCategoria(selectedCategoria.getNombre());
                nueva.setDescripcion(descripcion);
                nueva.setFecha(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

                // Send to backend
                saveTransaction(nueva);
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Complete todos los campos y seleccione una categoría", Toast.LENGTH_SHORT).show();
            }
        });

        btnIngresar.setOnClickListener(v -> {
            String descripcion = inputDescripcion.getText().toString().trim();
            String monto = inputMonto.getText().toString().trim();
            if (!descripcion.isEmpty() && !monto.isEmpty() && selectedCategoria != null) {
                Transaction nueva = new Transaction();
                nueva.setIdUsuario(idUsuario);
                nueva.setTipo("Ingreso");
                nueva.setMonto(Float.parseFloat(monto));
                nueva.setCategoria(selectedCategoria.getNombre());
                nueva.setDescripcion(descripcion);
                nueva.setFecha(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

                // Send to backend
                saveTransaction(nueva);
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Complete todos los campos y seleccione una categoría", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void saveTransaction(Transaction transaccion) {
        Call<Transaction> call = apiService.postTransaccion(transaccion);
        call.enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transacciones.add(0, response.body());
                    adapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                    updateTransactionSums();
                    Log.d(TAG, "Transaction saved: tipo=" + response.body().getTipo() + ", monto=" + response.body().getMonto());
                    Toast.makeText(context, "Transacción guardada", Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(TAG, "Failed to save transaction, response code: " + response.code());
                    Toast.makeText(context, "Error al guardar la transacción", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {
                Log.e(TAG, "Error saving transaction: " + t.getMessage());
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}