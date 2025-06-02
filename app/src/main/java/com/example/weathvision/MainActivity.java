package com.example.weathvision;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.Categoria;
import com.example.weathvision.Api.Class.Metas;
import com.example.weathvision.Api.Class.Transaction;
import com.example.weathvision.transactions.MainTransactions;
import com.example.weathvision.transactions.NewAlert;
import com.example.weathvision.transactions.NewTransaction;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
    private List<Categoria> categorias;
    private TransactionAdapter adapter;
    private MetasAdapter metasAdapter;
    private ApiService apiService;
    private TextView textViewPatrimonio, textViewIngresos, textViewGastado;
    private Categoria selectedCategoria;
    private Button perfil, grafica;
    private PieChart pieChart;
    private Context context;
    private TextView nombre;


    public MainActivity() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);
        context = requireContext();

        perfil = view.findViewById(R.id.perfil);
        perfil.setOnClickListener(v -> irPerfil());

        grafica = view.findViewById(R.id.grafica);
        grafica.setOnClickListener( v -> mostrarGrafica());

        nombre = view.findViewById(R.id.nombre);

        pieChart = view.findViewById(R.id.chart);
        // Initialize TextViews
        textViewPatrimonio = view.findViewById(R.id.patrimonio_actual);
        textViewIngresos = view.findViewById(R.id.ingresos);
        textViewGastado = view.findViewById(R.id.gastado);

        recyclerView = view.findViewById(R.id.recycler_transactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        transacciones = new ArrayList<>();
        categorias = new ArrayList<>();
        adapter = new TransactionAdapter(context, transacciones, categorias, this::deleteTransaction);
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
        String nombreUsuario = sharedPreferences.getString("nombre_usuario", "ValorPorDefecto");
        nombre.setText("¡Hola, " + nombreUsuario + "!");

        if (idUsuario != -1) {

            loadCategorias(idUsuario);
            loadTransacciones(idUsuario);
            loadMetas(idUsuario);
        } else {
            Toast.makeText(context, "No se encontró el usuario logueado", Toast.LENGTH_LONG).show();
            textViewPatrimonio.setText("0.00€");
            textViewIngresos.setText("0.00€");
            textViewGastado.setText("0.00€");
        }


        Button btnAdd = view.findViewById(R.id.btn_add_transaction);
        btnAdd.setOnClickListener(v -> mostrarMasBotones(view));

        return view;
    }


        private void mostrarGrafica() {
            pieChart.setVisibility(pieChart.getVisibility() == VISIBLE ? GONE : VISIBLE);
        }




    private void irPerfil() {
    }

    private void mostrarMasBotones(View view) {
        Button btnAddMain = view.findViewById(R.id.btn_add_transaction);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("id_usuario", -1);

        String patrimonioText = textViewPatrimonio.getText().toString().replace("€", "").trim();
        float patrimonioActual = 0f; // Valor por defecto
        try {
            DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("es", "ES"))); // Configura para español
            df.setParseBigDecimal(true);
            patrimonioActual = df.parse(patrimonioText).floatValue();
        } catch (ParseException e) {
            e.printStackTrace();
            // Maneja el error, por ejemplo, mostrar un mensaje al usuario
            Toast.makeText(context, "Error en el formato del patrimonio", Toast.LENGTH_SHORT).show();
            return; // Evita continuar si hay error
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("patrimonioActual", patrimonioActual);
        editor.apply();

        showAddTransactionDialog(idUsuario);

    }

    private void deleteTransaction(Transaction transaction) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("id_usuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(context, "Usuario no logueado", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<Void> call = apiService.deleteTransaccion(transaction.getIdTransaccion());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    transacciones.remove(transaction);
                    adapter.notifyDataSetChanged();
                    updateTransactionSums();
                    actualizarGrafico();

                    Toast.makeText(context, "Transacción eliminada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error al eliminar la transacción", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadCategorias(int idUsuario) {
        Call<List<Categoria>> call = apiService.getCategorias(idUsuario);
        call.enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categorias.clear();
                    categorias.addAll(response.body());
                    adapter.updateCategorias(categorias);
                    actualizarGrafico();

                } else {
                    Toast.makeText(context, "No se encontraron categorías", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
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
                    metasAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "No se encontraron metas", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Metas>> call, Throwable t) {
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
                    Collections.reverse(transacciones);
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(0);
                    updateTransactionSums();
                    actualizarGrafico();
                } else {
                    Toast.makeText(context, "No se encontraron transacciones", Toast.LENGTH_LONG).show();
                    textViewPatrimonio.setText("0.00€");
                    textViewIngresos.setText("0.00€");
                    textViewGastado.setText("0.00€");
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(context, "Error al cargar transacciones: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

        float patrimonio = totalIngresos - totalGastos;
        textViewPatrimonio.setText(String.format("%.2f€", patrimonio));
        textViewIngresos.setText(String.format("%.2f€", totalIngresos));
        textViewGastado.setText(String.format("%.2f€", totalGastos));
    }



    private void showAddTransactionDialog(int idUsuario) {
        Intent intent = new Intent(getContext(), MainTransactions.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
                    Toast.makeText(context, "Transacción guardada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error al guardar la transacción", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarGrafico() {
        // Crear entradas para el gráfico
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Categoria categoria : categorias) {
            float valor = calcularValorPorCategoria(categoria);
            if (valor > 0) { // Solo añadir entradas con valores mayores a 0
                entries.add(new PieEntry(valor, categoria.getNombre())); // Usar getNombre() para la etiqueta
            }
        }

        // Configurar el conjunto de datos
        PieDataSet dataSet = new PieDataSet(entries, "Categorías");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Colores para las secciones
        dataSet.setValueTextSize(12f); // Tamaño del texto de los valores
        dataSet.setValueTextColor(Color.BLACK); // Color de los valores


        // Configurar los datos del gráfico
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Refrescar el gráfico
        pieChart.invalidate();
    }

    private float calcularValorPorCategoria(Categoria categoria) {
        float total = 0f;
        for (Transaction transaccion : transacciones) {
            // Comparar el nombre de la categoría de la transacción con el nombre de la categoría actual
            if (transaccion.getCategoria().equals(categoria.getNombre())) {
                total += transaccion.getMonto();
            }
        }
        return total;
    }
}