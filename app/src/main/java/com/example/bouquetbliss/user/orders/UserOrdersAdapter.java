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
import com.example.bouquetbliss.utils.TranslationManager;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserOrdersAdapter extends RecyclerView.Adapter<UserOrdersAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OnOrderClickListener listener;
    private SimpleDateFormat dateFormat;
    private TranslationManager translationManager;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public UserOrdersAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
    }
    
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        translationManager = TranslationManager.getInstance(recyclerView.getContext());
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
            setOrderStatus(order, context);
            
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

        private void setOrderStatus(Order order, Context context) {
            String statusText;
            int colorRes, iconRes;
            String status = order.getStatus();
            
            // Check if payment is required for pending orders
            if ("pending".equalsIgnoreCase(status) && 
                (order.getPaymentStatus() == null || !Order.PaymentStatus.PAID.equals(order.getPaymentStatus()))) {
                statusText = "Payment Required!";
                colorRes = R.color.error;
                iconRes = android.R.drawable.ic_dialog_alert;
            } else {
                switch (status.toLowerCase()) {
                    case "pending":
                        statusText = "Pending Review";
                        colorRes = R.color.warning;
                        iconRes = android.R.drawable.ic_menu_recent_history;
                        break;
                case "approved":
                    statusText = "Approved";
                    colorRes = R.color.info;
                    iconRes = android.R.drawable.ic_menu_agenda;
                    break;
                case "in_preparation":
                    statusText = "In Preparation";
                    colorRes = R.color.pink_primary;
                    iconRes = android.R.drawable.ic_popup_sync;
                    break;
                case "ready":
                    statusText = "Ready for Pickup";
                    colorRes = R.color.success;
                    iconRes = android.R.drawable.ic_menu_compass;
                    break;
                case "completed":
                    statusText = "Completed";
                    colorRes = R.color.success;
                    iconRes = android.R.drawable.ic_menu_compass;
                    break;
                case "rejected":
                    statusText = "Rejected";
                    colorRes = R.color.error;
                    iconRes = android.R.drawable.ic_delete;
                    break;
                default:
                    statusText = "Unknown";
                    colorRes = R.color.text_secondary;
                    iconRes = android.R.drawable.ic_menu_help;
                    break;
                }
            }
            
            // Apply translation if available
            if (translationManager != null) {
                translationManager.translateAndSetText(textOrderStatus, statusText);
            } else {
                textOrderStatus.setText(statusText);
            }
            
            textOrderStatus.setTextColor(context.getResources().getColor(colorRes));
            iconStatus.setImageResource(iconRes);
            iconStatus.setColorFilter(context.getResources().getColor(colorRes));
        }
    }
}
