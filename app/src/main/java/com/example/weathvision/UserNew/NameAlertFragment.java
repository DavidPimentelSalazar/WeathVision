package com.example.weathvision.UserNew;



import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.weathvision.R;
import com.example.weathvision.transactions.MainTransactions;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class NameAlertFragment extends Fragment {

    private TextView titulo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.name_alert_fragment, container, false);

        temporizador();

        titulo = view.findViewById(R.id.titulo);
        titulo.setText("¡Hola!, te recibimos en WealthVision.");

        return  view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {




    }



    private void temporizador() {

        // Create and start a 10-second timer
        new CountDownTimer(5000, 1000) { // 10000ms = 10s, 1000ms = tick interval
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                Intent intent = new Intent(getContext(), MainTransactions.class);
                startActivity(intent);

            }
        }.start();
    }




}