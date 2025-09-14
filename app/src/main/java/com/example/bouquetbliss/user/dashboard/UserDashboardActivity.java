package com.example.bouquetbliss.user.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.common.auth.LoginActivity;
import com.example.bouquetbliss.databinding.ActivityUserDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class UserDashboardActivity extends AppCompatActivity {

    private ActivityUserDashboardBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.cardBrowseBouquets.setOnClickListener(v -> {
            Toast.makeText(this, "Modul Penyediaan dan Permintaan Stok akan dilaksanakan tidak lama lagi", Toast.LENGTH_SHORT).show();
        });

        binding.cardMyOrders.setOnClickListener(v -> {
            Toast.makeText(this, "Modul Tempahan Saya akan dilaksanakan tidak lama lagi", Toast.LENGTH_SHORT).show();
        });

        binding.cardOrderHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Modul Sejarah Tempahan akan dilaksanakan tidak lama lagi", Toast.LENGTH_SHORT).show();
        });

        binding.cardProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Modul Profil Pengguna akan dilaksanakan tidak lama lagi", Toast.LENGTH_SHORT).show();
        });

        binding.btnLogout.setOnClickListener(v -> performLogout());
    }

    private void performLogout() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Berjaya log keluar", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
