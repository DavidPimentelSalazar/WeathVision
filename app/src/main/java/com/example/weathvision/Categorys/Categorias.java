package com.example.weathvision.Categorys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.Categoria;
import com.example.weathvision.R;
import com.example.weathvision.UserNew.PatrimonioActual;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import Adapters.CategoriasAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Categorias extends Fragment {

    private static final String TAG = "Categorias";
    private RecyclerView recyclerView;
    private CategoriasAdapter categoriasAdapter;
    private List<Categoria> categoriasList;
    private List<Categoria> filteredCategoriasList;
    private ApiService apiService;
    private TextInputLayout buscarCategoria;
    private Button anadirButton;
    private Context context;
    private int idUsuario;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.categorias_fragment, container, false);
        context = requireContext();

        // Get user ID from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        idUsuario = sharedPreferences.getInt("id_usuario", -1);
        if (idUsuario == -1) {
            Toast.makeText(context, "Usuario no logueado", Toast.LENGTH_LONG).show();
            return view;
        }

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerCategorias);
        buscarCategoria = view.findViewById(R.id.buscarCategoria);
        anadirButton = view.findViewById(R.id.anadir);

        // Initialize data and adapter
        categoriasList = new ArrayList<>();
        filteredCategoriasList = new ArrayList<>();
        categoriasAdapter = new CategoriasAdapter(context, filteredCategoriasList, categoria -> {
            // Abrir fragmento para modificar la categoría seleccionada
            ModificarCategoria fragment = ModificarCategoria.newInstance(categoria);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
        recyclerView.setAdapter(categoriasAdapter);

        // Initialize API service
        apiService = ApiClient.getClient().create(ApiService.class);

        // Load categories for the user
        loadCategorias();

        EditText editText = buscarCategoria.getEditText();
        if (editText != null) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    filterCategorias(s.toString());
                }
            });
        }

        // Setup add category button
        anadirButton.setOnClickListener(v -> showNewCategory());

        return view;
    }

    private void showNewCategory() {
        Intent intent = new Intent (getContext(), NewCategory.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

    private void loadCategorias() {
        Call<List<Categoria>> call = apiService.getCategorias(idUsuario);
        call.enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoriasList.clear();
                    categoriasList.addAll(response.body());
                    filteredCategoriasList.clear();
                    filteredCategoriasList.addAll(categoriasList);
                    categoriasAdapter.notifyDataSetChanged();
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

    private void filterCategorias(String query) {
        filteredCategoriasList.clear();
        if (query.isEmpty()) {
            filteredCategoriasList.addAll(categoriasList);
        } else {
            String queryLower = query.toLowerCase();
            for (Categoria categoria : categoriasList) {
                if (categoria.getNombre() != null && categoria.getNombre().toLowerCase().contains(queryLower)) {
                    filteredCategoriasList.add(categoria);
                }
            }
        }
        categoriasAdapter.notifyDataSetChanged();
    }


    private void saveCategoria(Categoria categoria) {
        Call<Categoria> call = apiService.createCategoria(categoria);
        call.enqueue(new Callback<Categoria>() {
            @Override
            public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoriasList.add(response.body());
                    filteredCategoriasList.add(response.body());
                    categoriasAdapter.notifyItemInserted(filteredCategoriasList.size() - 1);
                    recyclerView.scrollToPosition(filteredCategoriasList.size() - 1);
                    Toast.makeText(context, "Categoría añadida", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error al añadir la categoría", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Categoria> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}