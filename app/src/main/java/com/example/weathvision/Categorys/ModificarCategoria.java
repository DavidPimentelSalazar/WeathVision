package com.example.weathvision.Categorys;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.Categoria;
import com.example.weathvision.R;

import java.util.ArrayList;
import java.util.List;

import Adapters.CategoriasAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModificarCategoria extends Fragment {

    private static final String ARG_ID = "idCategoria";
    private static final String ARG_NOMBRE = "nombre";
    private static final String ARG_IMAGEN = "imagen";

    private RecyclerView recyclerView;
    private CategoriasAdapter adapter;
    private List<Categoria> categoriaList;
    private Categoria categoria;
    private int idUsuario;
    private String imagenSeleccionada;

    private Button modificar, eliminar;

    public static ModificarCategoria newInstance(Categoria categoria) {
        ModificarCategoria fragment = new ModificarCategoria();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, categoria.getIdCategoria());
        args.putString(ARG_NOMBRE, categoria.getNombre());
        args.putString(ARG_IMAGEN, categoria.getImagen());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.modificar_categoria, container, false);
        categoriaList = new ArrayList<>();

        EditText nombreEditText = view.findViewById(R.id.nombreIngresado);
        modificar = view.findViewById(R.id.modificar);
        eliminar = view.findViewById(R.id.eliminarCategoria);
        eliminar.setOnClickListener( v -> eliminarCategoria());
        recyclerView = view.findViewById(R.id.recyclerNewCategorias);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        idUsuario = sharedPreferences.getInt("id_usuario", -1);

        // Inicializar adaptador con listener
        adapter = new CategoriasAdapter(getContext(), categoriaList, categoriaSeleccionada -> {
            imagenSeleccionada = categoriaSeleccionada.getImagen();
            if (categoria != null) {
                categoria.setImagen(imagenSeleccionada);
            }
        });

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(adapter);

        CargarCategorias();

        if (getArguments() != null) {
            int id = getArguments().getInt(ARG_ID);
            String nombre = getArguments().getString(ARG_NOMBRE);
            String imagen = getArguments().getString(ARG_IMAGEN);
            categoria = new Categoria(id, nombre, imagen);
            nombreEditText.setText(nombre);
            imagenSeleccionada = imagen; // ← también inicializamos con la imagen actual
        }

        modificar.setOnClickListener(v -> {
            String nuevoNombre = nombreEditText.getText().toString().trim();
            if (nuevoNombre.isEmpty()) {
                Toast.makeText(getContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }
            categoria.setNombre(nuevoNombre);
            actualizarCategoria(categoria);
        });

        return view;
    }

    private void eliminarCategoria() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.deleteCategoria(categoria.getIdCategoria());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Categoría eliminada con éxito", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "Error al eliminar la categoría", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarCategoria(Categoria categoria) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Categoria> call = apiService.updateCategoria(categoria.getIdCategoria(), categoria);
        call.enqueue(new Callback<Categoria>() {
            @Override
            public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Categoría actualizada", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "Error al actualizar la categoría", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Categoria> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void CargarCategorias() {
        categoriaList.clear();
        String[] categoryArray = getResources().getStringArray(R.array.categorias_array);

        for (int i = 0; i < categoryArray.length; i++) {
            Categoria categoria = new Categoria();
            categoria.setIdCategoria(i + 1);
            categoria.setImagen(categoryArray[i]); // e.g., "@drawable/salud.png"
            categoria.setIdUsuario(idUsuario);
            categoriaList.add(categoria);
        }

        adapter.notifyDataSetChanged();
    }
}
