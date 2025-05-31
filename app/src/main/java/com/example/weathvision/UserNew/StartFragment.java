package com.example.weathvision.UserNew;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.CategoriaUpdate;
import com.example.weathvision.Api.Class.UsuarioResponse;
import com.example.weathvision.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StartFragment extends Fragment {

    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment's layout
        View view = inflater.inflate(R.layout.fragment_start, container, false);


        // Initialize categories
        int[] imagenesCategorias = {
                R.drawable.student_837825,
                R.drawable.freelancer_4299312,
                R.drawable.punctual_9197504,
                R.drawable.time_15505082,
                R.drawable.power_5156964,
                R.drawable.bag_8775037,
                R.drawable.power_5156964,
                R.drawable.follower_7548275,
                R.drawable.network_3815917,
                R.drawable.old_people,
                R.drawable.profit_1390493,
                R.drawable.pencil_10419299,
                R.drawable.student_837825,
                R.drawable.freelancer_4299312,
                R.drawable.punctual_9197504,
                R.drawable.time_15505082,
                R.drawable.power_5156964,
                R.drawable.bag_8775037,
                R.drawable.power_5156964,
                R.drawable.follower_7548275,
                R.drawable.network_3815917,
                R.drawable.old_people,
                R.drawable.profit_1390493,
                R.drawable.pencil_10419299
        };

        String[] categorias = getResources().getStringArray(R.array.categorias_trabajo);
        GridLayout gridLayout = view.findViewById(R.id.categories);

        for (int i = 0; i < categorias.length; i++) {
            View categoryView = inflater.inflate(R.layout.categorias_layout, gridLayout, false);

            TextView textView = categoryView.findViewById(R.id.category_name);
            ImageView imageView = categoryView.findViewById(R.id.image_category);
            imageView.setMaxWidth(10);

            textView.setText(categorias[i]);
            imageView.setImageResource(imagenesCategorias[i]);

            // Add click listener for each category
            categoryView.setOnClickListener(v -> {
                String categoriaSeleccionada = textView.getText().toString();
                seleccionCategoria(requireContext(), categoriaSeleccionada);
            });

            gridLayout.addView(categoryView);
        }

        return view;
    }

    private void seleccionCategoria(Context context, String categoriaSeleccionada) {
        // Save to SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("categoria_seleccionada", categoriaSeleccionada);
        editor.apply();

        // Get id_usuario from SharedPreferences
        int idUsuario = sharedPreferences.getInt("id_usuario", -1);
        if (idUsuario == -1) {
            Toast.makeText(context, "Error: No se encontró el ID del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configure Retrofit
        apiService = ApiClient.getClient().create(ApiService.class);

        // Create CategoriaUpdate object
        CategoriaUpdate categoriaUpdate = new CategoriaUpdate(categoriaSeleccionada);

        // Send PUT request
        Call<UsuarioResponse> call = apiService.actualizarCategoria(idUsuario, categoriaUpdate);
        call.enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                if (response.isSuccessful()) {
                    // Clear parent activity content and show NameAlertFragment
                    getParentFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                            .replace(R.id.mainRegister, new NameAlertFragment())
                            .addToBackStack(null) // Optional: Allows back navigation
                            .commit();
                } else {
                }
            }

            @Override
            public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                Toast.makeText(context, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}