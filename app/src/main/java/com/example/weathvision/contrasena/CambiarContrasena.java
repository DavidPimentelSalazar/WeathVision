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

        /**
         * Recogemos la clave generada en el anterior Fragmento y el correo al que se le envió
         * **/
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int nuevaClave = sharedPreferences.getInt("nuevaClave", -1);
        String destinatario = sharedPreferences.getString("destinatario", "");

        codigoIntroducido = view.findViewById(R.id.codigo_verificacion);
        nuevaContrasena = view.findViewById(R.id.nueva_contrasena);
        confirmacionContrasena = view.findViewById(R.id.confirmar_contrasena);
        textCodigo = view.findViewById(R.id.text_codigo);
        textVerificacion = view.findViewById(R.id.text_verificacion);


        button = view.findViewById(R.id.boton_verificar);

        /**
         * Funcionalidad al botón para la verificación de la clave y cambio de contraseña
         * **/
        button.setOnClickListener(v -> {
            String textoCodigo = codigoIntroducido.getText().toString().trim();
            cambiarContrasena(nuevaClave, textoCodigo, destinatario);
        });

        return view;
    }



    /**
     * Este Método verifica la clave mandada al correo y si es igual a la que introduce el usuario,
     * se mostraran dos edittext para hacer una modificacion de contraseña para su cuenta.
     * **/
    private void cambiarContrasena(int nuevaClave, String textoCodigo, String destinatario) {

        /**
         * Verificamos que el edittext donde se introduce el código no este vacío, si lo está,
         * se mostrará un mensaje
         * **/
        if (textoCodigo.isEmpty()) {
            codigoIntroducido.setError("Introduce el código de verificación.");
            return;
        }


        try {

            /**
             *  recogemos el codigo introducido por el usuario en el edittext
             * **/
            int codigoUsuario = Integer.parseInt(textoCodigo);


            /**
             * Si el código introducido por el usuario y la clave son iguales
             * **/
            if (codigoUsuario == nuevaClave) {


                /**
                 * se mostrarán los edittext de cambio de contraseña
                 * **/
                textVerificacion.setText("Introduce tu nueva contraseña para tu cuenta");
                textCodigo.setText("Verificación de código exitoso");
                codigoIntroducido.setEnabled(false);
                nuevaContrasena.setVisibility(VISIBLE);
                confirmacionContrasena.setVisibility(VISIBLE);


                /**
                 * Funcionalidad del botón para cambiar la contraseña
                 * **/
                button.setOnClickListener( v -> contrasenaCambiada(destinatario));


            } else {
                codigoIntroducido.setError("El código de verificación es incorrecto.");
            }

        } catch (NumberFormatException e) {
            codigoIntroducido.setError("Introduce un código válido de 6 dígitos.");
        }
    }


    /**
     * Método el cual recoge la nueva contraseña introducida por el usuario y la remplaza por la que ya estaba
     * en la base de datos.
     * **/
    private void contrasenaCambiada(String destinatario) {


        /**
         *  Recogemos los datos (la contraseña y la confirmación de contraseña)
         * **/
        String nuevaPass = nuevaContrasena.getText().toString().trim();
        String confirmPass = confirmacionContrasena.getText().toString().trim();


        /**
         * si la confirmación de la contraseña es valida realizará el contenido, si no, mandará un mensaje
         * al usuario de que las contraseñas introducidas no son iguales
         * **/
        if (nuevaPass.equals(confirmPass)) {


            /**
             * Llamamos a la API
             * **/
            ApiService apiService = ApiClient.getClient().create(ApiService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {


                /**
                 * Decodificamos el correo porque daba error en la API al pasarlo, el @ salia como %40% y
                 * no lo comparaba bien, en este caso nos aseguramos el buen funcionamiento
                 * **/
                String correoDecodificado = URLDecoder.decode(destinatario, StandardCharsets.UTF_8);


                /**
                 * Crear el map con el nombre correcto para que se pueda hashear correctamente si no daba error
                 * **/
                Map<String, String> body = new HashMap<>();
                body.put("contraseña_hash", nuevaPass);

                Call<UsuarioResponse> call = apiService.actualizarContrasena(correoDecodificado, body);
                call.enqueue(new Callback<UsuarioResponse>() {
                    @Override
                    public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                        if (response.isSuccessful()) {


                            /**
                             * Si la modificación es exitosa, redirigirá al usuario al login
                             * **/
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
