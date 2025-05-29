package com.example.weathvision;

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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import Adapters.TransactionAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Estadisticas extends Fragment {

    private List<Categoria> categorias;
    private ApiService apiService;
    private TransactionAdapter adapter;
    private Context context;
    private List<Transaction> transacciones;
    private BarChart barChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.estadisticas_fragment, container, false);
        context = requireContext();

        // Inicializar el gráfico
        barChart = view.findViewById(R.id.chart);
        barChart.getAxisRight().setDrawLabels(false);

        // Configurar el eje Y
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f); // Ajusta según los datos reales
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10);

        // Configurar el eje X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(45);
        barChart.getDescription().setEnabled(false);

        // Inicializar API y categorías
        apiService = ApiClient.getClient().create(ApiService.class);
        categorias = new ArrayList<>();
        transacciones = new ArrayList<>(); // Inicializar transacciones si no lo está
        adapter = new TransactionAdapter(context, transacciones, categorias);

        // Cargar categorías desde la API
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("id_usuario", -1);
        CargarCategorias(idUsuario);

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

                    // Actualizar el gráfico con las categorías
                    actualizarGrafico();
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

    private void actualizarGrafico() {
        // Crear una lista de nombres de categorías para el eje X
        List<String> nombresCategorias = new ArrayList<>();
        for (Categoria categoria : categorias) {
            nombresCategorias.add(categoria.getNombre()); // Asegúrate de que `getNombre()` es el método correcto
        }

        // Crear entradas para el gráfico (esto es un ejemplo, ajusta según tus datos reales)
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < categorias.size(); i++) {
            // Aquí deberías calcular los valores reales para cada categoría, por ejemplo, sumando transacciones
            float valor = calcularValorPorCategoria(categorias.get(i)); // Implementa esta lógica
            entries.add(new BarEntry(i, valor));
        }

        // Configurar el conjunto de datos
        BarDataSet dataSet = new BarDataSet(entries, "Categorías");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        // Configurar los datos del gráfico
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Configurar el eje X con las categorías
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(nombresCategorias));

        // Refrescar el gráfico
        barChart.invalidate();
    }

    private float calcularValorPorCategoria(Categoria categoria) {
        // Implementa la lógica para calcular el valor asociado a la categoría
        // Por ejemplo, suma el monto de todas las transacciones asociadas a esta categoría
        float total = 0f;
        for (Transaction transaccion : transacciones) {
            if (transaccion.getIdTransaccion() == categoria.getIdCategoria()) { // Asegúrate de que `getCategoriaId()` y `getId()` existan
                total += transaccion.getMonto(); // Asegúrate de que `getMonto()` exista
            }
        }
        return total;
    }
}