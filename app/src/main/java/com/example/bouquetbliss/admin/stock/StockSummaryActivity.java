package com.example.bouquetbliss.admin.stock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.adapters.StockSummaryAdapter;
import com.example.bouquetbliss.admin.base.BaseAdminNavigationActivity;
import com.example.bouquetbliss.databinding.ActivityStockSummaryBinding;
import com.example.bouquetbliss.models.Bouquet;
import com.example.bouquetbliss.models.StockTransaction;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for detailed stock summary and review
 */
public class StockSummaryActivity extends BaseAdminNavigationActivity {

    private ActivityStockSummaryBinding binding;
    private FirebaseFirestore db;
    private StockSummaryAdapter adapter;
    private List<Bouquet> bouquetList;
    private List<Bouquet> filteredList;
    private List<StockTransaction> recentTransactions;
    
    private String selectedPeriod = "This Month";
    private String selectedCategory = "All Categories";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStockSummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        bouquetList = new ArrayList<>();
        filteredList = new ArrayList<>();
        recentTransactions = new ArrayList<>();

        setupViews();
        setupRecyclerView();
        setupSpinners();
        setupClickListeners();
        setupAdminBottomNavigation();
        setSelectedNavItem("stock");
        
        loadData();
    }

    private void setupViews() {
        binding.layoutEmptyState.setVisibility(View.GONE);
        updateDateRange();
    }

    private void setupRecyclerView() {
        adapter = new StockSummaryAdapter(filteredList, new StockSummaryAdapter.OnStockSummaryActionListener() {
            @Override
            public void onViewDetails(Bouquet bouquet) {
                showBouquetStockDetails(bouquet);
            }

            @Override
            public void onViewTransactions(Bouquet bouquet) {
                showBouquetTransactionHistory(bouquet);
            }
        });
        binding.recyclerViewSummary.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewSummary.setAdapter(adapter);
    }

    private void setupSpinners() {
        // Period Spinner
        List<String> periods = List.of("Today", "This Week", "This Month", "Last Month", "Last 3 Months", "This Year");
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, periods);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPeriod.setAdapter(periodAdapter);
        binding.spinnerPeriod.setSelection(2); // Default to "This Month"

        binding.spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPeriod = periods.get(position);
                updateDateRange();
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Category Spinner
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        categories.add("Low Stock Items");
        categories.add("High Movement Items");
        for (String category : Bouquet.Categories.getAllCategories()) {
            categories.add(category);
        }
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories.get(position);
                filterBouquets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnExportReport.setOnClickListener(v -> {
            Toast.makeText(this, "Export functionality coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        binding.btnRefresh.setOnClickListener(v -> {
            loadData();
            Toast.makeText(this, "Data refreshed", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateDateRange() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        
        String startDate, endDate;
        
        switch (selectedPeriod) {
            case "Today":
                startDate = endDate = dateFormat.format(calendar.getTime());
                break;
            case "This Week":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                startDate = dateFormat.format(calendar.getTime());
                calendar.add(Calendar.DAY_OF_WEEK, 6);
                endDate = dateFormat.format(calendar.getTime());
                break;
            case "This Month":
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = dateFormat.format(calendar.getTime());
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = dateFormat.format(calendar.getTime());
                break;
            case "Last Month":
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = dateFormat.format(calendar.getTime());
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = dateFormat.format(calendar.getTime());
                break;
            case "Last 3 Months":
                calendar.add(Calendar.MONTH, -3);
                startDate = dateFormat.format(calendar.getTime());
                calendar = Calendar.getInstance();
                endDate = dateFormat.format(calendar.getTime());
                break;
            case "This Year":
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                startDate = dateFormat.format(calendar.getTime());
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                endDate = dateFormat.format(calendar.getTime());
                break;
            default:
                startDate = endDate = dateFormat.format(new Date());
        }
        
        binding.tvDateRange.setText(startDate.equals(endDate) ? startDate : startDate + " - " + endDate);
    }

    private void loadData() {
        loadBouquets();
        loadRecentTransactions();
    }

    private void loadBouquets() {
        db.collection("bouquets")
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bouquetList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Bouquet bouquet = document.toObject(Bouquet.class);
                        bouquet.setId(document.getId());
                        bouquetList.add(bouquet);
                    }
                    filterBouquets();
                    updateOverallStatistics();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load bouquets: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRecentTransactions() {
        // Get transactions for the selected period
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        
        // Calculate start time based on selected period
        switch (selectedPeriod) {
            case "Today":
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                break;
            case "This Week":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                break;
            case "This Month":
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case "Last Month":
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case "Last 3 Months":
                calendar.add(Calendar.MONTH, -3);
                break;
            case "This Year":
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                break;
        }
        
        long startTime = calendar.getTimeInMillis();
        
        db.collection("stock_transactions")
                .whereGreaterThanOrEqualTo("timestamp", startTime)
                .whereLessThanOrEqualTo("timestamp", endTime)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recentTransactions.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        StockTransaction transaction = document.toObject(StockTransaction.class);
                        transaction.setId(document.getId());
                        recentTransactions.add(transaction);
                    }
                    updateTransactionStatistics();
                })
                .addOnFailureListener(e -> {
                    // Silently handle as transactions might not exist yet
                    updateTransactionStatistics();
                });
    }

    private void filterBouquets() {
        filteredList.clear();

        for (Bouquet bouquet : bouquetList) {
            boolean shouldInclude = false;
            
            switch (selectedCategory) {
                case "All Categories":
                    shouldInclude = true;
                    break;
                case "Low Stock Items":
                    shouldInclude = bouquet.getQuantity() <= 5;
                    break;
                case "High Movement Items":
                    // This would require transaction analysis - simplified for now
                    shouldInclude = bouquet.getQuantity() > 0;
                    break;
                default:
                    shouldInclude = bouquet.getCategory().equals(selectedCategory);
                    break;
            }
            
            if (shouldInclude) {
                filteredList.add(bouquet);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateOverallStatistics() {
        int totalItems = bouquetList.size();
        int availableItems = 0;
        int lowStockItems = 0;
        int outOfStockItems = 0;
        double totalValue = 0;

        for (Bouquet bouquet : bouquetList) {
            totalValue += bouquet.getPrice() * bouquet.getQuantity();
            
            if (bouquet.getQuantity() <= 0) {
                outOfStockItems++;
            } else if (bouquet.getQuantity() <= 5) {
                lowStockItems++;
            } else {
                availableItems++;
            }
        }

        binding.tvTotalItems.setText(String.valueOf(totalItems));
        binding.tvAvailableItems.setText(String.valueOf(availableItems));
        binding.tvLowStockItems.setText(String.valueOf(lowStockItems));
        binding.tvOutOfStockItems.setText(String.valueOf(outOfStockItems));
        binding.tvTotalValue.setText(String.format("RM %.2f", totalValue));
    }

    private void updateTransactionStatistics() {
        int totalTransactions = recentTransactions.size();
        int stockInTransactions = 0;
        int stockOutTransactions = 0;

        for (StockTransaction transaction : recentTransactions) {
            if (transaction.isStockIncrease()) {
                stockInTransactions++;
            } else {
                stockOutTransactions++;
            }
        }

        binding.tvTotalTransactions.setText(String.valueOf(totalTransactions));
        binding.tvStockInTransactions.setText(String.valueOf(stockInTransactions));
        binding.tvStockOutTransactions.setText(String.valueOf(stockOutTransactions));
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            binding.recyclerViewSummary.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewSummary.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void showBouquetStockDetails(Bouquet bouquet) {
        // TODO: Show detailed stock information for the bouquet
        Toast.makeText(this, "Stock details for " + bouquet.getName(), Toast.LENGTH_SHORT).show();
    }

    private void showBouquetTransactionHistory(Bouquet bouquet) {
        Intent intent = new Intent(this, StockHistoryActivity.class);
        intent.putExtra("bouquet_id", bouquet.getId());
        intent.putExtra("bouquet_name", bouquet.getName());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
