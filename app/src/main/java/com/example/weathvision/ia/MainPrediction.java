package com.example.weathvision.ia;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.TasaInflacion;
import com.example.weathvision.Api.Class.Transaction;
import com.example.weathvision.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPrediction extends Fragment {

    private static final String TAG = "MainPrediction";
    private List<Transaction> transacciones;
    private List<TasaInflacion> tasasInflacion;
    private ApiService apiService;
    private Context context;
    private Interpreter tflite;
    private TextView predictionTextView;
    private MaterialButton predictButton;
    private MaterialButton graficasButton;
    private BarChart barChart;
    private Spinner timePeriodSpinner;
    private ImageView imageView;
    private boolean transaccionesCargadas = false;
    private boolean tasasCargadas = false;
    private float[] entrada_mean;
    private float[] entrada_std;
    private float[] salida_mean;
    private float[] salida_std;
    private String selectedTimePeriod = "Month"; // Default to Month

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_prediction, container, false);
        context = requireContext();

        predictionTextView = view.findViewById(R.id.predictionTextView);
        predictButton = view.findViewById(R.id.predictButton);
        graficasButton = view.findViewById(R.id.graficas);
        barChart = view.findViewById(R.id.barChart);
        imageView = view.findViewById(R.id.imageView);

        barChart.setVisibility(GONE);





        timePeriodSpinner = view.findViewById(R.id.timePeriodSpinner);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.time_periods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timePeriodSpinner.setAdapter(adapter);
        timePeriodSpinner.setSelection(1);
        timePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimePeriod = parent.getItemAtPosition(position).toString();
                if (graficasButton.isChecked()) {
                    updateBarChart();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Disable predict button initially
        predictButton.setEnabled(false);

        // Set up toggle group listener
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.radio_buttons_SiNoNvNp);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.predictButton) {
                    predictSavings();
                } else if (checkedId == R.id.graficas) {
                    updateBarChart();
                }
            }
        });

        // Initialize API and data lists
        apiService = ApiClient.getClient().create(ApiService.class);
        transacciones = new ArrayList<>();
        tasasInflacion = new ArrayList<>();

        // Load scaler parameters
        try {
            loadScalerParams(context);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error cargando parámetros de escalado: " + e.getMessage());
            return view;
        }

        // Load TensorFlow Lite model
        try {
            tflite = new Interpreter(loadModelFile(context));
        } catch (IOException e) {
            Log.e(TAG, "Error cargando modelo TFLite: " + e.getMessage());
            return view;
        }

        // Get user ID
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("id_usuario", -1);


        CargarTransacciones(idUsuario);
        CargarTasasInflacion();


        return view;
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        android.content.res.AssetFileDescriptor fileDescriptor = context.getAssets().openFd("modeloNuevo.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void loadScalerParams(Context context) throws IOException, JSONException {
        InputStream is = context.getAssets().open("scaler_params.json");
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, "UTF-8");
        JSONObject jsonObject = new JSONObject(json);

        entrada_mean = toFloatArray(jsonObject.getJSONArray("entrada_mean"));
        entrada_std = toFloatArray(jsonObject.getJSONArray("entrada_std"));
        salida_mean = toFloatArray(jsonObject.getJSONArray("salida_mean"));
        salida_std = toFloatArray(jsonObject.getJSONArray("salida_std"));

        Log.d(TAG, "Scaler params loaded: entrada_mean=" + Arrays.toString(entrada_mean) +
                ", entrada_std=" + Arrays.toString(entrada_std) +
                ", salida_mean=" + Arrays.toString(salida_mean) +
                ", salida_std=" + Arrays.toString(salida_std));
    }

    private float[] toFloatArray(JSONArray jsonArray) throws JSONException {
        float[] array = new float[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            array[i] = (float) jsonArray.getDouble(i);
        }
        return array;
    }

    private void habilitarSiListos() {
        if (transaccionesCargadas && tasasCargadas) {
            predictButton.setEnabled(true);
            predictButton.setText("Predecir");
        }
    }

    private void CargarTransacciones(int idUsuario) {
        Call<List<Transaction>> call = apiService.getTransacciones(idUsuario);
        call.enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transacciones.clear();
                    transacciones.addAll(response.body());
                    transaccionesCargadas = true;
                    Log.d(TAG, "Loaded " + transacciones.size() + " transactions for user " + idUsuario);
                    habilitarSiListos();
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

    private void CargarTasasInflacion() {
        Call<List<TasaInflacion>> call = apiService.obtenerTasas();
        call.enqueue(new Callback<List<TasaInflacion>>() {
            @Override
            public void onResponse(Call<List<TasaInflacion>> call, Response<List<TasaInflacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tasasInflacion.clear();
                    tasasInflacion.addAll(response.body());
                    tasasCargadas = true;
                    Log.d(TAG, "Loaded " + tasasInflacion.size() + " inflation rates");
                    habilitarSiListos();
                } else {
                    Log.w(TAG, "Failed to load inflation rates, response code: " + response.code() + ", message: " + response.message());
                    Toast.makeText(context, "No se encontraron tasas de inflación", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<TasaInflacion>> call, Throwable t) {
                Log.e(TAG, "Error loading inflation rates: " + t.getMessage(), t);
                Toast.makeText(context, "Error al cargar tasas de inflación: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void predictSavings() {

        if (transacciones.isEmpty() || tasasInflacion.size() < 5 || tflite == null) {
            predictionTextView.setText("Predicted Savings: Insufficient data");
            predictButton.setEnabled(true);
            predictButton.setText("Predecir");
            barChart.setVisibility(GONE);
            return;
        }

        // Show loading message
        predictionTextView.setText("Calculando predicciones...");

        // Fade-in animation
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        predictionTextView.startAnimation(fadeIn);

        // Calculate total incomes and expenses
        float totalIngresos = 0f;
        float totalGastos = 0f;
        for (Transaction t : transacciones) {
            if ("Ingreso".equals(t.getTipo())) {
                totalIngresos += t.getMonto();
            } else if ("Gasto".equals(t.getTipo())) {
                totalGastos += t.getMonto();
            }
        }

        // Prepare inflation rates
        float[] tasas = new float[10];
        int startIndex = Math.max(0, tasasInflacion.size() - 10);
        for (int i = 0; i < 10; i++) {
            if (startIndex + i < tasasInflacion.size()) {
                tasas[i] = tasasInflacion.get(startIndex + i).getTasaInflacion();
            } else {
                tasas[i] = 2.0f; // Default inflation rate
            }
        }

        List<com.github.mikephil.charting.data.Entry> entries = new ArrayList<>();
        int[] horizontes = {1, 5, 10};
        List<String> resultados = new ArrayList<>();
        Handler handler = new Handler(Looper.getMainLooper());

        // Process predictions with delay
        for (int i = 0; i < horizontes.length; i++) {
            int h = horizontes[i];
            float promedioInflacion = 0f;
            for (int j = 0; j < h; j++) {
                promedioInflacion += tasas[j];
            }
            promedioInflacion /= h;

            // Prepare and scale input
            float[] input = new float[]{totalIngresos, totalGastos, promedioInflacion, (float) h};
            float[] input_scaled = new float[4];
            for (int j = 0; j < 4; j++) {
                input_scaled[j] = (input[j] - entrada_mean[j]) / entrada_std[j];
            }

            // Run prediction
            float[][] output = new float[1][1];
            tflite.run(input_scaled, output);

            // Inverse-scale output
            float prediccion_scaled = output[0][0];
            float prediccion = prediccion_scaled * salida_std[0] + salida_mean[0];

            SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String nombreUsuario = sharedPreferences.getString("nombre_usuario", "ValorPorDefecto");

            resultados.add(String.format("- %d años: %.2f €", h, prediccion));
            entries.add(new com.github.mikephil.charting.data.Entry(h, prediccion));

            // Update TextView with delay
            int finalI = i;
            handler.postDelayed(() -> {
                StringBuilder resultadosTexto = new StringBuilder();
                resultadosTexto.append(String.format("Tus predicciones, %s, son:\n\n", nombreUsuario));
                for (int j = 0; j <= finalI; j++) {
                    resultadosTexto.append(resultados.get(j)).append("\n");
                }
                predictionTextView.startAnimation(fadeIn);
                resultadosTexto.append("\nNota: Esta IA está en desarrollo y puede presentar errores en las predicciones. Son estimaciones y no siempre son exactas.");
                predictionTextView.setText(resultadosTexto.toString());

                // Update chart on last prediction
                if (finalI == horizontes.length - 1) {
                    updatePredictionChart(entries);
                    predictButton.setEnabled(true);
                    predictButton.setText("Predecir");
                }
            }, (i + 1) * 1000); // 1-second delay per prediction
        }

    }

    private void updatePredictionChart(List<com.github.mikephil.charting.data.Entry> entries) {
        imageView.setVisibility(GONE);
        barChart.setVisibility(VISIBLE);
        timePeriodSpinner.setVisibility(GONE);

        // Convert entries to BarEntry
        List<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            com.github.mikephil.charting.data.Entry entry = entries.get(i);
            barEntries.add(new BarEntry(entry.getX(), entry.getY()));
        }

        // Configure dataset
        BarDataSet dataSet = new BarDataSet(barEntries, "Predicción Ahorros");
        dataSet.setColor(getResources().getColor(R.color.violet));
        dataSet.setValueTextColor(getResources().getColor(R.color.white));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f); // Set bar width

        barChart.setData(barData);

        // Configure X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(barEntries.size(), true);
        xAxis.setValueFormatter((value, axis) -> (int) value + " años");
        xAxis.setTextColor(getResources().getColor(R.color.white));
        xAxis.setTextSize(10f);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);

        // Configure Y-axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1000f);
        leftAxis.setTextColor(getResources().getColor(R.color.white));
        leftAxis.setValueFormatter((value, axis) -> (int) value + " €");
        leftAxis.setTextSize(10f);
        leftAxis.setSpaceTop(15f);
        leftAxis.setSpaceBottom(15f);
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Chart settings
        barChart.getDescription().setEnabled(false);
        barChart.setBackgroundColor(getResources().getColor(R.color.transparente));
        barChart.setDrawGridBackground(false);
        barChart.setExtraOffsets(5f, 5f, 5f, 5f);
        barChart.setTouchEnabled(true);
        barChart.setPinchZoom(true);
        barChart.animateY(1000);
        barChart.invalidate();
    }
    private void updateBarChart() {
        imageView.setVisibility(View.GONE);
        timePeriodSpinner.setVisibility(View.VISIBLE);

        if (transacciones.isEmpty()) {
            predictionTextView.setText("No hay transacciones para mostrar");
            barChart.setVisibility(View.GONE);
            return;
        }

        barChart.setVisibility(View.VISIBLE);

        // Maps to store incomes and expenses by time period (use Double instead of Float)
        TreeMap<String, Double> ingresosPorPeriodo = new TreeMap<>();
        TreeMap<String, Double> gastosPorPeriodo = new TreeMap<>();

        // Date format based on selected time period
        SimpleDateFormat dateFormat;
        switch (selectedTimePeriod) {
            case "Day":
                dateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
                break;
            case "Month":
                dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                break;
            case "Year":
                dateFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                break;
            default:
                dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                break;
        }

        // Group transactions by time period
        for (Transaction t : transacciones) {
            try {
                Date fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(t.getFecha());
                String key = dateFormat.format(fecha);
                double monto = t.getMonto(); // Use double here

                if ("Ingreso".equals(t.getTipo())) {
                    ingresosPorPeriodo.put(key, ingresosPorPeriodo.getOrDefault(key, 0.0) + monto);
                } else if ("Gasto".equals(t.getTipo())) {
                    gastosPorPeriodo.put(key, gastosPorPeriodo.getOrDefault(key, 0.0) + monto);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date: " + t.getFecha(), e);
            }
        }

        // Combine keys from both maps to ensure all periods are included
        Set<String> allPeriods = new TreeSet<>();
        allPeriods.addAll(ingresosPorPeriodo.keySet());
        allPeriods.addAll(gastosPorPeriodo.keySet());

        // Prepare bar entries for incomes and expenses
        List<BarEntry> ingresosEntries = new ArrayList<>();
        List<BarEntry> gastosEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (String period : allPeriods) {
            labels.add(period);
            double ingreso = ingresosPorPeriodo.getOrDefault(period, 0.0);
            double gasto = gastosPorPeriodo.getOrDefault(period, 0.0);
            ingresosEntries.add(new BarEntry(index, (float) ingreso)); // Cast to float for BarEntry
            gastosEntries.add(new BarEntry(index + 0.45f, (float) gasto)); // Cast to float for BarEntry
            index++;
        }

        // Create datasets for incomes and expenses
        BarDataSet ingresosDataSet = new BarDataSet(ingresosEntries, "Ingresos");
        ingresosDataSet.setColor(getResources().getColor(R.color.green));
        ingresosDataSet.setValueTextColor(getResources().getColor(R.color.white));
        ingresosDataSet.setValueTextSize(10f);

        BarDataSet gastosDataSet = new BarDataSet(gastosEntries, "Gastos");
        gastosDataSet.setColor(getResources().getColor(R.color.red));
        gastosDataSet.setValueTextColor(getResources().getColor(R.color.white));
        gastosDataSet.setValueTextSize(10f);

        // Combine datasets
        BarData barData = new BarData(ingresosDataSet, gastosDataSet);
        barData.setBarWidth(0.45f);

        barChart.setData(barData);

        // Configure X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size(), true);
        xAxis.setValueFormatter((value, axis) -> {
            int idx = (int) value;
            return idx < labels.size() ? labels.get(idx) : "";
        });
        xAxis.setTextColor(getResources().getColor(R.color.white));
        xAxis.setTextSize(10f);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);

        // Configure Y-axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(100f);
        leftAxis.setTextColor(getResources().getColor(R.color.white));
        leftAxis.setValueFormatter((value, axis) -> (int) value + " €");
        leftAxis.setTextSize(10f);
        leftAxis.setSpaceTop(15f);
        leftAxis.setSpaceBottom(15f);
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Chart settings
        barChart.getDescription().setEnabled(false);
        barChart.setBackgroundColor(getResources().getColor(R.color.transparente));
        barChart.setDrawGridBackground(false);
        barChart.setExtraOffsets(5f, 5f, 5f, 5f);
        barChart.setTouchEnabled(true);
        barChart.setPinchZoom(true);
        barChart.animateY(1000);
        barChart.invalidate();
    }
}