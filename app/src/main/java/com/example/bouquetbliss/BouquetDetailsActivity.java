package com.example.bouquetbliss;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bouquetbliss.admin.stock.AddEditBouquetActivity;
import com.example.bouquetbliss.databinding.ActivityCommonBouquetDetailsBinding;
import com.example.bouquetbliss.models.Bouquet;
import com.example.bouquetbliss.utils.LocalImageManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BouquetDetailsActivity extends AppCompatActivity {

    private ActivityCommonBouquetDetailsBinding binding;
    private FirebaseFirestore db;
    private String bouquetId;
    private Bouquet currentBouquet;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommonBouquetDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        bouquetId = getIntent().getStringExtra("bouquet_id");
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "MY"));
        dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

        setupClickListeners();
        
        if (bouquetId != null) {
            loadBouquetDetails();
        } else {
            showError("Invalid bouquet ID");
        }
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnEdit.setOnClickListener(v -> {
            if (currentBouquet != null) {
                Intent intent = new Intent(this, AddEditBouquetActivity.class);
                intent.putExtra("bouquet_id", bouquetId);
                intent.putExtra("edit_mode", true);
                startActivityForResult(intent, 101);
            }
        });
    }

    private void loadBouquetDetails() {
        showLoading(true);
        
        db.collection("bouquets").document(bouquetId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    if (documentSnapshot.exists()) {
                        currentBouquet = documentSnapshot.toObject(Bouquet.class);
                        if (currentBouquet != null) {
                            currentBouquet.setId(documentSnapshot.getId());
                            populateBouquetData();
                        } else {
                            showError("Failed to parse bouquet data");
                        }
                    } else {
                        showError("Bouquet not found");
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showError("Failed to load bouquet: " + e.getMessage());
                });
    }

    private void populateBouquetData() {
        // Header
        binding.tvBouquetNameHeader.setText(currentBouquet.getName());
        
        // Basic Information
        binding.tvBouquetName.setText(currentBouquet.getName());
        binding.tvBouquetDescription.setText(currentBouquet.getDescription());
        binding.tvBouquetPrice.setText(currencyFormat.format(currentBouquet.getPrice()));
        binding.tvBouquetStock.setText(currentBouquet.getQuantity() + " units");
        
        // Status with color
        binding.tvAvailabilityStatus.setText(currentBouquet.getAvailability());
        setStatusColor(currentBouquet.getAvailability());
        
        // Properties
        binding.tvBouquetCategory.setText(currentBouquet.getCategory() != null ? 
            currentBouquet.getCategory() : "Not specified");
        binding.tvBouquetSize.setText(currentBouquet.getSize() != null ? 
            currentBouquet.getSize() : "Not specified");
        binding.tvBouquetOccasion.setText(currentBouquet.getOccasion() != null && 
            !currentBouquet.getOccasion().isEmpty() ? 
            currentBouquet.getOccasion() : "Not specified");
        
        // Flower Types
        if (currentBouquet.getFlowerTypes() != null && !currentBouquet.getFlowerTypes().isEmpty()) {
            binding.tvFlowerTypes.setText(String.join(", ", currentBouquet.getFlowerTypes()));
        } else {
            binding.tvFlowerTypes.setText("Not specified");
        }
        
        // Timestamps
        if (currentBouquet.getCreatedAt() > 0) {
            binding.tvCreatedDate.setText(dateFormat.format(new Date(currentBouquet.getCreatedAt())));
        } else {
            binding.tvCreatedDate.setText("Not available");
        }
        
        if (currentBouquet.getUpdatedAt() > 0) {
            binding.tvUpdatedDate.setText(dateFormat.format(new Date(currentBouquet.getUpdatedAt())));
        } else {
            binding.tvUpdatedDate.setText("Not available");
        }
        
        // Load local image if available
        if (currentBouquet.getImageUrls() != null && !currentBouquet.getImageUrls().isEmpty()) {
            String imagePath = currentBouquet.getImageUrls().get(0);
            LocalImageManager.loadLocalImage(this, imagePath, binding.ivBouquetImage);
        } else {
            binding.ivBouquetImage.setImageResource(R.drawable.circular_logo_placeholder);
        }
        
        showContent(true);
    }

    private void setStatusColor(String availability) {
        int statusColor;
        switch (availability) {
            case "Available":
                statusColor = getColor(R.color.pink_primary);
                break;
            case "Limited":
                statusColor = getColor(R.color.pink_medium);
                break;
            case "Out of Stock":
                statusColor = getColor(R.color.text_secondary);
                break;
            default:
                statusColor = getColor(R.color.text_secondary);
                break;
        }
        binding.tvAvailabilityStatus.setTextColor(statusColor);
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.layoutContent.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
    }

    private void showContent(boolean show) {
        binding.layoutContent.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.progressBar.setVisibility(View.GONE);
        binding.layoutError.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        binding.layoutContent.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            // Refresh the bouquet details after editing
            loadBouquetDetails();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
