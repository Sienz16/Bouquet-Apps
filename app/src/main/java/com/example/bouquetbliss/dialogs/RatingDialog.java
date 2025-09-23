package com.example.bouquetbliss.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.bouquetbliss.R;
import com.example.bouquetbliss.databinding.DialogRateOrderBinding;
import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.models.OrderItem;
import com.example.bouquetbliss.models.Rating;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RatingDialog {
    
    private Context context;
    private AlertDialog dialog;
    private DialogRateOrderBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Order order;
    private OrderItem orderItem;
    private int selectedRating = 0;
    private RatingCallback callback;
    
    private ImageView[] starViews;
    private String[] ratingTexts = {
        "Tap stars to rate",
        "Poor - Very disappointed",
        "Fair - Below expectations", 
        "Good - Meets expectations",
        "Very Good - Exceeds expectations",
        "Excellent - Outstanding!"
    };
    
    public interface RatingCallback {
        void onRatingSubmitted(Rating rating);
        void onRatingCancelled();
    }
    
    public RatingDialog(@NonNull Context context, Order order, OrderItem orderItem, RatingCallback callback) {
        this.context = context;
        this.order = order;
        this.orderItem = orderItem;
        this.callback = callback;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        
        createDialog();
    }
    
    private void createDialog() {
        binding = DialogRateOrderBinding.inflate(LayoutInflater.from(context));
        
        dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot())
                .setCancelable(false)
                .create();
        
        initializeStarViews();
        setupUI();
        setupClickListeners();
    }
    
    private void initializeStarViews() {
        starViews = new ImageView[]{
            binding.star1, binding.star2, binding.star3, binding.star4, binding.star5
        };
    }
    
    private void setupUI() {
        // Set order information
        binding.tvBouquetName.setText(orderItem.getBouquetName());
        binding.tvOrderId.setText("Order: " + order.getFormattedOrderId());
        
        // Load bouquet image
        if (orderItem.getBouquetImageUrl() != null && !orderItem.getBouquetImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(orderItem.getBouquetImageUrl())
                    .placeholder(R.drawable.ic_bouquet)
                    .error(R.drawable.ic_bouquet)
                    .centerCrop()
                    .into(binding.ivBouquetImage);
        }
        
        // Initialize rating text
        binding.tvRatingText.setText(ratingTexts[0]);
        
        // Initially disable submit button
        binding.btnSubmit.setEnabled(false);
    }
    
    private void setupClickListeners() {
        // Star click listeners
        for (int i = 0; i < starViews.length; i++) {
            final int starIndex = i;
            starViews[i].setOnClickListener(v -> setRating(starIndex + 1));
        }
        
        // Button click listeners
        binding.btnCancel.setOnClickListener(v -> {
            if (callback != null) {
                callback.onRatingCancelled();
            }
            dismiss();
        });
        
        binding.btnSubmit.setOnClickListener(v -> submitRating());
    }
    
    private void setRating(int rating) {
        selectedRating = rating;
        
        // Update star visuals
        for (int i = 0; i < starViews.length; i++) {
            if (i < rating) {
                starViews[i].setImageResource(R.drawable.ic_star_filled);
            } else {
                starViews[i].setImageResource(R.drawable.ic_star_outline);
            }
        }
        
        // Update rating text
        binding.tvRatingText.setText(ratingTexts[rating]);
        
        // Enable submit button
        binding.btnSubmit.setEnabled(true);
    }
    
    private void submitRating() {
        if (selectedRating == 0) {
            Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (auth.getCurrentUser() == null) {
            Toast.makeText(context, "You must be logged in to rate", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading state
        binding.btnSubmit.setEnabled(false);
        binding.btnSubmit.setText("Submitting...");
        
        String comment = binding.etComment.getText() != null ? 
            binding.etComment.getText().toString().trim() : "";
        
        // Create rating object
        Rating rating = new Rating(
            order.getId(),
            auth.getCurrentUser().getUid(),
            order.getCustomerName(),
            orderItem.getBouquetId(),
            orderItem.getBouquetName(),
            selectedRating,
            comment
        );
        
        // Save rating to Firestore
        db.collection("ratings")
                .add(rating)
                .addOnSuccessListener(documentReference -> {
                    rating.setId(documentReference.getId());
                    
                    // Mark order as rated
                    order.markAsRated();
                    db.collection("orders").document(order.getId())
                            .set(order)
                            .addOnSuccessListener(aVoid -> {
                                // Update bouquet rating statistics
                                updateBouquetRating(rating);
                            })
                            .addOnFailureListener(e -> {
                                handleSubmissionError("Failed to update order: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    handleSubmissionError("Failed to submit rating: " + e.getMessage());
                });
    }
    
    private void updateBouquetRating(Rating rating) {
        // Get current bouquet ratings
        db.collection("ratings")
                .whereEqualTo("bouquetId", rating.getBouquetId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalStars = 0;
                    int totalRatings = queryDocumentSnapshots.size();
                    
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Rating r = document.toObject(Rating.class);
                        totalStars += r.getStars();
                    }
                    
                    double averageRating = totalRatings > 0 ? totalStars / totalRatings : 0;
                    
                    // Update bouquet with new rating statistics
                    db.collection("bouquets").document(rating.getBouquetId())
                            .update(
                                "averageRating", averageRating,
                                "totalRatings", totalRatings,
                                "updatedAt", System.currentTimeMillis()
                            )
                            .addOnSuccessListener(aVoid -> {
                                handleSubmissionSuccess(rating);
                            })
                            .addOnFailureListener(e -> {
                                // Even if bouquet update fails, rating was still saved
                                handleSubmissionSuccess(rating);
                            });
                })
                .addOnFailureListener(e -> {
                    // Even if we can't update bouquet stats, rating was still saved
                    handleSubmissionSuccess(rating);
                });
    }
    
    private void handleSubmissionSuccess(Rating rating) {
        Toast.makeText(context, "Thank you for your rating!", Toast.LENGTH_SHORT).show();
        
        if (callback != null) {
            callback.onRatingSubmitted(rating);
        }
        
        dismiss();
    }
    
    private void handleSubmissionError(String error) {
        binding.btnSubmit.setEnabled(true);
        binding.btnSubmit.setText("Submit Rating");
        Toast.makeText(context, error, Toast.LENGTH_LONG).show();
    }
    
    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }
    
    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
