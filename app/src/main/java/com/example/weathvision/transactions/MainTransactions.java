package com.example.weathvision.transactions;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.weathvision.R;
import com.example.weathvision.transactions.NewAlert;

public class MainTransactions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_transactions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load NewTransaction Fragment by default if not restoring state
        if (savedInstanceState == null) {
            Fragment newTransactionFragment = new NewTransaction();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_transaction, newTransactionFragment);
            transaction.commit();
        }

        // Button click listeners
        findViewById(R.id.transaccion).setOnClickListener(v -> {
            Fragment newTransactionFragment = new NewTransaction();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_transaction, newTransactionFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        findViewById(R.id.alerta).setOnClickListener(v -> {
            Fragment newAlertFragment = new NewAlert();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_transaction, newAlertFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }
}