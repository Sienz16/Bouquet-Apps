package com.example.bouquetbliss.adapters;

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
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BouquetStockAdapter extends RecyclerView.Adapter<BouquetStockAdapter.ViewHolder> {

    private List<Bouquet> bouquetList;
    private OnBouquetActionListener listener;
    private NumberFormat currencyFormat;

    public interface OnBouquetActionListener {
        void onEditBouquet(Bouquet bouquet);
        void onDeleteBouquet(Bouquet bouquet);
        void onViewBouquet(Bouquet bouquet);
    }

    public BouquetStockAdapter(List<Bouquet> bouquetList, OnBouquetActionListener listener) {
        this.bouquetList = bouquetList;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "MY"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_bouquet_stock, parent, false);
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
        TextView tvBouquetStock;
        TextView tvAvailabilityStatus;
        ImageView btnEdit;
        ImageView btnDelete;
        View stockIndicator;

        ViewHolder(View itemView) {
            super(itemView);
            cardBouquet = itemView.findViewById(R.id.cardBouquet);
            ivBouquetImage = itemView.findViewById(R.id.ivBouquetImage);
            tvBouquetName = itemView.findViewById(R.id.tvBouquetName);
            tvBouquetCategory = itemView.findViewById(R.id.tvBouquetCategory);
            tvBouquetPrice = itemView.findViewById(R.id.tvBouquetPrice);
            tvBouquetStock = itemView.findViewById(R.id.tvBouquetStock);
            tvAvailabilityStatus = itemView.findViewById(R.id.tvAvailabilityStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            stockIndicator = itemView.findViewById(R.id.stockIndicator);
        }

        void bind(Bouquet bouquet) {
            tvBouquetName.setText(bouquet.getName());
            tvBouquetCategory.setText(bouquet.getCategory());
            tvBouquetPrice.setText(currencyFormat.format(bouquet.getPrice()));
            tvBouquetStock.setText("Stock: " + bouquet.getQuantity());
            tvAvailabilityStatus.setText(bouquet.getAvailability());

            // Set availability status color
            int statusColor;
            switch (bouquet.getAvailability()) {
                case "Available":
                    statusColor = itemView.getContext().getColor(R.color.pink_primary);
                    break;
                case "Limited":
                    statusColor = itemView.getContext().getColor(R.color.pink_medium);
                    break;
                case "Out of Stock":
                    statusColor = itemView.getContext().getColor(R.color.text_secondary);
                    break;
                default:
                    statusColor = itemView.getContext().getColor(R.color.text_secondary);
                    break;
            }
            tvAvailabilityStatus.setTextColor(statusColor);
            stockIndicator.setBackgroundColor(statusColor);

            // Load local image if available
            if (bouquet.getImageUrls() != null && !bouquet.getImageUrls().isEmpty()) {
                String imagePath = bouquet.getImageUrls().get(0);
                LocalImageManager.loadLocalImage(itemView.getContext(), imagePath, ivBouquetImage);
            } else {
                ivBouquetImage.setImageResource(R.drawable.circular_logo_placeholder);
            }

            // Click listeners
            cardBouquet.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewBouquet(bouquet);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditBouquet(bouquet);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteBouquet(bouquet);
                }
            });
        }
    }
}
