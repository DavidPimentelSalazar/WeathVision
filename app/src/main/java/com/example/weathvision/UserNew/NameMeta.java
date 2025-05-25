package com.example.weathvision.UserNew;



import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weathvision.Api.Class.Metas;
import com.example.weathvision.Api.Class.Transaction;
import com.example.weathvision.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class NameMeta extends Fragment {

    private Button siguiente;
    private TextView titulo;

    private List<Metas> metasList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.name_meta, container, false);

        siguiente = view.findViewById(R.id.siguiente);
        titulo = view.findViewById(R.id.titulo);
        siguiente.setOnClickListener( v -> cambiarFragment());

        return  view;
    }

    private void cambiarFragment() {
        String tituloIntroducido = titulo.getText().toString().trim();

        Metas metaEjemplo = new Metas();
        metaEjemplo.setTitulo(tituloIntroducido);

        PatrimonioActual nextFragment = new PatrimonioActual();
        Bundle bundle = new Bundle();
        bundle.putSerializable("metaData", metaEjemplo);
        nextFragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.mainRegister, nextFragment) // Use nextFragment here
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {






    }





}