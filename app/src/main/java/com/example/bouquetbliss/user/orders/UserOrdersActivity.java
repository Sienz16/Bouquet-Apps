package com.example.bouquetbliss.user.orders;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bouquetbliss.databinding.ActivityUserOrdersBinding;
import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.user.base.BaseUserNavigationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserOrdersActivity extends BaseUserNavigationActivity {

    private ActivityUserOrdersBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private UserOrdersAdapter adapter;
    private List<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        orderList = new ArrayList<>();

        setupRecyclerView();
        setupRefreshButton();
        setupUserBottomNavigation();
        setSelectedNavItem("orders");
        
        // Apply translations
        applyTranslations();
        
        if (auth.getCurrentUser() != null) {
            loadUserOrders();
        } else {
            showNoOrdersMessage("Please login to view your orders");
        }
    }

    private void setupRecyclerView() {
        adapter = new UserOrdersAdapter(orderList, this::onOrderClick);
        binding.recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewOrders.setAdapter(adapter);
    }

    private void setupRefreshButton() {
        binding.cardRefresh.setOnClickListener(v -> refreshOrders());
    }

    private void loadUserOrders() {
        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        binding.layoutNoOrders.setVisibility(android.view.View.GONE);
        
        db.collection("orders")
                .whereEqualTo("customerId", auth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(android.view.View.GONE);
                    
                    if (task.isSuccessful()) {
                        orderList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Order order = document.toObject(Order.class);
                            order.setId(document.getId());
                            orderList.add(order);
                        }
                        
                        // Sort orders by creation date (newest first)
                        Collections.sort(orderList, new Comparator<Order>() {
                            @Override
                            public int compare(Order o1, Order o2) {
                                return Long.compare(o2.getCreatedAt(), o1.getCreatedAt());
                            }
                        });
                        
                        adapter.notifyDataSetChanged();
                        
                        if (orderList.isEmpty()) {
                            showNoOrdersMessage("No orders found. Start browsing bouquets to place your first order!");
                        } else {
                            binding.layoutNoOrders.setVisibility(android.view.View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "Error loading orders: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                                Toast.LENGTH_SHORT).show();
                        showNoOrdersMessage("Failed to load orders. Please try again.");
                    }
                });
    }

    private void showNoOrdersMessage(String message) {
        binding.textNoOrders.setText(message);
        binding.layoutNoOrders.setVisibility(android.view.View.VISIBLE);
    }

    private void onOrderClick(Order order) {
        Intent intent = new Intent(this, UserOrderDetailsActivity.class);
        intent.putExtra("order_id", order.getId());
        startActivity(intent);
    }

    public void refreshOrders() {
        if (auth.getCurrentUser() != null) {
            loadUserOrders();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh orders when returning to this activity
        refreshOrders();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
