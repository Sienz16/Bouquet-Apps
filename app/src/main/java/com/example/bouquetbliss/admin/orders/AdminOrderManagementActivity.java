package com.example.bouquetbliss.admin.orders;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.adapters.OrderAdapter;
import com.example.bouquetbliss.admin.base.BaseAdminNavigationActivity;
import com.example.bouquetbliss.databinding.ActivityAdminOrderManagementBinding;
import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.models.OrderItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminOrderManagementActivity extends BaseAdminNavigationActivity implements OrderAdapter.OnOrderActionListener {

    private ActivityAdminOrderManagementBinding binding;
    private FirebaseFirestore db;
    private OrderAdapter adapter;
    private List<Order> orderList;
    private List<Order> filteredList;
    
    private String currentFilter = "pending"; // pending, approved, completed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminOrderManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();
        filteredList = new ArrayList<>();

        setupViews();
        setupRecyclerView();
        setupClickListeners();
        setupAdminBottomNavigation();
        setSelectedNavItem("orders");
        loadOrders();
    }

    private void setupViews() {
        binding.layoutEmptyState.setVisibility(View.GONE);
        updateTabUI();
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter(filteredList, this);
        binding.recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewOrders.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Filter options coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Tab click listeners
        binding.tabPending.setOnClickListener(v -> {
            currentFilter = "pending";
            updateTabUI();
            filterOrders();
        });

        binding.tabApproved.setOnClickListener(v -> {
            currentFilter = "approved";
            updateTabUI();
            filterOrders();
        });

        binding.tabCompleted.setOnClickListener(v -> {
            currentFilter = "completed";
            updateTabUI();
            filterOrders();
        });
    }

    private void updateTabUI() {
        // Reset all tabs
        resetTab(binding.tabPending);
        resetTab(binding.tabApproved);
        resetTab(binding.tabCompleted);

        // Set active tab
        switch (currentFilter) {
            case "pending":
                setActiveTab(binding.tabPending);
                binding.tvOrdersTitle.setText("Pending Orders");
                binding.tvEmptyMessage.setText("No pending orders at the moment");
                break;
            case "approved":
                setActiveTab(binding.tabApproved);
                binding.tvOrdersTitle.setText("Approved Orders");
                binding.tvEmptyMessage.setText("No approved orders in progress");
                break;
            case "completed":
                setActiveTab(binding.tabCompleted);
                binding.tvOrdersTitle.setText("Completed Orders");
                binding.tvEmptyMessage.setText("No completed orders yet");
                break;
        }
    }

    private void resetTab(View tab) {
        tab.setBackgroundResource(R.drawable.spinner_background);
        // Find TextView in the tab and set color
        if (tab instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout layout = (android.widget.LinearLayout) tab;
            if (layout.getChildCount() > 0 && layout.getChildAt(0) instanceof android.widget.TextView) {
                android.widget.TextView textView = (android.widget.TextView) layout.getChildAt(0);
                textView.setTextColor(getResources().getColor(R.color.text_secondary));
            }
        }
    }

    private void setActiveTab(View tab) {
        tab.setBackgroundResource(R.drawable.soft_pink_button);
        // Find TextView in the tab and set color
        if (tab instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout layout = (android.widget.LinearLayout) tab;
            if (layout.getChildCount() > 0 && layout.getChildAt(0) instanceof android.widget.TextView) {
                android.widget.TextView textView = (android.widget.TextView) layout.getChildAt(0);
                textView.setTextColor(getResources().getColor(R.color.white));
            }
        }
    }

    private void loadOrders() {
        // For demo purposes, create some sample orders
        createSampleOrders();
        
        // In a real app, you would load from Firestore:
        /*
        db.collection("orders")
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        order.setId(document.getId());
                        orderList.add(order);
                    }
                    updateStatistics();
                    filterOrders();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load orders: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        */
    }

    private void createSampleOrders() {
        orderList.clear();
        
        // Sample Order 1 - Pending
        List<OrderItem> items1 = Arrays.asList(
            new OrderItem("b1", "Rose Garden Bouquet", "", 75.0, 2)
        );
        Order order1 = new Order("c1", "John Doe", "+60 12-345 6789", "john@email.com",
            items1, 150.0, System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000), "123 Main St");
        order1.setId("order1");
        orderList.add(order1);

        // Sample Order 2 - Approved
        List<OrderItem> items2 = Arrays.asList(
            new OrderItem("b2", "Tulip Delight", "", 65.0, 1)
        );
        Order order2 = new Order("c2", "Jane Smith", "+60 19-876 5432", "jane@email.com",
            items2, 65.0, System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000), "456 Oak Ave");
        order2.setId("order2");
        order2.setStatus(Order.Status.APPROVED);
        order2.setPreparationStatus(Order.PreparationStatus.PREPARING);
        orderList.add(order2);

        // Sample Order 3 - Completed
        List<OrderItem> items3 = Arrays.asList(
            new OrderItem("b3", "Sunflower Joy", "", 55.0, 3)
        );
        Order order3 = new Order("c3", "Bob Johnson", "+60 16-234 5678", "bob@email.com",
            items3, 165.0, System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000), "789 Pine Rd");
        order3.setId("order3");
        order3.setStatus(Order.Status.COMPLETED);
        order3.setPreparationStatus(Order.PreparationStatus.COMPLETED);
        orderList.add(order3);

        // Sample Order 4 - Pending
        List<OrderItem> items4 = Arrays.asList(
            new OrderItem("b4", "Lily Elegance", "", 80.0, 1)
        );
        Order order4 = new Order("c4", "Alice Wilson", "+60 17-345 6789", "alice@email.com",
            items4, 80.0, System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000), "321 Elm St");
        order4.setId("order4");
        orderList.add(order4);

        updateStatistics();
        filterOrders();
    }

    private void filterOrders() {
        filteredList.clear();
        
        for (Order order : orderList) {
            boolean shouldInclude = false;
            switch (currentFilter) {
                case "pending":
                    shouldInclude = order.isPending();
                    break;
                case "approved":
                    shouldInclude = order.isApproved();
                    break;
                case "completed":
                    shouldInclude = order.isCompleted();
                    break;
            }
            
            if (shouldInclude) {
                filteredList.add(order);
            }
        }
        
        adapter.updateOrders(filteredList);
        updateEmptyState();
    }

    private void updateStatistics() {
        int pendingCount = 0;
        int approvedCount = 0;
        int completedCount = 0;

        for (Order order : orderList) {
            if (order.isPending()) {
                pendingCount++;
            } else if (order.isApproved()) {
                approvedCount++;
            } else if (order.isCompleted()) {
                completedCount++;
            }
        }

        binding.tvPendingCount.setText(String.valueOf(pendingCount));
        binding.tvApprovedCount.setText(String.valueOf(approvedCount));
        binding.tvCompletedCount.setText(String.valueOf(completedCount));
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            binding.recyclerViewOrders.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewOrders.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }

    // OrderAdapter.OnOrderActionListener implementations
    @Override
    public void onViewDetails(Order order) {
        // TODO: Implement order details view
        Toast.makeText(this, "Order details for " + order.getFormattedOrderId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApproveOrder(Order order) {
        new AlertDialog.Builder(this)
            .setTitle("Approve Order")
            .setMessage("Are you sure you want to approve order " + order.getFormattedOrderId() + "?")
            .setPositiveButton("Approve", (dialog, which) -> {
                order.setStatus(Order.Status.APPROVED);
                updateOrderInDatabase(order);
                updateStatistics();
                filterOrders();
                Toast.makeText(this, "Order approved successfully", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onRejectOrder(Order order) {
        EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setHint("Enter rejection reason (optional)");
        
        new AlertDialog.Builder(this)
            .setTitle("Reject Order")
            .setMessage("Reject order " + order.getFormattedOrderId() + "?")
            .setView(editText)
            .setPositiveButton("Reject", (dialog, which) -> {
                String reason = editText.getText().toString().trim();
                order.setStatus(Order.Status.REJECTED);
                if (!reason.isEmpty()) {
                    order.setRejectionReason(reason);
                }
                updateOrderInDatabase(order);
                updateStatistics();
                filterOrders();
                Toast.makeText(this, "Order rejected", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onUpdatePreparationStatus(Order order, String newStatus) {
        String statusValue = convertStatusToValue(newStatus);
        order.setPreparationStatus(statusValue);
        
        // Update main status if needed
        if (statusValue.equals(Order.PreparationStatus.COMPLETED)) {
            order.setStatus(Order.Status.COMPLETED);
        } else if (statusValue.equals(Order.PreparationStatus.READY_FOR_PICKUP)) {
            order.setStatus(Order.Status.READY);
        } else if (statusValue.equals(Order.PreparationStatus.PREPARING)) {
            order.setStatus(Order.Status.IN_PREPARATION);
        }
        
        updateOrderInDatabase(order);
        updateStatistics();
        filterOrders();
        Toast.makeText(this, "Preparation status updated", Toast.LENGTH_SHORT).show();
    }

    private String convertStatusToValue(String displayStatus) {
        switch (displayStatus) {
            case "Not Started":
                return Order.PreparationStatus.NOT_STARTED;
            case "Preparing":
                return Order.PreparationStatus.PREPARING;
            case "Ready for Pickup":
                return Order.PreparationStatus.READY_FOR_PICKUP;
            case "Completed":
                return Order.PreparationStatus.COMPLETED;
            default:
                return Order.PreparationStatus.NOT_STARTED;
        }
    }

    private void updateOrderInDatabase(Order order) {
        // In a real app, update Firestore:
        /*
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", order.getStatus());
        updates.put("preparationStatus", order.getPreparationStatus());
        updates.put("updatedAt", order.getUpdatedAt());
        if (order.getRejectionReason() != null) {
            updates.put("rejectionReason", order.getRejectionReason());
        }

        db.collection("orders").document(order.getId())
            .update(updates)
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update order: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
