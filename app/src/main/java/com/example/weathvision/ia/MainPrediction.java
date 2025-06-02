package com.example.weathvision.ia;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.TasaInflacion;
import com.example.weathvision.Api.Class.Transaction;
import com.example.weathvision.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private Button predictButton;
    private LineChart lineChart;
    private boolean transaccionesCargadas = false;
    private boolean tasasCargadas = false;
    private float[] entrada_mean;
    private float[] entrada_std;
    private float[] salida_mean;
    private float[] salida_std;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_prediction, container, false);
        context = requireContext();

        predictionTextView = view.findViewById(R.id.predictionTextView);
        predictButton = view.findViewById(R.id.predictButton);
        lineChart = view.findViewById(R.id.lineChart);

        /**
         * Si los datos no se obtuvieron bien, saldrá el boton bloqueado, de esta forma nos aseguramos
         * que este todo listo para la predicción
         * **/
        predictButton.setEnabled(false);

        /**
         * Damos funcionalidad de click para que empiece la predicción
         * **/
        predictButton.setOnClickListener(v -> predictSavings());

        /**
         * Inicializamos la API y los arrays donde se guardaran los datos rescatados.
         * **/
        apiService = ApiClient.getClient().create(ApiService.class);
        transacciones = new ArrayList<>();
        tasasInflacion = new ArrayList<>();

        /**
         * Cargamos los parámetros para normalizar los datos (escaladores) desde un archivo JSON en assets
         * **/
        try {
            loadScalerParams(context);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error cargando parámetros de escalado: " + e.getMessage());
            return view;
        }
        /**
         * Cargamos el modelo de TensorFLow Lite desde la carpeta assets
         * **/
        try {
            tflite = new Interpreter(loadModelFile(context));
        } catch (IOException e) {
            Log.e(TAG, "Error cargando modelo TFLite: " + e.getMessage());
            return view;
        }

        /**
         * Obtengo el id del usuario guardado para consultar sus datos.
         * **/

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("id_usuario", -1);

        /**
         * Llamamos a las metodos para cargar tanto las transacciones del usuario como las tasas previas.
         * **/

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
            predictButton.setText("Predict Savings"); // Restaurar texto original
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
            predictButton.setText("Predict Savings");
            return;
        }

        // Mostrar mensaje de "cargando"
        predictionTextView.setText("Calculando predicciones...");

        // Animación de desvanecimiento para el TextView
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

        List<Entry> entries = new ArrayList<>();
        int[] horizontes = {1, 5, 10};
        List<String> resultados = new ArrayList<>();
        Handler handler = new Handler(Looper.getMainLooper());

        // Procesar cada predicción con un retraso
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
            int idUsuario = sharedPreferences.getInt("id_usuario", -1);
            String nombreUsuario = sharedPreferences.getString("nombre_usuario", "ValorPorDefecto");

            resultados.add(String.format("para %d años: €%.2f", h, prediccion));
            entries.add(new Entry(h, prediccion));

// Programar la actualización del TextView con retraso
            int finalI = i;
            handler.postDelayed(() -> {
                StringBuilder resultadosTexto = new StringBuilder();

                // Agregar mensaje inicial con el nombre del usuario
                resultadosTexto.append(String.format("Las predicciones para %s son:\n", nombreUsuario));

                for (int j = 0; j <= finalI; j++) {
                    resultadosTexto.append(resultados.get(j)).append("\n");
                }

                // Agregar mensaje sobre la precisión del modelo
                resultadosTexto.append("\nNota: Esta IA está en desarrollo y puede presentar errores en las predicciones. Son estimaciones y no siempre son exactas.");

                predictionTextView.setText(resultadosTexto.toString());

                // Si es la última predicción, actualizar el gráfico y habilitar el botón
                if (finalI == horizontes.length - 1) {
                    updateChart(entries);
                    predictButton.setEnabled(true);
                    predictButton.setText("Predict Savings");
                }
            }, (i + 1) * 1000); // Retraso de 1 segundo por predicción
        }

        Log.d(TAG, "Total ingresos: " + totalIngresos);
        Log.d(TAG, "Total gastos: " + totalGastos);
        Log.d(TAG, "Tasas de inflación usadas: " + Arrays.toString(tasas));
    }

    private void updateChart(List<Entry> entries) {
        // Configure dataset
        LineDataSet dataSet = new LineDataSet(entries, "Predicción Ahorros");
        dataSet.setColor(getResources().getColor(R.color.violet));
        dataSet.setValueTextColor(getResources().getColor(R.color.white));
        dataSet.setLineWidth(1.5f); // Reduced for compactness
        dataSet.setCircleRadius(5f); // Smaller circles
        dataSet.setCircleColor(getResources().getColor(R.color.violet));
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f); // Smaller text size

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Configure X-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(entries.size(), true); // Match label count to data points
        xAxis.setValueFormatter((value, axis) -> (int) value + " años");
        xAxis.setTextColor(getResources().getColor(R.color.white));
        xAxis.setTextSize(10f); // Smaller text size
        xAxis.setAvoidFirstLastClipping(true); // Prevent label clipping
        xAxis.setSpaceMin(0.5f); // Add padding to start
        xAxis.setSpaceMax(0.5f); // Add padding to end

        // Configure Y-axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setGranularity(1000f);
        leftAxis.setTextColor(getResources().getColor(R.color.white));
        leftAxis.setValueFormatter((value, axis) -> (int) value + " €");
        leftAxis.setTextSize(10f); // Smaller text size
        leftAxis.setSpaceTop(15f); // Add top padding
        leftAxis.setSpaceBottom(15f); // Add bottom padding
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Chart settings
        lineChart.getDescription().setEnabled(false);
        lineChart.setBackgroundColor(getResources().getColor(R.color.transparente));
        lineChart.setDrawGridBackground(false);
        lineChart.setExtraOffsets(5f, 5f, 5f, 5f); // Add margins around chart
        lineChart.setTouchEnabled(true); // Allow zooming/panning
        lineChart.setPinchZoom(true); // Enable pinch zoom
        lineChart.animateY(1000); // Animation
        lineChart.invalidate(); // Refresh chart
    }
}