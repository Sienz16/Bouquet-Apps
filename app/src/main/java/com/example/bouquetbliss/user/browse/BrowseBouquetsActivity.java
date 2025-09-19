package com.example.bouquetbliss.user.browse;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.bouquetbliss.databinding.ActivityBrowseBouquetsBinding;
import com.example.bouquetbliss.models.Bouquet;
import com.example.bouquetbliss.repository.FavoriteRepository;
import com.example.bouquetbliss.user.base.BaseUserNavigationActivity;
import com.example.bouquetbliss.user.detail.BouquetDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BrowseBouquetsActivity extends BaseUserNavigationActivity {

    private ActivityBrowseBouquetsBinding binding;
    private FirebaseFirestore db;
    private BrowseBouquetsAdapter adapter;
    private FavoriteRepository favoriteRepository;
    private List<Bouquet> bouquetList;
    private List<Bouquet> filteredBouquetList;
    private Set<String> favoriteIds;
    private String currentSearchQuery = "";
    private String currentCategoryFilter = "All";
    private String currentSizeFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseBouquetsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        favoriteRepository = new FavoriteRepository();
        bouquetList = new ArrayList<>();
        filteredBouquetList = new ArrayList<>();
        favoriteIds = new HashSet<>();

        setupRecyclerView();
        setupSearchAndFilter();
        setupUserBottomNavigation();
        setSelectedNavItem("browse");
        loadFavorites();
        loadBouquets();
    }

    private void setupRecyclerView() {
        adapter = new BrowseBouquetsAdapter(filteredBouquetList, this::onBouquetClick);
        binding.recyclerViewBouquets.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerViewBouquets.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        // Setup search functionality
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString().trim();
                filterBouquets();
            }
        });

        // Setup filter button
        binding.cardFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void loadBouquets() {
        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        
        db.collection("bouquets")
                .get()
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(android.view.View.GONE);
                    
                    if (task.isSuccessful()) {
                        bouquetList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bouquet bouquet = document.toObject(Bouquet.class);
                            bouquet.setId(document.getId());
                            // Only add bouquets that have quantity > 0
                            if (bouquet.getQuantity() > 0) {
                                bouquetList.add(bouquet);
                            }
                        }
                        filterBouquets(); // Apply current filters
                        
                        if (bouquetList.isEmpty()) {
                            binding.textNoResults.setVisibility(android.view.View.VISIBLE);
                            binding.textNoResults.setText("No bouquets available");
                        } else {
                            binding.textNoResults.setVisibility(android.view.View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "Error loading bouquets: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterBouquets() {
        filteredBouquetList.clear();
        
        for (Bouquet bouquet : bouquetList) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    bouquet.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    bouquet.getDescription().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    bouquet.getCategory().toLowerCase().contains(currentSearchQuery.toLowerCase());
            
            boolean matchesCategory = currentCategoryFilter.equals("All") ||
                    bouquet.getCategory().equals(currentCategoryFilter);
            
            boolean matchesSize = currentSizeFilter.equals("All") ||
                    bouquet.getSize().equals(currentSizeFilter);
            
            if (matchesSearch && matchesCategory && matchesSize) {
                filteredBouquetList.add(bouquet);
            }
        }
        
        adapter.notifyDataSetChanged();
        
        if (filteredBouquetList.isEmpty() && !bouquetList.isEmpty()) {
            binding.textNoResults.setVisibility(android.view.View.VISIBLE);
            binding.textNoResults.setText("No bouquets match your search criteria");
        } else {
            binding.textNoResults.setVisibility(android.view.View.GONE);
        }
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Bouquets");
        
        String[] options = {"Filter by Category", "Filter by Size", "Clear Filters"};
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showCategoryFilter();
                    break;
                case 1:
                    showSizeFilter();
                    break;
                case 2:
                    clearFilters();
                    break;
            }
        });
        
        builder.show();
    }

    private void showCategoryFilter() {
        String[] categories = {"All", "Wedding", "Birthday", "Anniversary", "Romantic", 
                             "Sympathy", "Congratulations", "Seasonal", "Custom"};
        
        int currentIndex = 0;
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(currentCategoryFilter)) {
                currentIndex = i;
                break;
            }
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Category")
                .setSingleChoiceItems(categories, currentIndex, (dialog, which) -> {
                    currentCategoryFilter = categories[which];
                    filterBouquets();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null);
        
        builder.show();
    }

    private void showSizeFilter() {
        String[] sizes = {"All", "Small", "Medium", "Large", "Extra Large"};
        
        int currentIndex = 0;
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i].equals(currentSizeFilter)) {
                currentIndex = i;
                break;
            }
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Size")
                .setSingleChoiceItems(sizes, currentIndex, (dialog, which) -> {
                    currentSizeFilter = sizes[which];
                    filterBouquets();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null);
        
        builder.show();
    }

    private void clearFilters() {
        currentSearchQuery = "";
        currentCategoryFilter = "All";
        currentSizeFilter = "All";
        binding.editTextSearch.setText("");
        filterBouquets();
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }

    private void onBouquetClick(Bouquet bouquet) {
        Intent intent = new Intent(this, BouquetDetailActivity.class);
        
        // Pass bouquet data to detail activity
        if (bouquet.getId() != null && !bouquet.getId().isEmpty()) {
            intent.putExtra("bouquet_id", bouquet.getId());
        } else {
            // Fallback: pass individual fields
            intent.putExtra("bouquet_name", bouquet.getName());
            intent.putExtra("bouquet_description", bouquet.getDescription());
            intent.putExtra("bouquet_price", bouquet.getPrice());
            intent.putExtra("bouquet_quantity", bouquet.getQuantity());
            intent.putExtra("bouquet_category", bouquet.getCategory());
            intent.putExtra("bouquet_size", bouquet.getSize());
        }
        
        startActivity(intent);
    }

    private void loadFavorites() {
        favoriteRepository.getUserFavorites(new FavoriteRepository.FavoritesListCallback() {
            @Override
            public void onSuccess(List<String> bouquetIds) {
                favoriteIds.clear();
                favoriteIds.addAll(bouquetIds);
                if (adapter != null) {
                    adapter.updateFavorites(favoriteIds);
                }
            }

            @Override
            public void onError(String error) {
                // Silently handle error - favorites are optional
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh favorites when returning to this activity
        loadFavorites();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
