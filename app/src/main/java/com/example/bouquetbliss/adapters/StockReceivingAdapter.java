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
 * Adapter for stock receiving and management functionality
 */
public class StockReceivingAdapter extends RecyclerView.Adapter<StockReceivingAdapter.ViewHolder> {

    private List<Bouquet> bouquetList;
    private OnStockActionListener listener;
    private NumberFormat currencyFormat;
    private Context context;

    public interface OnStockActionListener {
        void onReceiveStock(Bouquet bouquet);
        void onAdjustStock(Bouquet bouquet);
        void onViewStockHistory(Bouquet bouquet);
    }

    public StockReceivingAdapter(List<Bouquet> bouquetList, OnStockActionListener listener) {
        this.bouquetList = bouquetList;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "MY"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_stock_receiving, parent, false);
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
        TextView tvBouquetPrice;
        TextView tvCurrentStock;
        TextView tvStockStatus;
        View stockStatusIndicator;
        MaterialButton btnReceive;
        MaterialButton btnAdjust;
        MaterialButton btnHistory;

        ViewHolder(View itemView) {
            super(itemView);
            cardBouquet = itemView.findViewById(R.id.cardBouquet);
            ivBouquetImage = itemView.findViewById(R.id.ivBouquetImage);
            tvBouquetName = itemView.findViewById(R.id.tvBouquetName);
            tvBouquetCategory = itemView.findViewById(R.id.tvBouquetCategory);
            tvBouquetPrice = itemView.findViewById(R.id.tvBouquetPrice);
            tvCurrentStock = itemView.findViewById(R.id.tvCurrentStock);
            tvStockStatus = itemView.findViewById(R.id.tvStockStatus);
            stockStatusIndicator = itemView.findViewById(R.id.stockStatusIndicator);
            btnReceive = itemView.findViewById(R.id.btnReceive);
            btnAdjust = itemView.findViewById(R.id.btnAdjust);
            btnHistory = itemView.findViewById(R.id.btnHistory);
        }

        void bind(Bouquet bouquet) {
            // Basic information
            tvBouquetName.setText(bouquet.getName());
            tvBouquetCategory.setText(bouquet.getCategory());
            tvBouquetPrice.setText(currencyFormat.format(bouquet.getPrice()));
            tvCurrentStock.setText(String.valueOf(bouquet.getQuantity()));
            tvStockStatus.setText(bouquet.getAvailability());

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
            
            // Highlight low stock and out of stock items
            if (bouquet.getQuantity() <= 0) {
                cardBouquet.setStrokeColor(context.getColor(R.color.status_out_of_stock));
                cardBouquet.setStrokeWidth(4);
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
            btnReceive.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReceiveStock(bouquet);
                }
            });

            btnAdjust.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAdjustStock(bouquet);
                }
            });

            btnHistory.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewStockHistory(bouquet);
                }
            });

            // Card click for quick receive (if out of stock or low stock)
            cardBouquet.setOnClickListener(v -> {
                if (bouquet.getQuantity() <= 5 && listener != null) {
                    listener.onReceiveStock(bouquet);
                }
            });
        }
    }
}
