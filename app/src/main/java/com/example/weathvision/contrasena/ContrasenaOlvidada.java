package com.example.weathvision.contrasena;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Mail.MailSender;
import com.example.weathvision.R;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContrasenaOlvidada extends Fragment {

    private Button button;
    private EditText editText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contrasena_olvidada, container, false);

        button = view.findViewById(R.id.siguienteContrasenaOlvidada);
        editText = view.findViewById(R.id.correoContrasenaOlvidada);

        button.setOnClickListener( v -> nuevaContrasena());
        return  view;
    }


    private void nuevaContrasena() {
        int nuevaClave = (int) (Math.random() * 900000) + 100000;
        String destinatario = editText.getText().toString().trim();

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("nuevaClave", nuevaClave);
        editor.putString("destinatario", destinatario);
        editor.apply();

        if (destinatario.isEmpty()) {
            editText.setError("Introduce un correo");
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<String>> call = apiService.obtenerCorreos();

        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                List<String> correosActivos = response.body();

                for (String correo : correosActivos) {
                    if (correo.equals(destinatario)) {
                        String asunto = "Recuperación de contraseña - WeathVision";

                        // HTML-formatted email body
                        String mensaje = "<html>" +
                                "<body style='font-family: Arial, sans-serif; color: #333; margin: 0; padding: 0; background-color: #F5F5F5;'>" +
                                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #FFFFFF;'>" +
                                "<div style='margin-bottom: 20px;'>" +
                                "<h2 style='color: #800080; text-align: center;'>WealthVision</h2>" +
                                "</div>" +
                                "<p style='text-align: center;'>Hola:</p>" +
                                "<p style='text-align: center;'>Introduce este código para restablecer tu contraseña.</p>" +
                                "<h1 style='font-size: 36px; font-weight: bold; color: #800080; text-align: center;'>" + nuevaClave + "</h1>" +
                                "<p style='text-align: center;'>Por motivos de seguridad, no compartas este correo ni tu nueva contraseña con nadie. Te recomendamos:</p>" +
                                "<ul style='text-align: center; list-style-position: inside; padding: 0;'>" +
                                "<li>Cambiar tu contraseña lo antes posible desde la configuración de tu cuenta.</li>" +
                                "<li>Usar una contraseña segura y única que solo tú conozcas.</li>" +
                                "<li>No reenviar este correo ni guardarlo en lugares accesibles.</li>" +
                                "</ul>" +
                                "<p style='text-align: center;'>Si no solicitaste este cambio, contacta con nuestro equipo de soporte de inmediato.</p>" +
                                "<p style='margin-top: 20px; text-align: center;'>Atentamente,<br>El equipo de WealthVision<br>" +
                                "<a href='mailto:wealthvisionoficial@gmail.com' style='color: #800080; text-decoration: none;'>" +
                                "📧 wealthvisionoficial@gmail.com" +
                                "</a></p>" +
                                "<hr style='border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;'>" +
                                "<p style='text-align: center; font-size: 12px; color: #666;'>" +
                                "<a href='#' style='color: #800080; text-decoration: none; margin: 0 5px;'>Aviso Legal</a> | " +
                                "<a href='#' style='color: #800080; text-decoration: none; margin: 0 5px;'>Política de Privacidad</a> | " +
                                "<a href='#' style='color: #800080; text-decoration: none; margin: 0 5px;'>Soporte</a><br>" +
                                "© 2025 WealthVision. Todos los derechos reservados.<br>" +
                                "WealthVision, Málaga, España" +
                                "</p>" +
                                "</div>" +
                                "</body>" +
                                "</html>";

                        // Ejecutar en un hilo separado
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.execute(() -> {
                            MailSender mailSender = new MailSender();
                            mailSender.enviarCorreo(destinatario, asunto, mensaje);
                        });

                        CambiarContrasena nextFragment = new CambiarContrasena();
                        getParentFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                .replace(R.id.frame_layout_contrasena, nextFragment)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        editText.setError("El correo que introduciste no está registrado en WeathVision");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                // Handle failure (e.g., show a Toast message)
            }
        });
    }
}
