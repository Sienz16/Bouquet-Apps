package com.example.bouquetbliss.user.favorites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.models.Bouquet;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private List<Bouquet> favoritesList;
    private OnBouquetClickListener clickListener;
    private OnRemoveFromFavoritesListener removeListener;

    public interface OnBouquetClickListener {
        void onBouquetClick(Bouquet bouquet);
    }

    public interface OnRemoveFromFavoritesListener {
        void onRemoveFromFavorites(Bouquet bouquet);
    }

    public FavoritesAdapter(List<Bouquet> favoritesList, OnBouquetClickListener clickListener, OnRemoveFromFavoritesListener removeListener) {
        this.favoritesList = favoritesList;
        this.clickListener = clickListener;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_favorite_bouquet, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Bouquet bouquet = favoritesList.get(position);
        holder.bind(bouquet, clickListener, removeListener);
    }

    @Override
    public int getItemCount() {
        return favoritesList.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardBouquet;
        private ImageView imageBouquet;
        private TextView textName;
        private TextView textPrice;
        private TextView textStock;
        private MaterialCardView cardRemove;
        private ImageView iconRemove;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBouquet = itemView.findViewById(R.id.cardBouquet);
            imageBouquet = itemView.findViewById(R.id.imageBouquet);
            textName = itemView.findViewById(R.id.textName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textStock = itemView.findViewById(R.id.textStock);
            cardRemove = itemView.findViewById(R.id.cardRemove);
            iconRemove = itemView.findViewById(R.id.iconRemove);
        }

        public void bind(Bouquet bouquet, OnBouquetClickListener clickListener, OnRemoveFromFavoritesListener removeListener) {
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

            // Set click listener for bouquet card
            cardBouquet.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onBouquetClick(bouquet);
                }
            });

            // Set click listener for remove button
            cardRemove.setOnClickListener(v -> {
                new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Remove from Favorites")
                        .setMessage("Are you sure you want to remove \"" + bouquet.getName() + "\" from your favorites?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            if (removeListener != null) {
                                removeListener.onRemoveFromFavorites(bouquet);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            // TODO: Load image from URL using Glide or Picasso
            // For now, set a placeholder
            imageBouquet.setImageResource(R.drawable.image_placeholder_background);
        }
    }
}
