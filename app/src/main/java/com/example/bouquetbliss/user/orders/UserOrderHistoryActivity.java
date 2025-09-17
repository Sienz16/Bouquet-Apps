package com.example.bouquetbliss.user.orders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.adapters.UserOrderHistoryAdapter;
import com.example.bouquetbliss.databinding.ActivityUserOrderHistoryBinding;
import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.user.base.BaseUserNavigationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Activity for users to view their complete order history with statistics
 */
public class UserOrderHistoryActivity extends BaseUserNavigationActivity {

    private ActivityUserOrderHistoryBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private UserOrderHistoryAdapter adapter;
    private List<Order> orderList;
    private List<Order> filteredOrderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        orderList = new ArrayList<>();
        filteredOrderList = new ArrayList<>();

        setupViews();
        setupClickListeners();
        setupUserBottomNavigation();
        setSelectedNavItem("orders");

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            loadUserOrderHistory();
        } else {
            showNoOrdersMessage("Please login to view your order history");
        }
    }

    private void setupViews() {
        // Setup RecyclerView
        adapter = new UserOrderHistoryAdapter(filteredOrderList, this::onOrderClick);
        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewHistory.setAdapter(adapter);

        // Initialize statistics
        updateStatistics();
    }

    private void setupClickListeners() {
        // Filter buttons
        binding.btnFilterAll.setOnClickListener(v -> filterOrders("ALL"));
        binding.btnFilterPending.setOnClickListener(v -> filterOrders("PENDING"));
        binding.btnFilterCompleted.setOnClickListener(v -> filterOrders("COMPLETED"));
        binding.btnFilterRejected.setOnClickListener(v -> filterOrders("REJECTED"));

        // Refresh button
        binding.btnRefresh.setOnClickListener(v -> {
            loadUserOrderHistory();
            Toast.makeText(this, "Order history refreshed", Toast.LENGTH_SHORT).show();
        });

        // Browse bouquets button (in no orders layout)
        binding.btnBrowseBouquets.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.bouquetbliss.user.browse.BrowseBouquetsActivity.class);
            startActivity(intent);
        });

        // Period filter buttons
        binding.btnThisMonth.setOnClickListener(v -> filterByPeriod("THIS_MONTH"));
        binding.btnLastMonth.setOnClickListener(v -> filterByPeriod("LAST_MONTH"));
        binding.btnLast3Months.setOnClickListener(v -> filterByPeriod("LAST_3_MONTHS"));
        binding.btnAllTime.setOnClickListener(v -> filterByPeriod("ALL_TIME"));
    }

    private void loadUserOrderHistory() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutNoOrders.setVisibility(View.GONE);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            showNoOrdersMessage("Please login to view your orders");
            return;
        }

        db.collection("orders")
            .whereEqualTo("customerId", currentUser.getUid())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                orderList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Order order = document.toObject(Order.class);
                    order.setId(document.getId());
                    orderList.add(order);
                }

                // Sort by orderDate in descending order (newest first)
                Collections.sort(orderList, new Comparator<Order>() {
                    @Override
                    public int compare(Order o1, Order o2) {
                        return Long.compare(o2.getOrderDate(), o1.getOrderDate());
                    }
                });

                filteredOrderList.clear();
                filteredOrderList.addAll(orderList);
                
                updateStatistics();
                adapter.notifyDataSetChanged();
                
                binding.progressBar.setVisibility(View.GONE);
                
                if (orderList.isEmpty()) {
                    showNoOrdersMessage("You haven't placed any orders yet");
                } else {
                    binding.layoutContent.setVisibility(View.VISIBLE);
                    binding.layoutNoOrders.setVisibility(View.GONE);
                }
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load order history: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
                showNoOrdersMessage("Failed to load your orders");
            });
    }

    private void filterOrders(String status) {
        // Reset filter buttons
        resetFilterButtons();
        
        // Set active filter button
        switch (status) {
            case "ALL":
                binding.btnFilterAll.setBackgroundTintList(
                    getColorStateList(R.color.pink_primary));
                binding.btnFilterAll.setTextColor(getColor(R.color.white));
                break;
            case "PENDING":
                binding.btnFilterPending.setBackgroundTintList(
                    getColorStateList(R.color.pink_primary));
                binding.btnFilterPending.setTextColor(getColor(R.color.white));
                break;
            case "COMPLETED":
                binding.btnFilterCompleted.setBackgroundTintList(
                    getColorStateList(R.color.pink_primary));
                binding.btnFilterCompleted.setTextColor(getColor(R.color.white));
                break;
            case "REJECTED":
                binding.btnFilterRejected.setBackgroundTintList(
                    getColorStateList(R.color.pink_primary));
                binding.btnFilterRejected.setTextColor(getColor(R.color.white));
                break;
        }
        
        // Apply filter
        filteredOrderList.clear();
        for (Order order : orderList) {
            if (status.equals("ALL") || order.getStatus().equalsIgnoreCase(status)) {
                filteredOrderList.add(order);
            }
        }
        
        adapter.notifyDataSetChanged();
        updateFilteredStatistics();
    }

    private void filterByPeriod(String period) {
        // Reset period filter buttons
        resetPeriodFilterButtons();
        
        Calendar now = Calendar.getInstance();
        Calendar filterDate = Calendar.getInstance();
        
        switch (period) {
            case "THIS_MONTH":
                binding.btnThisMonth.setBackgroundTintList(getColorStateList(R.color.pink_primary));
                binding.btnThisMonth.setTextColor(getColor(R.color.white));
                filterDate.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case "LAST_MONTH":
                binding.btnLastMonth.setBackgroundTintList(getColorStateList(R.color.pink_primary));
                binding.btnLastMonth.setTextColor(getColor(R.color.white));
                filterDate.add(Calendar.MONTH, -1);
                filterDate.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case "LAST_3_MONTHS":
                binding.btnLast3Months.setBackgroundTintList(getColorStateList(R.color.pink_primary));
                binding.btnLast3Months.setTextColor(getColor(R.color.white));
                filterDate.add(Calendar.MONTH, -3);
                break;
            case "ALL_TIME":
            default:
                binding.btnAllTime.setBackgroundTintList(getColorStateList(R.color.pink_primary));
                binding.btnAllTime.setTextColor(getColor(R.color.white));
                filterDate.set(Calendar.YEAR, 2020); // Set to early date for all time
                break;
        }
        
        // Apply period filter
        filteredOrderList.clear();
        long filterTime = filterDate.getTimeInMillis();
        
        for (Order order : orderList) {
            if (order.getOrderDate() >= filterTime) {
                filteredOrderList.add(order);
            }
        }
        
        adapter.notifyDataSetChanged();
        updateFilteredStatistics();
    }

    private void resetFilterButtons() {
        binding.btnFilterAll.setBackgroundTintList(getColorStateList(R.color.text_secondary));
        binding.btnFilterAll.setTextColor(getColor(R.color.white));
        binding.btnFilterPending.setBackgroundTintList(getColorStateList(R.color.text_secondary));
        binding.btnFilterPending.setTextColor(getColor(R.color.white));
        binding.btnFilterCompleted.setBackgroundTintList(getColorStateList(R.color.text_secondary));
        binding.btnFilterCompleted.setTextColor(getColor(R.color.white));
        binding.btnFilterRejected.setBackgroundTintList(getColorStateList(R.color.text_secondary));
        binding.btnFilterRejected.setTextColor(getColor(R.color.white));
    }

    private void resetPeriodFilterButtons() {
        binding.btnThisMonth.setBackgroundTintList(getColorStateList(R.color.text_secondary));
        binding.btnThisMonth.setTextColor(getColor(R.color.white));
        binding.btnLastMonth.setBackgroundTintList(getColorStateList(R.color.text_secondary));
        binding.btnLastMonth.setTextColor(getColor(R.color.white));
        binding.btnLast3Months.setBackgroundTintList(getColorStateList(R.color.text_secondary));
        binding.btnLast3Months.setTextColor(getColor(R.color.white));
        binding.btnAllTime.setBackgroundTintList(getColorStateList(R.color.text_secondary));
        binding.btnAllTime.setTextColor(getColor(R.color.white));
    }

    private void updateStatistics() {
        int totalOrders = orderList.size();
        int pendingOrders = 0;
        int completedOrders = 0;
        int rejectedOrders = 0;
        double totalSpent = 0.0;
        
        for (Order order : orderList) {
            switch (order.getStatus()) {
                case Order.Status.PENDING:
                case Order.Status.APPROVED:
                case Order.Status.IN_PREPARATION:
                case Order.Status.READY:
                    pendingOrders++;
                    break;
                case Order.Status.COMPLETED:
                    completedOrders++;
                    totalSpent += order.getTotalAmount();
                    break;
                case Order.Status.REJECTED:
                    rejectedOrders++;
                    break;
            }
        }
        
        binding.tvTotalOrders.setText(String.valueOf(totalOrders));
        binding.tvPendingOrders.setText(String.valueOf(pendingOrders));
        binding.tvCompletedOrders.setText(String.valueOf(completedOrders));
        binding.tvRejectedOrders.setText(String.valueOf(rejectedOrders));
        binding.tvTotalSpent.setText(String.format("RM %.2f", totalSpent));
    }

    private void updateFilteredStatistics() {
        int filteredCount = filteredOrderList.size();
        double filteredSpent = 0.0;
        
        for (Order order : filteredOrderList) {
            if (order.getStatus().equals(Order.Status.COMPLETED)) {
                filteredSpent += order.getTotalAmount();
            }
        }
        
        binding.tvFilteredCount.setText("Showing " + filteredCount + " orders");
        binding.tvFilteredSpent.setText(String.format("Total: RM %.2f", filteredSpent));
        binding.tvFilteredCount.setVisibility(View.VISIBLE);
        binding.tvFilteredSpent.setVisibility(View.VISIBLE);
    }

    private void onOrderClick(Order order) {
        Intent intent = new Intent(this, UserOrderDetailsActivity.class);
        intent.putExtra("order_id", order.getId());
        startActivity(intent);
    }

    private void showNoOrdersMessage(String message) {
        binding.layoutContent.setVisibility(View.GONE);
        binding.layoutNoOrders.setVisibility(View.VISIBLE);
        binding.tvNoOrders.setText(message);
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        if (auth.getCurrentUser() != null) {
            loadUserOrderHistory();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
