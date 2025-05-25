package com.example.weathvision;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import Adapters.CategoriasAdapter;

public class Categorias extends Fragment {

    private RecyclerView recyclerView;

    private CategoriasAdapter categoriasAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.categorias_fragment, container, false);

        return view;
    }



}
