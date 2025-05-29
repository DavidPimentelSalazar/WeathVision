package com.example.weathvision.ia;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.Categoria;
import com.example.weathvision.Api.Class.Transaction;
import com.example.weathvision.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import Adapters.TransactionAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPrediction extends Fragment {

    private List<Categoria> categorias;
    private List<Transaction> transacciones;
    private ApiService apiService;
    private TransactionAdapter adapter;
    private Context context;
    private PieChart pieChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_prediction, container, false);
        context = requireContext();

        // Inicializar el gráfico
        pieChart = view.findViewById(R.id.chart);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true); // Mostrar valores en porcentaje
        pieChart.setEntryLabelColor(Color.BLACK); // Color de las etiquetas
        pieChart.setEntryLabelTextSize(12f); // Tamaño del texto de las etiquetas

        // Inicializar API y listas
        apiService = ApiClient.getClient().create(ApiService.class);
        categorias = new ArrayList<>();
        transacciones = new ArrayList<>();
        adapter = new TransactionAdapter(context, transacciones, categorias);

        // Cargar categorías y transacciones desde la API
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("id_usuario", -1);
        CargarCategorias(idUsuario);
        CargarTransacciones(idUsuario);

        return view;
    }

    private void CargarCategorias(int idUsuario) {
        Call<List<Categoria>> call = apiService.getCategorias(idUsuario);
        call.enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categorias.clear();
                    categorias.addAll(response.body());
                    adapter.updateCategorias(categorias);
                    Log.d(TAG, "Loaded " + categorias.size() + " categories for user " + idUsuario);
                    actualizarGrafico(); // Actualizar gráfico después de cargar categorías
                } else {
                    Log.w(TAG, "Failed to load categories, response code: " + response.code() + ", message: " + response.message());
                    Toast.makeText(context, "No se encontraron categorías", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Log.e(TAG, "Error loading categories: " + t.getMessage(), t);
                Toast.makeText(context, "Error al cargar categorías: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void CargarTransacciones(int idUsuario) {
        Call<List<Transaction>> call = apiService.getTransacciones(idUsuario);
        call.enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transacciones.clear();
                    transacciones.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + transacciones.size() + " transactions for user " + idUsuario);
                } else {
                    Log.w(TAG, "Failed to load transactions, response code: " + response.code() + ", message: " + response.message());
                    Toast.makeText(context, "No se encontraron transacciones", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Log.e(TAG, "Error loading transactions: " + t.getMessage(), t);
                Toast.makeText(context, "Error al cargar transacciones: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
        Log.d(TAG, "Categoria: " + categoria.getNombre() + ", Valor: " + total); // Para depuración
        return total;
    }
}




