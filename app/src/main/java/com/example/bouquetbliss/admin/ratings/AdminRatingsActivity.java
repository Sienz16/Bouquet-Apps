package com.example.bouquetbliss.admin.ratings;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bouquetbliss.adapters.AdminRatingAdapter;
import com.example.bouquetbliss.admin.base.BaseAdminNavigationActivity;
import com.example.bouquetbliss.databinding.ActivityAdminRatingsBinding;
import com.example.bouquetbliss.models.Rating;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminRatingsActivity extends BaseAdminNavigationActivity {

    private ActivityAdminRatingsBinding binding;
    private FirebaseFirestore db;
    private AdminRatingAdapter adapter;
    private List<Rating> ratingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminRatingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        ratingsList = new ArrayList<>();

        setupViews();
        setupRecyclerView();
        setupClickListeners();
        setupAdminBottomNavigation();
        setSelectedNavItem("home");
        
        loadAllRatings();
    }

    private void setupViews() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.recyclerViewRatings.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        adapter = new AdminRatingAdapter(ratingsList, this);
        binding.recyclerViewRatings.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewRatings.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnFilter.setOnClickListener(v -> {
            // TODO: Implement rating filters (by stars, date, etc.)
            Toast.makeText(this, "Filter options coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadAllRatings() {
        db.collection("ratings")
                .orderBy("ratedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ratingsList.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Rating rating = document.toObject(Rating.class);
                        rating.setId(document.getId());
                        ratingsList.add(rating);
                    }
                    
                    binding.progressBar.setVisibility(View.GONE);
                    
                    if (ratingsList.isEmpty()) {
                        showEmptyState();
                    } else {
                        showRatingsList();
                        updateStatistics();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    showEmptyState();
                    Toast.makeText(this, "Failed to load ratings: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }

    private void showRatingsList() {
        binding.recyclerViewRatings.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        adapter.updateRatings(ratingsList);
    }

    private void showEmptyState() {
        binding.recyclerViewRatings.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void updateStatistics() {
        int totalRatings = ratingsList.size();
        double totalStars = 0;
        int[] starCounts = new int[6]; // Index 0 unused, 1-5 for star counts
        
        for (Rating rating : ratingsList) {
            totalStars += rating.getStars();
            starCounts[rating.getStars()]++;
        }
        
        double averageRating = totalRatings > 0 ? totalStars / totalRatings : 0;
        
        // Update statistics display
        binding.tvTotalRatings.setText(String.valueOf(totalRatings));
        binding.tvAverageRating.setText(String.format("%.1f", averageRating));
        
        // Update star distribution
        binding.tv5Stars.setText(String.valueOf(starCounts[5]));
        binding.tv4Stars.setText(String.valueOf(starCounts[4]));
        binding.tv3Stars.setText(String.valueOf(starCounts[3]));
        binding.tv2Stars.setText(String.valueOf(starCounts[2]));
        binding.tv1Star.setText(String.valueOf(starCounts[1]));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
