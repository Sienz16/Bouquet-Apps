package com.example.bouquetbliss.admin.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.bouquetbliss.admin.base.BaseAdminNavigationActivity;
import com.example.bouquetbliss.admin.stock.StockManagementActivity;
import com.example.bouquetbliss.databinding.ActivityAdminDashboardBinding;
import com.example.bouquetbliss.models.Bouquet;
import com.example.bouquetbliss.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminDashboardActivity extends BaseAdminNavigationActivity {

    private ActivityAdminDashboardBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupClickListeners();
        setupAdminBottomNavigation();
        setSelectedNavItem("home");
        
        // Load real dashboard statistics
        loadDashboardStatistics();
    }

    private void setupClickListeners() {
        // Dashboard Cards
        binding.cardManageStock.setOnClickListener(v -> {
            Intent intent = new Intent(this, StockManagementActivity.class);
            startActivity(intent);
        });

        binding.cardManageOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.bouquetbliss.admin.orders.AdminOrderManagementActivity.class);
            startActivity(intent);
        });

        binding.cardViewRatings.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.bouquetbliss.admin.ratings.AdminRatingsActivity.class);
            startActivity(intent);
        });

        binding.cardReports.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.bouquetbliss.admin.reports.ReportsActivity.class);
            startActivity(intent);
        });

        // Bottom Navigation is handled by base class
    }

    private void loadDashboardStatistics() {
        // Show loading state
        showLoadingState();
        
        // Load orders statistics
        loadOrderStatistics();
    }

    private void showLoadingState() {
        // Show placeholder values while loading
        if (binding.tvTotalOrders != null) binding.tvTotalOrders.setText("...");
        if (binding.tvPendingOrders != null) binding.tvPendingOrders.setText("...");
        if (binding.tvTotalRevenue != null) binding.tvTotalRevenue.setText("...");
    }

    private void loadOrderStatistics() {
        db.collection("orders")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int totalOrders = 0;
                int pendingOrders = 0;
                double totalRevenue = 0.0;
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Order order = document.toObject(Order.class);
                    totalOrders++;
                    
                    if (order.getStatus().equals(Order.Status.PENDING) ||
                        order.getStatus().equals(Order.Status.APPROVED) ||
                        order.getStatus().equals(Order.Status.IN_PREPARATION) ||
                        order.getStatus().equals(Order.Status.READY)) {
                        pendingOrders++;
                    }
                    
                    if (order.getStatus().equals(Order.Status.COMPLETED)) {
                        totalRevenue += order.getTotalAmount();
                    }
                }
                
                // Update UI with real data
                if (binding.tvTotalOrders != null) {
                    binding.tvTotalOrders.setText(String.valueOf(totalOrders));
                }
                if (binding.tvPendingOrders != null) {
                    binding.tvPendingOrders.setText(String.valueOf(pendingOrders));
                }
                if (binding.tvTotalRevenue != null) {
                    binding.tvTotalRevenue.setText(String.format("RM %.0f", totalRevenue));
                }
            })
            .addOnFailureListener(e -> {
                // Handle error gracefully
                if (binding.tvTotalOrders != null) binding.tvTotalOrders.setText("0");
                if (binding.tvPendingOrders != null) binding.tvPendingOrders.setText("0");
                if (binding.tvTotalRevenue != null) binding.tvTotalRevenue.setText("RM 0");
            });
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadDashboardStatistics();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
