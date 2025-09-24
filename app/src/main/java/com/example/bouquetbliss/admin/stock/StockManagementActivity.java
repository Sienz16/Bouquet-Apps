package com.example.bouquetbliss.admin.stock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bouquetbliss.BouquetDetailsActivity;
import com.example.bouquetbliss.adapters.BouquetStockAdapter;
import com.example.bouquetbliss.admin.base.BaseAdminNavigationActivity;
import com.example.bouquetbliss.databinding.ActivityAdminStockManagementBinding;
import com.example.bouquetbliss.models.Bouquet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockManagementActivity extends BaseAdminNavigationActivity {

    private ActivityAdminStockManagementBinding binding;
    private FirebaseFirestore db;
    private BouquetStockAdapter adapter;
    private List<Bouquet> bouquetList;
    private List<Bouquet> filteredList;
    
    private String selectedCategory = "All Categories";
    private String selectedSort = "Name A-Z";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminStockManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        bouquetList = new ArrayList<>();
        filteredList = new ArrayList<>();

        setupViews();
        setupRecyclerView();
        setupSpinners();
        setupClickListeners();
        setupAdminBottomNavigation();
        setSelectedNavItem("stock");
        loadBouquets();
    }

    private void setupViews() {
        // Initially hide empty state
        binding.layoutEmptyState.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        adapter = new BouquetStockAdapter(filteredList, new BouquetStockAdapter.OnBouquetActionListener() {
            @Override
            public void onEditBouquet(Bouquet bouquet) {
                openEditBouquet(bouquet);
            }

            @Override
            public void onDeleteBouquet(Bouquet bouquet) {
                deleteBouquet(bouquet);
            }

            @Override
            public void onViewBouquet(Bouquet bouquet) {
                viewBouquetDetails(bouquet);
            }
        });

        binding.recyclerViewBouquets.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewBouquets.setAdapter(adapter);
    }

    private void setupSpinners() {
        // Category Spinner
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        categories.addAll(Arrays.asList(Bouquet.Categories.getAllCategories()));
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories.get(position);
                filterAndSortBouquets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Sort Spinner
        List<String> sortOptions = Arrays.asList(
            "Name A-Z", "Name Z-A", "Price Low-High", "Price High-Low", 
            "Stock Low-High", "Stock High-Low", "Newest First", "Oldest First"
        );
        
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSort.setAdapter(sortAdapter);

        binding.spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSort = sortOptions.get(position);
                filterAndSortBouquets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Search functionality coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        binding.btnStockSummary.setOnClickListener(v -> openStockSummary());

        binding.cardAddBouquet.setOnClickListener(v -> openAddBouquet());
        binding.fabAddBouquet.setOnClickListener(v -> openAddBouquet());

        
        // Add stock receiving navigation
        binding.btnStockReceiving.setOnClickListener(v -> openStockReceiving());
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
                    filterAndSortBouquets();
                    updateStatistics();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load bouquets: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }

    private void filterAndSortBouquets() {
        filteredList.clear();

        // Filter by category
        for (Bouquet bouquet : bouquetList) {
            if (selectedCategory.equals("All Categories") || 
                bouquet.getCategory().equals(selectedCategory)) {
                filteredList.add(bouquet);
            }
        }

        // Sort the filtered list
        switch (selectedSort) {
            case "Name A-Z":
                filteredList.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                break;
            case "Name Z-A":
                filteredList.sort((a, b) -> b.getName().compareToIgnoreCase(a.getName()));
                break;
            case "Price Low-High":
                filteredList.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
                break;
            case "Price High-Low":
                filteredList.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case "Stock Low-High":
                filteredList.sort((a, b) -> Integer.compare(a.getQuantity(), b.getQuantity()));
                break;
            case "Stock High-Low":
                filteredList.sort((a, b) -> Integer.compare(b.getQuantity(), a.getQuantity()));
                break;
            case "Newest First":
                filteredList.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                break;
            case "Oldest First":
                filteredList.sort((a, b) -> Long.compare(a.getCreatedAt(), b.getCreatedAt()));
                break;
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateStatistics() {
        int totalItems = bouquetList.size();
        int availableItems = 0;
        int outOfStockItems = 0;

        for (Bouquet bouquet : bouquetList) {
            if (bouquet.isAvailable()) {
                availableItems++;
            } else {
                outOfStockItems++;
            }
        }

        binding.tvTotalStock.setText(String.valueOf(totalItems));
        binding.tvAvailableStock.setText(String.valueOf(availableItems));
        binding.tvOutOfStock.setText(String.valueOf(outOfStockItems));
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            binding.recyclerViewBouquets.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewBouquets.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void openAddBouquet() {
        Intent intent = new Intent(this, AddEditBouquetActivity.class);
        startActivityForResult(intent, 100);
    }

    private void openEditBouquet(Bouquet bouquet) {
        Intent intent = new Intent(this, AddEditBouquetActivity.class);
        intent.putExtra("bouquet_id", bouquet.getId());
        intent.putExtra("edit_mode", true);
        startActivityForResult(intent, 101);
    }

    private void viewBouquetDetails(Bouquet bouquet) {
        Intent intent = new Intent(this, BouquetDetailsActivity.class);
        intent.putExtra("bouquet_id", bouquet.getId());
        startActivity(intent);
    }

    private void deleteBouquet(Bouquet bouquet) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Bouquet")
                .setMessage("Are you sure you want to delete \"" + bouquet.getName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("bouquets").document(bouquet.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Bouquet deleted successfully", Toast.LENGTH_SHORT).show();
                                loadBouquets(); // Refresh the list
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete bouquet: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void openStockReceiving() {
        Intent intent = new Intent(this, StockReceivingActivity.class);
        startActivity(intent);
    }
    
    private void openStockSummary() {
        Intent intent = new Intent(this, StockSummaryActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 100 || requestCode == 101) && resultCode == RESULT_OK) {
            // Refresh the bouquet list after adding/editing
            loadBouquets();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
