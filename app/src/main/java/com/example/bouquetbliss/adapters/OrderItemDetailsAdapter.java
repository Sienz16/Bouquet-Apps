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
import com.example.bouquetbliss.models.OrderItem;
import com.example.bouquetbliss.utils.LocalImageManager;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying order items in detail view
 */
public class OrderItemDetailsAdapter extends RecyclerView.Adapter<OrderItemDetailsAdapter.ViewHolder> {

    private List<OrderItem> orderItems;
    private NumberFormat currencyFormat;
    private Context context;

    public OrderItemDetailsAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "MY"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardItem;
        ImageView ivBouquetImage;
        TextView tvBouquetName;
        TextView tvUnitPrice;
        TextView tvQuantity;
        TextView tvSubtotal;

        ViewHolder(View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.cardItem);
            ivBouquetImage = itemView.findViewById(R.id.ivBouquetImage);
            tvBouquetName = itemView.findViewById(R.id.tvBouquetName);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
        }

        void bind(OrderItem item) {
            tvBouquetName.setText(item.getBouquetName());
            tvUnitPrice.setText(currencyFormat.format(item.getUnitPrice()));
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            tvSubtotal.setText(currencyFormat.format(item.getSubtotal()));

            // Load bouquet image
            if (item.getBouquetImageUrl() != null && !item.getBouquetImageUrl().isEmpty()) {
                LocalImageManager.loadLocalImage(context, item.getBouquetImageUrl(), ivBouquetImage);
            } else {
                ivBouquetImage.setImageResource(R.drawable.circular_logo_placeholder);
            }
        }
    }
}
