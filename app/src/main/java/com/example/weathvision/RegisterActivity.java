package com.example.weathvision;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {


    private TextInputEditText name, user, email, password, verificationPassword;
    private Button button;
    private Boolean usuarioRegistrado, correoRegistrado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);


        /**
         *
         * Flags para saber si hay un usuario o correo registrado ya y que no se puedan crear
         * más de una cuenta con un mismo usuario o correo.
         *
         * **/
        usuarioRegistrado = false;
        correoRegistrado = false;


        /**
         * Inicializamos todos los textViews y el botón de registro
         * **/
        name = findViewById(R.id.name);
        user = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        verificationPassword = findViewById(R.id.verificationPassword);
        button = findViewById(R.id.button);


        /**
         * Funcionalidad al botón, en este caso, para registrar los datos introducidos
         * en los textViews
         * **/
        button.setOnClickListener(v -> register());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    /**
     * Método para registrar al usuario en la base de datos
     * **/
    private void register() {

        /**
         * Recogemos todos los datos del usuario que introdució en los editTexts
         * **/
        String nameText = name.getText().toString().trim();
        String usernameText = user.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String verificationPasswordText = verificationPassword.getText().toString().trim();

        /**
         * Verificamos que el usuario introduce todos los datos y no hay ningún campo vacío
         * **/
        if (nameText.isEmpty() || usernameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty() || verificationPasswordText.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        /**
         * Verifica que lo que introduce el usuario es de tipo correo
         * **/
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.setError("Por favor, ingresa un correo válido");
            return;
        }


        /**
         * Verifica la confirmación de la contraseña
         * **/
        if (!passwordText.equals(verificationPasswordText)) {
            verificationPassword.setError("Las contraseñas no coinciden");
            return;
        }


        /**
         * Reseteamos las flags
         * **/
        usuarioRegistrado = false;
        correoRegistrado = false;


        /**
         * Llamamos al método de verificación de usuario y correo, pasandole
         * los datos del usuario
         * **/
        verificarUsuarioyCorreo(usernameText, emailText);
    }



    /**
     * Metodo para verificar en la base de datos que los datos del usuario no esten ya
     * introducidos (nombre del usuario y correo), con esto conseguimos que no haya ningún
     * tipo de error en tener dos usuarios con nombres iguales igual que con los correos
     * **/
    private void verificarUsuarioyCorreo(String usernameText, String emailText) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        // Check username
        Call<List<String>> usernameCall = apiService.obtenerUsername();
        usernameCall.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> userNames = response.body();
                    for (String username : userNames) {
                        if (username.equals(usernameText)) {
                            user.setError("Este usuario ya está registrado");
                            usuarioRegistrado = true;
                            break;
                        }
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Error al verificar usuario: " + response.message(), Toast.LENGTH_SHORT).show();
                    usuarioRegistrado = true;
                }

                /**
                 * Verificar el correo despues del usuario
                 * **/
                verificarCorreo(emailText);
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Fallo al verificar usuario: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                usuarioRegistrado = true; // Prevent registration on failure
                verificarCorreo(emailText);
            }
        });
    }

    private void verificarCorreo(String emailText) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<String>> emailCall = apiService.obtenerCorreos();
        emailCall.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> correosActivos = response.body();
                    for (String correo : correosActivos) {
                        if (correo.equals(emailText)) {
                            email.setError("Este correo ya está registrado");
                            correoRegistrado = true;
                            break;
                        }
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Error al verificar correo: " + response.message(), Toast.LENGTH_SHORT).show();
                    correoRegistrado = true;
                }

                /**
                 * Despues de las dos verificaciones y que todo este correcto para el registro, procedemos
                 * a llamar al metodo
                 * **/
                RegistrarUsuario();
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Fallo al verificar correo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                correoRegistrado = true; // Prevent registration on failure
                RegistrarUsuario();
            }
        });
    }

    private void RegistrarUsuario() {

        /**
         * Nos aseguramos que las Flags esten correctamente, en este caso si estuviesen en [true],
         * que determina que no se puede registrar el usuario entraría en el if y saldría del metodo
         * **/
        if (usuarioRegistrado || correoRegistrado) {
            return;
        }

        String nameText = name.getText().toString().trim();
        String usernameText = user.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        try {
            /**
             * Generamos la fecha y hora de registro
             * **/
            String fecha = new SimpleDateFormat("yyyy-MM-dd' - 'HH:mm:ss", Locale.getDefault()).format(new Date());

            /**
             * Creamos el objeto del usuario con sus datos
             *  nameText        =  Nombre del usuario
             *  usernameText    =  Nick del usuario para el inicio de sesión
             *  emailText       =  Correo
             *  passwordText    =  Contraseña
             *  null            =  Categoría, [Se elije en el siguiente fragmento por eso lo pongo en null]
             *  Fecha           =  Fecha de registro
             *
             * **/
            UsuarioRegisterRequest request = new UsuarioRegisterRequest(
                    nameText,
                    usernameText,
                    emailText,
                    passwordText,
                    null,
                    fecha
            );

            /**
             * Inicializamos la API para el registro
             * **/
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<UsuarioResponse> call = apiService.postUsuariosRegister(request);

            call.enqueue(new Callback<UsuarioResponse>() {
                @Override
                public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UsuarioResponse usuarioResponse = response.body();

                        /**
                         * Guardamos el id del usuario en shared preferences para que proximamente guarde
                         * sus movimientos en la base de datos
                         * **/
                        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("id_usuario", usuarioResponse.getIdUsuario());
                        editor.apply();

                        /**
                         * Cambiamos a otro activity
                         * **/
                        Intent intent = new Intent(RegisterActivity.this, MainActivityRegister.class);
                        startActivity(intent);
                        finish(); // Close RegisterActivity
                    } else {
                        String errorMessage = "Error: " + response.code() + " - " + response.message();
                        try {
                            if (response.errorBody() != null) {
                                errorMessage += "\nDetalles: " + response.errorBody().string();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this,
                            "Fallo en la conexión: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}