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
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying booking history in reports
 */
public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {

    private List<Order> orderList;
    private SimpleDateFormat dateFormat;
    private Context context;

    public BookingHistoryAdapter(List<Order> orderList) {
        this.orderList = orderList;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    public void updateOrders(List<Order> newOrderList) {
        this.orderList = newOrderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_booking_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardOrder;
        TextView tvOrderId;
        TextView tvCustomerName;
        TextView tvOrderDate;
        TextView tvDeliveryDate;
        TextView tvOrderStatus;
        TextView tvOrderAmount;
        TextView tvItemCount;

        ViewHolder(View itemView) {
            super(itemView);
            cardOrder = itemView.findViewById(R.id.cardOrder);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvDeliveryDate = itemView.findViewById(R.id.tvDeliveryDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderAmount = itemView.findViewById(R.id.tvOrderAmount);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
        }

        void bind(Order order) {
            tvOrderId.setText(order.getFormattedOrderId());
            tvCustomerName.setText(order.getCustomerName());
            tvOrderDate.setText("Ordered: " + dateFormat.format(new Date(order.getOrderDate())));
            tvDeliveryDate.setText("Delivery: " + dateFormat.format(new Date(order.getDeliveryDate())));
            tvOrderAmount.setText(String.format("RM %.2f", order.getTotalAmount()));
            
            // Set item count
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            tvItemCount.setText(itemCount + " item" + (itemCount != 1 ? "s" : ""));
            
            // Set status and color
            tvOrderStatus.setText(getStatusDisplayText(order.getStatus()));
            setStatusColor(order.getStatus());
        }

        private void setStatusColor(String status) {
            int colorRes;
            switch (status) {
                case Order.Status.PENDING:
                    colorRes = R.color.status_limited;
                    break;
                case Order.Status.APPROVED:
                case Order.Status.IN_PREPARATION:
                    colorRes = R.color.pink_primary;
                    break;
                case Order.Status.READY:
                case Order.Status.COMPLETED:
                    colorRes = R.color.status_available;
                    break;
                case Order.Status.REJECTED:
                    colorRes = R.color.status_out_of_stock;
                    break;
                default:
                    colorRes = R.color.text_secondary;
                    break;
            }
            
            tvOrderStatus.setTextColor(context.getColor(colorRes));
        }

        private String getStatusDisplayText(String status) {
            switch (status) {
                case Order.Status.PENDING:
                    return "Pending";
                case Order.Status.APPROVED:
                    return "Approved";
                case Order.Status.IN_PREPARATION:
                    return "In Preparation";
                case Order.Status.READY:
                    return "Ready";
                case Order.Status.COMPLETED:
                    return "Completed";
                case Order.Status.REJECTED:
                    return "Rejected";
                default:
                    return "Unknown";
            }
        }
    }
}
