package com.example.bouquetbliss.user.orders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.models.OrderItem;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserOrdersAdapter extends RecyclerView.Adapter<UserOrdersAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OnOrderClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public UserOrdersAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardOrder;
        private TextView textOrderId;
        private TextView textOrderDate;
        private TextView textOrderStatus;
        private TextView textOrderItems;
        private TextView textOrderTotal;
        private ImageView iconStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardOrder = itemView.findViewById(R.id.cardOrder);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textOrderDate = itemView.findViewById(R.id.textOrderDate);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
            textOrderItems = itemView.findViewById(R.id.textOrderItems);
            textOrderTotal = itemView.findViewById(R.id.textOrderTotal);
            iconStatus = itemView.findViewById(R.id.iconStatus);
        }

        public void bind(Order order, OnOrderClickListener listener) {
            Context context = itemView.getContext();
            
            // Set order ID
            textOrderId.setText("Order " + order.getFormattedOrderId());
            
            // Set order date
            Date orderDate = new Date(order.getCreatedAt());
            textOrderDate.setText(dateFormat.format(orderDate));
            
            // Set order status with appropriate styling
            setOrderStatus(order.getStatus(), context);
            
            // Set order items summary
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                StringBuilder itemsText = new StringBuilder();
                for (int i = 0; i < order.getItems().size(); i++) {
                    OrderItem item = order.getItems().get(i);
                    if (i > 0) itemsText.append(", ");
                    itemsText.append(item.getBouquetName()).append(" (").append(item.getQuantity()).append(")");
                }
                textOrderItems.setText(itemsText.toString());
            } else {
                textOrderItems.setText("No items");
            }
            
            // Set total amount
            textOrderTotal.setText(String.format("RM %.2f", order.getTotalAmount()));
            
            // Set click listener
            cardOrder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }

        private void setOrderStatus(String status, Context context) {
            switch (status.toLowerCase()) {
                case "pending":
                    textOrderStatus.setText("Pending Review");
                    textOrderStatus.setTextColor(context.getResources().getColor(R.color.warning));
                    iconStatus.setImageResource(android.R.drawable.ic_menu_recent_history);
                    iconStatus.setColorFilter(context.getResources().getColor(R.color.warning));
                    break;
                case "approved":
                    textOrderStatus.setText("Approved");
                    textOrderStatus.setTextColor(context.getResources().getColor(R.color.info));
                    iconStatus.setImageResource(android.R.drawable.ic_menu_agenda);
                    iconStatus.setColorFilter(context.getResources().getColor(R.color.info));
                    break;
                case "in_preparation":
                    textOrderStatus.setText("In Preparation");
                    textOrderStatus.setTextColor(context.getResources().getColor(R.color.pink_primary));
                    iconStatus.setImageResource(android.R.drawable.ic_popup_sync);
                    iconStatus.setColorFilter(context.getResources().getColor(R.color.pink_primary));
                    break;
                case "ready":
                    textOrderStatus.setText("Ready for Pickup");
                    textOrderStatus.setTextColor(context.getResources().getColor(R.color.success));
                    iconStatus.setImageResource(android.R.drawable.ic_menu_compass);
                    iconStatus.setColorFilter(context.getResources().getColor(R.color.success));
                    break;
                case "completed":
                    textOrderStatus.setText("Completed");
                    textOrderStatus.setTextColor(context.getResources().getColor(R.color.success));
                    iconStatus.setImageResource(android.R.drawable.ic_menu_compass);
                    iconStatus.setColorFilter(context.getResources().getColor(R.color.success));
                    break;
                case "rejected":
                    textOrderStatus.setText("Rejected");
                    textOrderStatus.setTextColor(context.getResources().getColor(R.color.error));
                    iconStatus.setImageResource(android.R.drawable.ic_delete);
                    iconStatus.setColorFilter(context.getResources().getColor(R.color.error));
                    break;
                default:
                    textOrderStatus.setText("Unknown");
                    textOrderStatus.setTextColor(context.getResources().getColor(R.color.text_secondary));
                    iconStatus.setImageResource(android.R.drawable.ic_menu_help);
                    iconStatus.setColorFilter(context.getResources().getColor(R.color.text_secondary));
                    break;
            }
        }
    }
}
