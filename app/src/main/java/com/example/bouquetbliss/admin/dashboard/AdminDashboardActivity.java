package com.example.bouquetbliss.admin.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.bouquetbliss.admin.base.BaseAdminNavigationActivity;
import com.example.bouquetbliss.admin.stock.StockManagementActivity;
import com.example.bouquetbliss.databinding.ActivityAdminDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends BaseAdminNavigationActivity {

    private ActivityAdminDashboardBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        setupClickListeners();
        setupAdminBottomNavigation();
        setSelectedNavItem("home");
    }

    private void setupClickListeners() {
        // Dashboard Cards
        binding.cardManageStock.setOnClickListener(v -> {
            Intent intent = new Intent(this, StockManagementActivity.class);
            startActivity(intent);
        });

        binding.cardManageOrders.setOnClickListener(v -> {
            Toast.makeText(this, "Order Management module coming soon!", Toast.LENGTH_SHORT).show();
        });

        binding.cardManageUsers.setOnClickListener(v -> {
            Toast.makeText(this, "User Management module coming soon!", Toast.LENGTH_SHORT).show();
        });

        binding.cardReports.setOnClickListener(v -> {
            Toast.makeText(this, "Reports module coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation is handled by base class
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
