package com.example.bouquetbliss.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bouquetbliss.R;
import com.example.bouquetbliss.databinding.ItemAdminRatingBinding;
import com.example.bouquetbliss.models.Rating;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminRatingAdapter extends RecyclerView.Adapter<AdminRatingAdapter.RatingViewHolder> {
    
    private List<Rating> ratings;
    private Context context;
    
    public AdminRatingAdapter(List<Rating> ratings, Context context) {
        this.ratings = ratings;
        this.context = context;
    }
    
    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminRatingBinding binding = ItemAdminRatingBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new RatingViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        Rating rating = ratings.get(position);
        holder.bind(rating);
    }
    
    @Override
    public int getItemCount() {
        return ratings.size();
    }
    
    public void updateRatings(List<Rating> newRatings) {
        this.ratings = newRatings;
        notifyDataSetChanged();
    }
    
    class RatingViewHolder extends RecyclerView.ViewHolder {
        private ItemAdminRatingBinding binding;
        
        public RatingViewHolder(ItemAdminRatingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(Rating rating) {
            // Set bouquet name
            binding.tvBouquetName.setText(rating.getBouquetName());
            
            // Set star rating display
            binding.tvStarRating.setText(rating.getStarsDisplay());
            binding.tvRatingNumber.setText("(" + rating.getStars() + ")");
            
            // Set rating date
            String dateText = getRelativeTimeString(rating.getRatedAt());
            binding.tvRatingDate.setText(dateText);
            
            // Set comment if available
            if (rating.getComment() != null && !rating.getComment().trim().isEmpty()) {
                binding.tvRatingComment.setText(rating.getComment());
                binding.tvRatingComment.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvRatingComment.setVisibility(android.view.View.GONE);
            }
            
            // Load bouquet image (placeholder for now since we don't have image URL in rating)
            binding.ivBouquetImage.setImageResource(R.drawable.ic_bouquet);
            
            // Could enhance to load actual bouquet image by fetching from bouquet collection
            // loadBouquetImage(rating.getBouquetId());
        }
        
        private String getRelativeTimeString(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            
            if (days > 0) {
                if (days == 1) {
                    return "1 day ago";
                } else if (days < 7) {
                    return days + " days ago";
                } else {
                    // Show actual date for older ratings
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    return dateFormat.format(new Date(timestamp));
                }
            } else if (hours > 0) {
                return hours == 1 ? "1 hour ago" : hours + " hours ago";
            } else if (minutes > 0) {
                return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
            } else {
                return "Just now";
            }
        }
        
        // Optional: Load actual bouquet image
        private void loadBouquetImage(String bouquetId) {
            // This would require fetching bouquet data from Firestore
            // For now, using placeholder
        }
    }
}
