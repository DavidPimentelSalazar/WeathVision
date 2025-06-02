package com.example.weathvision;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class IndexActivity extends AppCompatActivity {

    private Button button, github, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_index);

        email = findViewById(R.id.enviar_email);
        github = findViewById(R.id.github);
        button = findViewById(R.id.get_started);
        button.setOnClickListener(v -> enviarLogin());
        github.setOnClickListener(v -> enviarActividad("https://github.com/DavidPimentelSalazar/WeathVision/blob/main/README.md"));
        email.setOnClickListener(v -> enviarEmail());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    /**
     * Metodo para enviar un correo a (wealthvisionoficial@gmail.com);
     **/
    private void enviarEmail() {

        String correo = "wealthvisionoficial@gmail.com";
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{correo});
        intent.setType("message/rfc822");

        startActivity(Intent.createChooser(intent, "Elije un cliente de correo:"));


    }

    /**
     * Metodo donde pasandole una Url nos rederigirá a ella, en este caso a mi repositorio de github - readme
     **/
    public void enviarActividad(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * Método para empezar la actividad y nos redirigirá a la actividad del Login.
     **/
    private void enviarLogin() {

        Intent intent = new Intent(IndexActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }
}