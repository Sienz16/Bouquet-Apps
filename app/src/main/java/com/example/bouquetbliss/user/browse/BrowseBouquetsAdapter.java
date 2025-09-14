package com.example.bouquetbliss.user.browse;

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
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class BrowseBouquetsAdapter extends RecyclerView.Adapter<BrowseBouquetsAdapter.BouquetViewHolder> {

    private List<Bouquet> bouquetList;
    private OnBouquetClickListener listener;

    public interface OnBouquetClickListener {
        void onBouquetClick(Bouquet bouquet);
    }

    public BrowseBouquetsAdapter(List<Bouquet> bouquetList, OnBouquetClickListener listener) {
        this.bouquetList = bouquetList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BouquetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_browse_bouquet, parent, false);
        return new BouquetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BouquetViewHolder holder, int position) {
        Bouquet bouquet = bouquetList.get(position);
        holder.bind(bouquet, listener);
    }

    @Override
    public int getItemCount() {
        return bouquetList.size();
    }

    static class BouquetViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardBouquet;
        private ImageView imageBouquet;
        private TextView textName;
        private TextView textPrice;
        private TextView textStock;

        public BouquetViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBouquet = itemView.findViewById(R.id.cardBouquet);
            imageBouquet = itemView.findViewById(R.id.imageBouquet);
            textName = itemView.findViewById(R.id.textName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textStock = itemView.findViewById(R.id.textStock);
        }

        public void bind(Bouquet bouquet, OnBouquetClickListener listener) {
            textName.setText(bouquet.getName());
            textPrice.setText(String.format("RM %.2f", bouquet.getPrice()));
            
            // Set stock status
            if (bouquet.getQuantity() > 0) {
                textStock.setText(String.format("%d in stock", bouquet.getQuantity()));
                textStock.setTextColor(itemView.getContext().getResources().getColor(R.color.success));
            } else {
                textStock.setText("Out of stock");
                textStock.setTextColor(itemView.getContext().getResources().getColor(R.color.error));
            }

            // Set click listener
            cardBouquet.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBouquetClick(bouquet);
                }
            });

            // TODO: Load image from URL using Glide or Picasso
            // For now, set a placeholder
            imageBouquet.setImageResource(R.drawable.image_placeholder_background);
        }
    }
}
