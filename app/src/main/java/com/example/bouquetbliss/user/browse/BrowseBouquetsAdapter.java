package com.example.bouquetbliss.user.browse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.models.Bouquet;
import com.example.bouquetbliss.repository.FavoriteRepository;
import com.google.android.material.card.MaterialCardView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BrowseBouquetsAdapter extends RecyclerView.Adapter<BrowseBouquetsAdapter.BouquetViewHolder> {

    private List<Bouquet> bouquetList;
    private OnBouquetClickListener listener;
    private FavoriteRepository favoriteRepository;
    private Set<String> favoriteIds;

    public interface OnBouquetClickListener {
        void onBouquetClick(Bouquet bouquet);
    }

    public BrowseBouquetsAdapter(List<Bouquet> bouquetList, OnBouquetClickListener listener) {
        this.bouquetList = bouquetList;
        this.listener = listener;
        this.favoriteRepository = new FavoriteRepository();
        this.favoriteIds = new HashSet<>();
    }

    public void updateFavorites(Set<String> favoriteIds) {
        this.favoriteIds = favoriteIds;
        notifyDataSetChanged();
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
        holder.bind(bouquet, listener, favoriteRepository, favoriteIds);
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
        private MaterialCardView cardFavorite;
        private ImageView iconFavorite;

        public BouquetViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBouquet = itemView.findViewById(R.id.cardBouquet);
            imageBouquet = itemView.findViewById(R.id.imageBouquet);
            textName = itemView.findViewById(R.id.textName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textStock = itemView.findViewById(R.id.textStock);
            cardFavorite = itemView.findViewById(R.id.cardFavorite);
            iconFavorite = itemView.findViewById(R.id.iconFavorite);
        }

        public void bind(Bouquet bouquet, OnBouquetClickListener listener, FavoriteRepository favoriteRepository, Set<String> favoriteIds) {
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
                if (listener != null) {
                    listener.onBouquetClick(bouquet);
                }
            });

            // Set favorite status
            boolean isFavorite = favoriteIds.contains(bouquet.getId());
            updateFavoriteIcon(isFavorite);

            // Set click listener for favorite button
            cardFavorite.setOnClickListener(v -> {
                favoriteRepository.toggleFavorite(bouquet.getId(), new FavoriteRepository.FavoriteCallback() {
                    @Override
                    public void onSuccess() {
                        boolean newFavoriteStatus = !isFavorite;
                        if (newFavoriteStatus) {
                            favoriteIds.add(bouquet.getId());
                            Toast.makeText(itemView.getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            favoriteIds.remove(bouquet.getId());
                            Toast.makeText(itemView.getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                        }
                        updateFavoriteIcon(newFavoriteStatus);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(itemView.getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            });

            // TODO: Load image from URL using Glide or Picasso
            // For now, set a placeholder
            imageBouquet.setImageResource(R.drawable.image_placeholder_background);
        }

        private void updateFavoriteIcon(boolean isFavorite) {
            if (isFavorite) {
                iconFavorite.setImageResource(android.R.drawable.btn_star_big_on);
                iconFavorite.setColorFilter(itemView.getContext().getResources().getColor(R.color.pink_primary));
            } else {
                iconFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                iconFavorite.setColorFilter(itemView.getContext().getResources().getColor(R.color.text_secondary));
            }
        }
    }
}
