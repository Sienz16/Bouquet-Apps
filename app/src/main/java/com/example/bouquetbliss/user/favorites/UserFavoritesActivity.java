package com.example.bouquetbliss.user.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;

import com.example.bouquetbliss.databinding.ActivityUserFavoritesBinding;
import com.example.bouquetbliss.models.Bouquet;
import com.example.bouquetbliss.repository.FavoriteRepository;
import com.example.bouquetbliss.user.base.BaseUserNavigationActivity;
import com.example.bouquetbliss.user.detail.BouquetDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserFavoritesActivity extends BaseUserNavigationActivity {

    private ActivityUserFavoritesBinding binding;
    private FirebaseFirestore db;
    private FavoriteRepository favoriteRepository;
    private FavoritesAdapter adapter;
    private List<Bouquet> favoritesBouquetList;
    private Set<String> favoriteIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        favoriteRepository = new FavoriteRepository();
        favoritesBouquetList = new ArrayList<>();
        favoriteIds = new HashSet<>();

        setupRecyclerView();
        setupUserBottomNavigation();
        setSelectedNavItem("favorites");
        
        // Apply translations
        applyTranslations();
    }

    private void setupRecyclerView() {
        adapter = new FavoritesAdapter(favoritesBouquetList, this::onBouquetClick, this::onRemoveFromFavorites);
        binding.recyclerViewFavorites.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerViewFavorites.setAdapter(adapter);
    }

    private void loadFavorites() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.textNoFavorites.setVisibility(View.GONE);

        favoriteRepository.getUserFavorites(new FavoriteRepository.FavoritesListCallback() {
            @Override
            public void onSuccess(List<String> bouquetIds) {
                favoriteIds.clear();
                favoriteIds.addAll(bouquetIds);

                if (bouquetIds.isEmpty()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textNoFavorites.setVisibility(View.VISIBLE);
                    favoritesBouquetList.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }

                // Load bouquet details for each favorite
                loadBouquetDetails(bouquetIds);
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.textNoFavorites.setVisibility(View.VISIBLE);
                binding.textNoFavoritesMessage.setText("Error loading favorites: " + error);
            }
        });
    }

    private void loadBouquetDetails(List<String> bouquetIds) {
        favoritesBouquetList.clear();
        int[] loadedCount = {0}; // Using array to make it effectively final

        for (String bouquetId : bouquetIds) {
            db.collection("bouquets")
                    .document(bouquetId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Bouquet bouquet = documentSnapshot.toObject(Bouquet.class);
                            if (bouquet != null) {
                                bouquet.setId(documentSnapshot.getId());
                                favoritesBouquetList.add(bouquet);
                            }
                        }

                        loadedCount[0]++;
                        if (loadedCount[0] == bouquetIds.size()) {
                            // All bouquets loaded
                            binding.progressBar.setVisibility(View.GONE);
                            if (favoritesBouquetList.isEmpty()) {
                                binding.textNoFavorites.setVisibility(View.VISIBLE);
                                binding.textNoFavoritesMessage.setText("No favorite bouquets found");
                            } else {
                                binding.textNoFavorites.setVisibility(View.GONE);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadedCount[0]++;
                        if (loadedCount[0] == bouquetIds.size()) {
                            binding.progressBar.setVisibility(View.GONE);
                            if (favoritesBouquetList.isEmpty()) {
                                binding.textNoFavorites.setVisibility(View.VISIBLE);
                                binding.textNoFavoritesMessage.setText("Error loading some bouquets");
                            } else {
                                binding.textNoFavorites.setVisibility(View.GONE);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
        }
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

    private void onRemoveFromFavorites(Bouquet bouquet) {
        favoriteRepository.removeFromFavorites(bouquet.getId(), new FavoriteRepository.FavoriteCallback() {
            @Override
            public void onSuccess() {
                // Remove from local lists
                favoriteIds.remove(bouquet.getId());
                favoritesBouquetList.remove(bouquet);
                adapter.notifyDataSetChanged();

                // Show empty state if no favorites left
                if (favoritesBouquetList.isEmpty()) {
                    binding.textNoFavorites.setVisibility(View.VISIBLE);
                    binding.textNoFavoritesMessage.setText("No favorite bouquets");
                }

                Toast.makeText(UserFavoritesActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(UserFavoritesActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
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
