package com.example.weathvision.contrasena;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.weathvision.R;

public class MainContrasena extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_contrasena);

        ContrasenaOlvidada contrasenaOlvidada = new ContrasenaOlvidada();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout_contrasena, contrasenaOlvidada)
                .commit();


    }
}