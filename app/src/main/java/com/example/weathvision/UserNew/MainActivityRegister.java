package com.example.weathvision.UserNew;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.example.weathvision.UserNew.StartFragment;
import com.example.weathvision.R;

public class MainActivityRegister extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_register);

        // Load StartFragment into the fragment container
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainRegister, new StartFragment())
                    .commit();
        }
    }


}