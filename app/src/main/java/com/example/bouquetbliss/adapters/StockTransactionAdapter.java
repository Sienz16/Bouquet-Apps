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
import com.example.bouquetbliss.models.StockTransaction;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying stock transaction history
 */
public class StockTransactionAdapter extends RecyclerView.Adapter<StockTransactionAdapter.ViewHolder> {

    private List<StockTransaction> transactionList;
    private Context context;

    public StockTransactionAdapter(List<StockTransaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_stock_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockTransaction transaction = transactionList.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardTransaction;
        ImageView ivTransactionIcon;
        TextView tvTransactionType;
        TextView tvTransactionDate;
        TextView tvQuantityChange;
        TextView tvStockBefore;
        TextView tvStockAfter;
        TextView tvAdminName;
        TextView tvReason;
        View quantityIndicator;

        ViewHolder(View itemView) {
            super(itemView);
            cardTransaction = itemView.findViewById(R.id.cardTransaction);
            ivTransactionIcon = itemView.findViewById(R.id.ivTransactionIcon);
            tvTransactionType = itemView.findViewById(R.id.tvTransactionType);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvQuantityChange = itemView.findViewById(R.id.tvQuantityChange);
            tvStockBefore = itemView.findViewById(R.id.tvStockBefore);
            tvStockAfter = itemView.findViewById(R.id.tvStockAfter);
            tvAdminName = itemView.findViewById(R.id.tvAdminName);
            tvReason = itemView.findViewById(R.id.tvReason);
            quantityIndicator = itemView.findViewById(R.id.quantityIndicator);
        }

        void bind(StockTransaction transaction) {
            // Set transaction type and icon
            setupTransactionTypeAndIcon(transaction);
            
            // Format and set date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date transactionDate = new Date(transaction.getTimestamp());
            
            tvTransactionDate.setText(dateFormat.format(transactionDate));
            
            // Set quantity change with proper formatting
            setupQuantityDisplay(transaction);
            
            // Set before and after stock levels
            tvStockBefore.setText(String.valueOf(transaction.getPreviousQuantity()));
            tvStockAfter.setText(String.valueOf(transaction.getNewQuantity()));
            
            // Set admin name
            String adminName = transaction.getAdminName();
            if (adminName == null || adminName.trim().isEmpty()) {
                adminName = "System";
            }
            tvAdminName.setText("By: " + adminName);
            
            // Set reason
            String reason = transaction.getReason();
            if (reason != null && !reason.trim().isEmpty()) {
                tvReason.setText(reason);
                tvReason.setVisibility(View.VISIBLE);
            } else {
                tvReason.setVisibility(View.GONE);
            }
            
            // Add time to admin info
            tvAdminName.setText(tvAdminName.getText() + " • " + timeFormat.format(transactionDate));
        }

        private void setupTransactionTypeAndIcon(StockTransaction transaction) {
            int iconResource;
            int iconTint;
            String typeText;
            
            switch (transaction.getType()) {
                case STOCK_IN:
                    iconResource = R.drawable.ic_add_circle;
                    iconTint = context.getColor(R.color.status_available);
                    typeText = "Stock Received";
                    break;
                case STOCK_OUT:
                    iconResource = R.drawable.ic_remove_circle;
                    iconTint = context.getColor(R.color.status_out_of_stock);
                    typeText = "Stock Removed";
                    break;
                case ADJUSTMENT:
                    iconResource = R.drawable.ic_edit;
                    iconTint = context.getColor(R.color.status_limited);
                    typeText = "Stock Adjusted";
                    break;
                case ORDER_FULFILLMENT:
                    iconResource = R.drawable.ic_shopping_cart;
                    iconTint = context.getColor(R.color.pink_primary);
                    typeText = "Order Fulfilled";
                    break;
                case DAMAGED:
                    iconResource = R.drawable.ic_warning;
                    iconTint = context.getColor(R.color.status_out_of_stock);
                    typeText = "Damaged/Lost";
                    break;
                case EXPIRED:
                    iconResource = R.drawable.ic_schedule;
                    iconTint = context.getColor(R.color.status_limited);
                    typeText = "Expired";
                    break;
                default:
                    iconResource = R.drawable.ic_info;
                    iconTint = context.getColor(R.color.text_secondary);
                    typeText = "Unknown";
                    break;
            }
            
            ivTransactionIcon.setImageResource(iconResource);
            ivTransactionIcon.setColorFilter(iconTint);
            tvTransactionType.setText(typeText);
        }

        private void setupQuantityDisplay(StockTransaction transaction) {
            int quantityChange = transaction.getQuantityChange();
            String changeText;
            int changeColor;
            int indicatorColor;
            
            if (quantityChange > 0) {
                changeText = "+" + quantityChange;
                changeColor = context.getColor(R.color.status_available);
                indicatorColor = context.getColor(R.color.status_available);
            } else if (quantityChange < 0) {
                changeText = String.valueOf(quantityChange); // Already has minus sign
                changeColor = context.getColor(R.color.status_out_of_stock);
                indicatorColor = context.getColor(R.color.status_out_of_stock);
            } else {
                changeText = "0";
                changeColor = context.getColor(R.color.text_secondary);
                indicatorColor = context.getColor(R.color.text_secondary);
            }
            
            tvQuantityChange.setText(changeText);
            tvQuantityChange.setTextColor(changeColor);
            quantityIndicator.setBackgroundColor(indicatorColor);
        }
    }
}
