package com.example.weathvision.UserNew;





import static android.content.Context.MODE_PRIVATE;

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

import com.example.weathvision.Api.Class.Metas;
import com.example.weathvision.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
public class PatrimonioActual extends Fragment {
    private Button siguiente;
    private EditText patrimonio;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patrimonio_actual, container, false);
        siguiente = view.findViewById(R.id.siguiente);
        patrimonio = view.findViewById(R.id.patrimonio);
        siguiente.setOnClickListener(v -> cambiarFragment());
        return view;
    }

    private void cambiarFragment() {
        Bundle args = getArguments();
        if (args == null || !args.containsKey("metaData")) {
            Toast.makeText(requireContext(), "Error: Meta data not found", Toast.LENGTH_SHORT).show();
            return;
        }
        Metas metaEjemplo = (Metas) args.getSerializable("metaData");

        String patrimonioText = patrimonio.getText().toString().trim();
        // GUARDAR PATRIMONIO

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("patrimonio_actual", patrimonioText);
        editor.apply();


        if (patrimonioText.isEmpty()) {
            patrimonio.setError("Please enter a value");
            return;
        }
        double patrimonioObtenido;
        try {
            patrimonioObtenido = Double.parseDouble(patrimonioText);
        } catch (NumberFormatException e) {
            patrimonio.setError("Invalid number format");
            return;
        }
        metaEjemplo.setMonto_actual(patrimonioObtenido);

        MetaFinal metaFinal = new MetaFinal();
        Bundle bundle = new Bundle();
        bundle.putSerializable("metaData", metaEjemplo);
        metaFinal.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.mainRegister, metaFinal)
                .addToBackStack(null)
                .commit();
    }
}