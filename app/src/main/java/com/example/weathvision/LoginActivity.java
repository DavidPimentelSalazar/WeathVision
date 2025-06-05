package com.example.weathvision;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.LoginResponse;
import com.example.weathvision.Api.Class.UsuarioLoginRequest;
import com.example.weathvision.contrasena.ContrasenaOlvidada;
import com.example.weathvision.contrasena.MainContrasena;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText user, password;
    private TextView register, contrasenaOlvidada;
    private Button loginButton;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /**
         * Inicializamos los elementos del Layout
         * **/
        user = findViewById(R.id.userEditText);
        password = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.button);
        register = findViewById(R.id.register);
        contrasenaOlvidada = findViewById(R.id.contrasenaOlvidada);
        progressBar = findViewById(R.id.progressBar);
        /**
         * Funcionalidad al textview para obtener una nueva contraseña
         * **/
        contrasenaOlvidada.setOnClickListener(v -> nuevaContrasena());

        /**
         * Funcionalidad por si el usuario no esta registrado
         * **/
        register.setOnClickListener(v -> registerUser());

        /**
         * Damos funcionalidad al botón de loguearse
         * **/
        loginButton.setOnClickListener(v -> loginUser(progressBar));

    }


    /**
     * Método donde nos llevará al activity de cambiar la contraseña
     **/
    private void nuevaContrasena() {
        progressBar.setVisibility(VISIBLE);
        Intent intent = new Intent(this, MainContrasena.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        progressBar.setVisibility(GONE);

    }

    /**
     * Método donde nos llevará al activity de registrarse
     **/
    private void registerUser() {
        progressBar.setVisibility(VISIBLE);
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        progressBar.setVisibility(GONE);

    }

    /**
     * Método que verifica en la base de datos usuario y contraseña introducido
     * por el usuario para loguearse
     **/
    private void loginUser(ProgressBar progressBar) {
        progressBar.setVisibility(VISIBLE);
        /**
         * Obtenemos los valores tanto del editext de nick del usuario y contraseña
         * **/
        String textUser = user.getText().toString().trim();
        String textPass = password.getText().toString().trim();

        /**
         * Validamos que los editext no esten vacíos
         * **/
        if (textUser.isEmpty() || textPass.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(GONE);

            return;
        }


        try {
            /**
             * Creamos el objeto con los datos introducidos por el usuario
             * **/
            UsuarioLoginRequest usuarioLoginRequest = new UsuarioLoginRequest(textUser, textPass);

            /**
             * Inicializamos la APi para hacer la llamada
             * **/
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<LoginResponse> call = apiService.postUsuariosLogin(usuarioLoginRequest);

            /**
             * Ejecutamos la solicitud a la API
             * **/
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        /**
                         * Si la API responde de manera exitosa, recogemos el id del usuario
                         * que nos servirá mas adelante para recoger todos los movimientos del mismo,
                         * como transacciones, metas, alertas ...
                         * **/
                        LoginResponse loginResponse = response.body();
                        int idUsuario = loginResponse.getIdUsuario();

                        /**
                         * Guardamos el id del usuario y nombre para usarlo mas adelante.
                         * **/
                        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("id_usuario", idUsuario);
                        editor.putString("nombre_usuario", textUser);
                        editor.apply();


                        Toast.makeText(LoginActivity.this,
                                "¡Bienvenido!",
                                Toast.LENGTH_LONG).show();


                        Intent intent = new Intent(LoginActivity.this, BarActivity.class);
                        startActivity(intent);
                        finish();
                        progressBar.setVisibility(GONE);


                    } else {
                        progressBar.setVisibility(GONE);
                        Toast.makeText(LoginActivity.this,
                                "Usuario o contraseña incorrectos",
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    progressBar.setVisibility(GONE);
                    Toast.makeText(LoginActivity.this,
                            "Fallo en la conexión: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(GONE);

            Toast.makeText(LoginActivity.this,
                    "Error inesperado: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }
    }
}