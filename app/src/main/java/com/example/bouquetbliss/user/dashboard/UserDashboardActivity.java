package com.example.bouquetbliss.user.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.common.auth.LoginActivity;
import com.example.bouquetbliss.databinding.ActivityUserDashboardBinding;
import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.user.base.BaseUserNavigationActivity;
import com.example.bouquetbliss.user.browse.BrowseBouquetsActivity;
import com.example.bouquetbliss.user.orders.UserOrdersActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class UserDashboardActivity extends BaseUserNavigationActivity {

    private ActivityUserDashboardBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupClickListeners();
        setupUserBottomNavigation();
        setSelectedNavItem("home");
        
        // Apply translations
        applyTranslations();
        
        // Load real user statistics
        loadUserStatistics();
    }

    private void setupClickListeners() {
        binding.cardBrowseBouquets.setOnClickListener(v -> {
            Intent intent = new Intent(this, BrowseBouquetsActivity.class);
            startActivity(intent);
        });

        binding.cardMyOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserOrdersActivity.class);
            startActivity(intent);
        });

        binding.cardOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.bouquetbliss.user.orders.UserOrderHistoryActivity.class);
            startActivity(intent);
        });

        binding.cardProfile.setOnClickListener(v -> {
            Toast.makeText(this, "User Profile module coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        // Bottom Navigation is handled by base class
    }

    private void loadUserStatistics() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not logged in, show zero values
            updateUIWithValues(0, 0, 0.0);
            return;
        }

        // Show loading state
        showLoadingState();

        // Load user's order statistics
        db.collection("orders")
            .whereEqualTo("customerId", currentUser.getUid())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int totalOrders = 0;
                int pendingOrders = 0;
                double totalSpent = 0.0;
                
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
                        totalSpent += order.getTotalAmount();
                    }
                }
                
                // Update UI with real data
                updateUIWithValues(totalOrders, pendingOrders, totalSpent);
            })
            .addOnFailureListener(e -> {
                // Handle error gracefully
                updateUIWithValues(0, 0, 0.0);
            });
    }

    private void showLoadingState() {
        // Show placeholder values while loading
        if (binding.tvTotalOrders != null) binding.tvTotalOrders.setText("...");
        if (binding.tvPendingOrders != null) binding.tvPendingOrders.setText("...");
        if (binding.tvTotalSpent != null) binding.tvTotalSpent.setText("...");
    }

    private void updateUIWithValues(int totalOrders, int pendingOrders, double totalSpent) {
        if (binding.tvTotalOrders != null) {
            binding.tvTotalOrders.setText(String.valueOf(totalOrders));
        }
        if (binding.tvPendingOrders != null) {
            binding.tvPendingOrders.setText(String.valueOf(pendingOrders));
        }
        if (binding.tvTotalSpent != null) {
            binding.tvTotalSpent.setText(String.format("RM %.0f", totalSpent));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadUserStatistics();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
