package com.example.weathvision.contrasena;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.UsuarioResponse;
import com.example.weathvision.LoginActivity;
import com.example.weathvision.R;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CambiarContrasena extends Fragment {
    private EditText codigoIntroducido, nuevaContrasena, confirmacionContrasena;
    private TextView textCodigo, textVerificacion;
    private Button button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cambiar_contrasena, container, false);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int nuevaClave = sharedPreferences.getInt("nuevaClave", -1);
        String destinatario = sharedPreferences.getString("destinatario", "");

        codigoIntroducido = view.findViewById(R.id.codigo_verificacion);
        nuevaContrasena = view.findViewById(R.id.nueva_contrasena);
        confirmacionContrasena = view.findViewById(R.id.confirmar_contrasena);
        textCodigo = view.findViewById(R.id.text_codigo);
        textVerificacion = view.findViewById(R.id.text_verificacion);


        button = view.findViewById(R.id.boton_verificar);

        button.setOnClickListener(v -> {
            String textoCodigo = codigoIntroducido.getText().toString().trim();
            cambiarContrasena(nuevaClave, textoCodigo, destinatario);
        });

        return view;
    }

    private void cambiarContrasena(int nuevaClave, String textoCodigo, String destinatario) {
        if (textoCodigo.isEmpty()) {
            codigoIntroducido.setError("Introduce el código de verificación.");
            return;
        }

        try {
            int codigoUsuario = Integer.parseInt(textoCodigo);

            if (codigoUsuario == nuevaClave) {


                textVerificacion.setText("Introduce tu nueva contraseña para tu cuenta");
                textCodigo.setText("Verificación de código exitoso");
                codigoIntroducido.setEnabled(false);
                nuevaContrasena.setVisibility(VISIBLE);
                confirmacionContrasena.setVisibility(VISIBLE);


                button.setOnClickListener( v -> contrasenaCambiada(destinatario));


            } else {
                codigoIntroducido.setError("El código de verificación es incorrecto.");
            }

        } catch (NumberFormatException e) {
            codigoIntroducido.setError("Introduce un código válido de 6 dígitos.");
        }
    }

    private void contrasenaCambiada(String destinatario) {
        String nuevaPass = nuevaContrasena.getText().toString().trim();
        String confirmPass = confirmacionContrasena.getText().toString().trim();

        if (nuevaPass.equals(confirmPass)) {
            ApiService apiService = ApiClient.getClient().create(ApiService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                String correoDecodificado = URLDecoder.decode(destinatario, StandardCharsets.UTF_8);

                // Crear el cuerpo de la solicitud con el nombre correcto
                Map<String, String> body = new HashMap<>();
                body.put("contraseña_hash", nuevaPass); // Use "contraseña_hash" instead of "contraseña"
                Log.d("API_REQUEST", "Cuerpo enviado: " + body.toString()); // Depurar el cuerpo

                Call<UsuarioResponse> call = apiService.actualizarContrasena(correoDecodificado, body);
                call.enqueue(new Callback<UsuarioResponse>() {
                    @Override
                    public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                        if (response.isSuccessful()) {

                            Toast.makeText(getContext(), "Contraseña Actualizada", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getContext(), LoginActivity.class);
                            startActivity(intent);

                        } else {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e("API_ERROR", errorBody); // Depurar el error
                                confirmacionContrasena.setError("Error al actualizar contraseña: " + errorBody);
                            } catch (IOException e) {
                                confirmacionContrasena.setError("Error al actualizar contraseña: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            confirmacionContrasena.setError("Las contraseñas no coinciden");
        }
    }


}
