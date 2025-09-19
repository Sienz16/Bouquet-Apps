package com.example.bouquetbliss.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.utils.TranslationManager;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying user's order history
 */
public class UserOrderHistoryAdapter extends RecyclerView.Adapter<UserOrderHistoryAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private List<Order> orderList;
    private OnOrderClickListener listener;
    private SimpleDateFormat dateFormat;
    private Context context;
    private TranslationManager translationManager;

    public UserOrderHistoryAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (translationManager == null) {
            translationManager = TranslationManager.getInstance(context);
        }
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_user_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardOrder;
        TextView tvOrderId;
        TextView tvOrderDate;
        TextView tvDeliveryDate;
        TextView tvOrderStatus;
        TextView tvOrderAmount;
        TextView tvItemCount;
        TextView tvStatusDescription;

        ViewHolder(View itemView) {
            super(itemView);
            cardOrder = itemView.findViewById(R.id.cardOrder);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvDeliveryDate = itemView.findViewById(R.id.tvDeliveryDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderAmount = itemView.findViewById(R.id.tvOrderAmount);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            tvStatusDescription = itemView.findViewById(R.id.tvStatusDescription);
        }

        void bind(Order order, OnOrderClickListener listener) {
            tvOrderId.setText(order.getFormattedOrderId());
            tvOrderDate.setText("Ordered: " + dateFormat.format(new Date(order.getOrderDate())));
            tvDeliveryDate.setText("Delivery: " + dateFormat.format(new Date(order.getDeliveryDate())));
            tvOrderAmount.setText(String.format("RM %.2f", order.getTotalAmount()));
            
            // Set item count with translation
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            String itemText = itemCount + " item" + (itemCount != 1 ? "s" : "");
            
            if (translationManager != null) {
                translationManager.translateAndSetText(tvItemCount, itemText);
            } else {
                tvItemCount.setText(itemText);
            }
            
            // Set status and color with translation
            String statusText = getStatusDisplayText(order.getStatus());
            String statusDesc = getStatusDescription(order.getStatus());
            
            if (translationManager != null) {
                translationManager.translateAndSetText(tvOrderStatus, statusText);
                translationManager.translateAndSetText(tvStatusDescription, statusDesc);
            } else {
                tvOrderStatus.setText(statusText);
                tvStatusDescription.setText(statusDesc);
            }
            
            setStatusColor(order.getStatus());
            
            // Set click listener
            cardOrder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }

        private void setStatusColor(String status) {
            int colorRes;
            int backgroundRes;
            
            switch (status) {
                case Order.Status.PENDING:
                    colorRes = R.color.status_limited;
                    backgroundRes = R.color.status_limited_bg;
                    break;
                case Order.Status.APPROVED:
                case Order.Status.IN_PREPARATION:
                    colorRes = R.color.pink_primary;
                    backgroundRes = R.color.pink_lightest;
                    break;
                case Order.Status.READY:
                case Order.Status.COMPLETED:
                    colorRes = R.color.status_available;
                    backgroundRes = R.color.status_available_bg;
                    break;
                case Order.Status.REJECTED:
                    colorRes = R.color.status_out_of_stock;
                    backgroundRes = R.color.status_out_of_stock_bg;
                    break;
                default:
                    colorRes = R.color.text_secondary;
                    backgroundRes = R.color.pink_lightest;
                    break;
            }
            
            tvOrderStatus.setTextColor(context.getColor(colorRes));
            tvOrderStatus.setBackgroundTintList(context.getColorStateList(backgroundRes));
        }

        private String getStatusDisplayText(String status) {
            switch (status) {
                case Order.Status.PENDING:
                    return "Pending Review";
                case Order.Status.APPROVED:
                    return "Approved";
                case Order.Status.IN_PREPARATION:
                    return "Being Prepared";
                case Order.Status.READY:
                    return "Ready for Pickup";
                case Order.Status.COMPLETED:
                    return "Completed";
                case Order.Status.REJECTED:
                    return "Rejected";
                default:
                    return "Unknown";
            }
        }

        private String getStatusDescription(String status) {
            switch (status) {
                case Order.Status.PENDING:
                    return "Your order is being reviewed by our team";
                case Order.Status.APPROVED:
                    return "Your order has been approved and will be prepared soon";
                case Order.Status.IN_PREPARATION:
                    return "Your bouquet is being carefully prepared";
                case Order.Status.READY:
                    return "Your order is ready for pickup!";
                case Order.Status.COMPLETED:
                    return "Thank you for your order!";
                case Order.Status.REJECTED:
                    return "Unfortunately, your order was rejected";
                default:
                    return "";
            }
        }
    }
}
