package com.example.weathvision;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.UsuarioRegisterRequest;
import com.example.weathvision.Api.Class.UsuarioResponse;
import com.example.weathvision.UserNew.MainActivityRegister;
import com.example.weathvision.UserNew.StartFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register extends AppCompatActivity {

    private EditText user, email, password, verificationPassword;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        user = findViewById(R.id.user);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        verificationPassword = findViewById(R.id.verificationPassword);
        button = findViewById(R.id.button);

        button.setOnClickListener(v -> register());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void register() {
        String usernameText = user.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String verificationPasswordText = verificationPassword.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (usernameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty() || verificationPasswordText.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato de correo
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.setError("Por favor, ingresa un correo válido");
            return;
        }

        // Validar que las contraseñas coincidan
        if (!passwordText.equals(verificationPasswordText)) {
            verificationPassword.setError("Las contraseñas no coinciden");
            return;
        }

        try {
            // Generar la fecha actual en formato ISO 8601
            String createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());

            // Crear el objeto de solicitud
            UsuarioRegisterRequest request = new UsuarioRegisterRequest(
                    usernameText, // nombre_usuario
                    usernameText, // nombre (puedes usar un campo separado si tienes uno)
                    emailText,    // correo
                    passwordText, // contraseña
                    null,         // categoria_trabajo (opcional)
                    createdAt     // creado_en
            );

            // Inicializar el servicio API
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<UsuarioResponse> call = apiService.postUsuariosRegister(request);

            // Ejecutar la solicitud de manera asíncrona
            call.enqueue(new Callback<UsuarioResponse>() {
                @Override
                public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UsuarioResponse usuarioResponse = response.body();
                        Toast.makeText(Register.this,
                                "Registro exitoso: " + usuarioResponse.getNombreUsuario() + ", ID: " + usuarioResponse.getIdUsuario(),
                                Toast.LENGTH_LONG).show();

                        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("id_usuario", usuarioResponse.getIdUsuario());
                        editor.apply();

                        Intent intent = new Intent(Register.this, MainActivityRegister.class);
                        startActivity(intent);

                    } else {
                        // Mostrar detalles del error
                        String errorMessage = "Error: " + response.code() + " - " + response.message();
                        try {
                            if (response.errorBody() != null) {
                                errorMessage += "\nDetalles: " + response.errorBody().string();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(Register.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                    Toast.makeText(Register.this,
                            "Fallo en la conexión: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}