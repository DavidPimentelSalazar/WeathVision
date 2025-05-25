package com.example.weathvision;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.LoginResponse;
import com.example.weathvision.Api.Class.UsuarioLoginRequest;
import com.example.weathvision.UserNew.NameAlertFragment;
import com.example.weathvision.UserNew.StartFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText user, password;
    private TextView register;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar los elementos de la UI
        user = findViewById(R.id.userEditText); // Asegúrate de que el ID coincida con tu layout
        password = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.button);
        register = findViewById(R.id.register);

        register.setOnClickListener( v -> registerUser());

        // Configurar el listener del botón
        loginButton.setOnClickListener( v -> loginUser());

    }

    private void registerUser() {

        Intent intent = new Intent(LoginActivity.this, Register.class);
        startActivity(intent);
    }

    private void loginUser() {
        // Obtener los valores de los campos
        String textUser = user.getText().toString().trim();
        String textPass = password.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (textUser.isEmpty() || textPass.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Crear el objeto de solicitud con los datos del usuario
            UsuarioLoginRequest usuarioLoginRequest = new UsuarioLoginRequest(textUser, textPass);

            // Inicializar el servicio API usando ApiClient
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<LoginResponse> call = apiService.postUsuariosLogin(usuarioLoginRequest);

            // Ejecutar la solicitud de manera asíncrona
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Login exitoso
                        LoginResponse loginResponse = response.body();
                        int idUsuario = loginResponse.getIdUsuario();

                        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("id_usuario", idUsuario);
                        editor.apply();

                        Toast.makeText(LoginActivity.this,
                                "¡Bienvenido!",
                                Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(LoginActivity.this, BarActivity.class);
                        startActivity(intent);

                    } else {
                        // Error en la respuesta (e.g., 401 Unauthorized)
                        Toast.makeText(LoginActivity.this,
                                "Usuario o contraseña incorrectos" ,
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    // Error de conexión o red
                    Toast.makeText(LoginActivity.this,
                            "Fallo en la conexión: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(LoginActivity.this,
                    "Error inesperado: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }
    }
}