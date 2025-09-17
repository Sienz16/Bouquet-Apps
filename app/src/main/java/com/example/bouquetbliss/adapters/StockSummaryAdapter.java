package com.example.bouquetbliss.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.models.Bouquet;
import com.example.bouquetbliss.utils.LocalImageManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for stock summary and review functionality
 */
public class StockSummaryAdapter extends RecyclerView.Adapter<StockSummaryAdapter.ViewHolder> {

    private List<Bouquet> bouquetList;
    private OnStockSummaryActionListener listener;
    private NumberFormat currencyFormat;
    private Context context;

    public interface OnStockSummaryActionListener {
        void onViewDetails(Bouquet bouquet);
        void onViewTransactions(Bouquet bouquet);
    }

    public StockSummaryAdapter(List<Bouquet> bouquetList, OnStockSummaryActionListener listener) {
        this.bouquetList = bouquetList;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "MY"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_stock_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bouquet bouquet = bouquetList.get(position);
        holder.bind(bouquet);
    }

    @Override
    public int getItemCount() {
        return bouquetList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardBouquet;
        ImageView ivBouquetImage;
        TextView tvBouquetName;
        TextView tvBouquetCategory;
        TextView tvCurrentStock;
        TextView tvStockValue;
        TextView tvStockStatus;
        TextView tvLastUpdated;
        View stockStatusIndicator;
        MaterialButton btnViewDetails;
        MaterialButton btnViewTransactions;

        ViewHolder(View itemView) {
            super(itemView);
            cardBouquet = itemView.findViewById(R.id.cardBouquet);
            ivBouquetImage = itemView.findViewById(R.id.ivBouquetImage);
            tvBouquetName = itemView.findViewById(R.id.tvBouquetName);
            tvBouquetCategory = itemView.findViewById(R.id.tvBouquetCategory);
            tvCurrentStock = itemView.findViewById(R.id.tvCurrentStock);
            tvStockValue = itemView.findViewById(R.id.tvStockValue);
            tvStockStatus = itemView.findViewById(R.id.tvStockStatus);
            tvLastUpdated = itemView.findViewById(R.id.tvLastUpdated);
            stockStatusIndicator = itemView.findViewById(R.id.stockStatusIndicator);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnViewTransactions = itemView.findViewById(R.id.btnViewTransactions);
        }

        void bind(Bouquet bouquet) {
            // Basic information
            tvBouquetName.setText(bouquet.getName());
            tvBouquetCategory.setText(bouquet.getCategory());
            tvCurrentStock.setText(String.valueOf(bouquet.getQuantity()));
            
            // Calculate stock value
            double stockValue = bouquet.getPrice() * bouquet.getQuantity();
            tvStockValue.setText(currencyFormat.format(stockValue));
            
            tvStockStatus.setText(bouquet.getAvailability());
            
            // Format last updated time
            if (bouquet.getUpdatedAt() > 0) {
                long timeDiff = System.currentTimeMillis() - bouquet.getUpdatedAt();
                String timeAgo = formatTimeAgo(timeDiff);
                tvLastUpdated.setText("Updated " + timeAgo);
            } else {
                tvLastUpdated.setText("Never updated");
            }

            // Set stock status styling
            setupStockStatusStyling(bouquet);

            // Load bouquet image
            loadBouquetImage(bouquet);

            // Setup click listeners
            setupClickListeners(bouquet);
        }

        private void setupStockStatusStyling(Bouquet bouquet) {
            int statusColor;
            int backgroundColor;
            
            switch (bouquet.getAvailability()) {
                case "Available":
                    statusColor = context.getColor(R.color.status_available);
                    backgroundColor = context.getColor(R.color.status_available_bg);
                    break;
                case "Limited":
                    statusColor = context.getColor(R.color.status_limited);
                    backgroundColor = context.getColor(R.color.status_limited_bg);
                    break;
                case "Out of Stock":
                    statusColor = context.getColor(R.color.status_out_of_stock);
                    backgroundColor = context.getColor(R.color.status_out_of_stock_bg);
                    break;
                default:
                    statusColor = context.getColor(R.color.text_secondary);
                    backgroundColor = context.getColor(R.color.background_secondary);
                    break;
            }
            
            tvStockStatus.setTextColor(statusColor);
            tvStockStatus.setBackgroundColor(backgroundColor);
            stockStatusIndicator.setBackgroundColor(statusColor);
            
            // Highlight critical stock levels
            if (bouquet.getQuantity() <= 0) {
                cardBouquet.setStrokeColor(context.getColor(R.color.status_out_of_stock));
                cardBouquet.setStrokeWidth(3);
            } else if (bouquet.getQuantity() <= 5) {
                cardBouquet.setStrokeColor(context.getColor(R.color.status_limited));
                cardBouquet.setStrokeWidth(2);
            } else {
                cardBouquet.setStrokeWidth(0);
            }
        }

        private void loadBouquetImage(Bouquet bouquet) {
            if (bouquet.getImageUrls() != null && !bouquet.getImageUrls().isEmpty()) {
                String imagePath = bouquet.getImageUrls().get(0);
                LocalImageManager.loadLocalImage(context, imagePath, ivBouquetImage);
            } else {
                ivBouquetImage.setImageResource(R.drawable.circular_logo_placeholder);
            }
        }

        private void setupClickListeners(Bouquet bouquet) {
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(bouquet);
                }
            });

            btnViewTransactions.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewTransactions(bouquet);
                }
            });

            cardBouquet.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(bouquet);
                }
            });
        }

        private String formatTimeAgo(long timeDiff) {
            if (timeDiff < 60000) { // Less than 1 minute
                return "just now";
            } else if (timeDiff < 3600000) { // Less than 1 hour
                long minutes = timeDiff / 60000;
                return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
            } else if (timeDiff < 86400000) { // Less than 1 day
                long hours = timeDiff / 3600000;
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else if (timeDiff < 604800000) { // Less than 1 week
                long days = timeDiff / 86400000;
                return days + " day" + (days > 1 ? "s" : "") + " ago";
            } else {
                long weeks = timeDiff / 604800000;
                return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
            }
        }
    }
}
