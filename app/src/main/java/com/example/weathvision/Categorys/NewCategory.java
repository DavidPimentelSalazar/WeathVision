package com.example.weathvision.Categorys;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewCategory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button button;
    private CategoriasAdapter adapter;
    private List<Categoria> categoriaList;
    private int idUsuario;
    private ApiService apiService;
    private EditText nombre;
    private Categoria categoriaSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_category);


        /***
         *
         * Inicializamos las variables y el SharedPreferences para obtener el id del usuario que inicio
         *
         * */
        recyclerView = findViewById(R.id.recyclerNewCategorias);
        button = findViewById(R.id.appCompatButton);
        apiService = ApiClient.getClient().create(ApiService.class);
        nombre = findViewById(R.id.nombreIngresado);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        idUsuario = sharedPreferences.getInt("id_usuario", -1);
        if (idUsuario == -1) {
            finish();
            return;
        }


        /***
         *
         * Inicializamos la lista de categorias y el adaptador
         *
         * */
        categoriaList = new ArrayList<>();
        adapter = new CategoriasAdapter(this, categoriaList, new CategoriasAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Categoria categoria) {
                // Marcar la categoría seleccionada
                categoriaSeleccionada = categoria;
                Toast.makeText(NewCategory.this, "Categoría seleccionada: " + categoria.getNombre(), Toast.LENGTH_SHORT).show();
            }
        });

        /***
         *
         * Damos al recyclerView un Layout para que se muestren en columnas de 3
         *
         * */

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);

        /***
         *
         * iniciamos el metodo de cargar las imagenes de las categorias
         *
         * */

        CargarCategorias();

        /***
         *
         * Damos al boton la funcionalidad de onClick para que se guarde en este caso
         * la categoria personalizada por el usuario.
         *
         * */

        button.setOnClickListener(v -> {
            if (categoriaSeleccionada != null) {
                String nombreUsuario = nombre.getText().toString().trim();

                if (nombreUsuario.isEmpty()) {
                    Toast.makeText(NewCategory.this, "Ingresa un nombre para la categoría.", Toast.LENGTH_SHORT).show();
                    return;
                }

                categoriaSeleccionada.setNombre(nombreUsuario);

                saveCategoria(categoriaSeleccionada);
            } else {
                Toast.makeText(NewCategory.this, "Por favor, selecciona una categoría antes de guardar.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void CargarCategorias() {

        /***
         *
         * Limpiamos la lista de categorias
         *
         * */

        categoriaList.clear();

        /***
         *
         * Cargamos el array de categorias y lo guardamos en la lista
         *
         * */
        String[] categoryArray = getResources().getStringArray(R.array.categorias_array);

        /***
         *
         * Convertimos la lista de array en objetos, en este caso en categorias para que salga
         * con su item en el recyclerview
         *
         * */
        for (int i = 0; i < categoryArray.length; i++) {
            Categoria categoria = new Categoria();
            categoria.setIdCategoria(i + 1); // ID incremental
            String drawableName = categoryArray[i]; // Ejemplo: "@drawable/salud.png"
            categoria.setImagen(drawableName); // Guardar "@drawable/salud.png"
            categoria.setIdUsuario(idUsuario);

            categoriaList.add(categoria);
        }

        /***
         *
         * Notificamos al adaptador los cambios
         *
         * */
        adapter.notifyDataSetChanged();
    }


    /***
     *
     * Metodo para guardar en la base de datos la nueva categoria creada por el usuario,
     * en este caso se guardara como nombre el que introduce el usuario por el EditText,
     * y la imagen seleccionada de las categorias cargadas en el recyclerView.
     *
     * */
    private void saveCategoria(Categoria categoria) {

        Call<Categoria> call = apiService.createCategoria(categoria);
        call.enqueue(new Callback<Categoria>() {
            @Override
            public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Categoria savedCategoria = response.body();
                    Toast.makeText(NewCategory.this, "Categoría guardada: " + savedCategoria.getNombre(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NewCategory.this, "Error al guardar la categoría", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Categoria> call, Throwable t) {
                Toast.makeText(NewCategory.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
