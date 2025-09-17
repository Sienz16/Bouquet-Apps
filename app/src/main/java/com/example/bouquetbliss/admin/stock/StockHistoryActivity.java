package com.example.bouquetbliss.admin.stock;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bouquetbliss.adapters.StockTransactionAdapter;
import com.example.bouquetbliss.admin.base.BaseAdminNavigationActivity;
import com.example.bouquetbliss.databinding.ActivityStockHistoryBinding;
import com.example.bouquetbliss.models.Bouquet;
import com.example.bouquetbliss.models.StockTransaction;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity for displaying stock transaction history for a specific bouquet
 */
public class StockHistoryActivity extends BaseAdminNavigationActivity {

    private ActivityStockHistoryBinding binding;
    private FirebaseFirestore db;
    private StockTransactionAdapter adapter;
    private List<StockTransaction> transactionList;
    
    private String bouquetId;
    private String bouquetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStockHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        transactionList = new ArrayList<>();

        // Get bouquet information from intent
        bouquetId = getIntent().getStringExtra("bouquet_id");
        bouquetName = getIntent().getStringExtra("bouquet_name");

        if (bouquetId == null || bouquetName == null) {
            Toast.makeText(this, "Invalid bouquet information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
        setupRecyclerView();
        setupClickListeners();
        setupAdminBottomNavigation();
        setSelectedNavItem("stock");
        
        loadBouquetInfo();
        loadTransactionHistory();
    }

    private void setupViews() {
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.tvBouquetName.setText(bouquetName);
        binding.tvLastUpdated.setText("Loading...");
    }

    private void setupRecyclerView() {
        adapter = new StockTransactionAdapter(transactionList);
        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewHistory.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnRefresh.setOnClickListener(v -> {
            loadTransactionHistory();
            Toast.makeText(this, "History refreshed", Toast.LENGTH_SHORT).show();
        });
        
        binding.btnExportHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Export functionality coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadBouquetInfo() {
        db.collection("bouquets").document(bouquetId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Bouquet bouquet = documentSnapshot.toObject(Bouquet.class);
                    if (bouquet != null) {
                        updateBouquetInfo(bouquet);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load bouquet info: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void updateBouquetInfo(Bouquet bouquet) {
        binding.tvCurrentStock.setText(String.valueOf(bouquet.getQuantity()));
        binding.tvStockStatus.setText(bouquet.getAvailability());
        binding.tvBouquetCategory.setText(bouquet.getCategory());
        
        // Set status color
        int statusColor;
        switch (bouquet.getAvailability()) {
            case "Available":
                statusColor = getColor(android.R.color.holo_green_dark);
                break;
            case "Limited":
                statusColor = getColor(android.R.color.holo_orange_dark);
                break;
            case "Out of Stock":
                statusColor = getColor(android.R.color.holo_red_dark);
                break;
            default:
                statusColor = getColor(android.R.color.darker_gray);
                break;
        }
        binding.tvStockStatus.setTextColor(statusColor);
        
        // Format last updated time
        if (bouquet.getUpdatedAt() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
            String lastUpdated = dateFormat.format(new Date(bouquet.getUpdatedAt()));
            binding.tvLastUpdated.setText("Last updated: " + lastUpdated);
        } else {
            binding.tvLastUpdated.setText("Never updated");
        }
    }

    private void loadTransactionHistory() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        db.collection("stock_transactions")
                .whereEqualTo("bouquetId", bouquetId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    transactionList.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        StockTransaction transaction = document.toObject(StockTransaction.class);
                        transaction.setId(document.getId());
                        transactionList.add(transaction);
                    }
                    
                    adapter.notifyDataSetChanged();
                    updateStatistics();
                    updateEmptyState();
                    binding.progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load transaction history: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void updateStatistics() {
        int totalTransactions = transactionList.size();
        int stockInCount = 0;
        int stockOutCount = 0;
        int adjustmentCount = 0;
        
        for (StockTransaction transaction : transactionList) {
            switch (transaction.getType()) {
                case STOCK_IN:
                    stockInCount++;
                    break;
                case STOCK_OUT:
                case ORDER_FULFILLMENT:
                case DAMAGED:
                case EXPIRED:
                    stockOutCount++;
                    break;
                case ADJUSTMENT:
                    adjustmentCount++;
                    break;
            }
        }
        
        binding.tvTotalTransactions.setText(String.valueOf(totalTransactions));
        binding.tvStockInCount.setText(String.valueOf(stockInCount));
        binding.tvStockOutCount.setText(String.valueOf(stockOutCount));
        binding.tvAdjustmentCount.setText(String.valueOf(adjustmentCount));
    }

    private void updateEmptyState() {
        if (transactionList.isEmpty()) {
            binding.recyclerViewHistory.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewHistory.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
