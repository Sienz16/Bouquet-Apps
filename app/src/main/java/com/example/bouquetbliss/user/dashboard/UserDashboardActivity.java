package com.example.bouquetbliss.user.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.common.auth.LoginActivity;
import com.example.bouquetbliss.databinding.ActivityUserDashboardBinding;
import com.example.bouquetbliss.user.base.BaseUserNavigationActivity;
import com.example.bouquetbliss.user.browse.BrowseBouquetsActivity;
import com.example.bouquetbliss.user.orders.UserOrdersActivity;
import com.google.firebase.auth.FirebaseAuth;

public class UserDashboardActivity extends BaseUserNavigationActivity {

    private ActivityUserDashboardBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        setupClickListeners();
        setupUserBottomNavigation();
        setSelectedNavItem("home");
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
            Toast.makeText(this, "Order History module coming soon!", Toast.LENGTH_SHORT).show();
        });

        binding.cardProfile.setOnClickListener(v -> {
            Toast.makeText(this, "User Profile module coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        // Bottom Navigation is handled by base class
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
