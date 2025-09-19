package com.example.bouquetbliss.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.databinding.ItemOrderAdminBinding;
import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.models.OrderItem;
import com.example.bouquetbliss.utils.TranslationManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OnOrderActionListener listener;
    private Context context;
    private TranslationManager translationManager;

    public interface OnOrderActionListener {
        void onViewDetails(Order order);
        void onApproveOrder(Order order);
        void onRejectOrder(Order order);
        void onUpdatePreparationStatus(Order order, String newStatus);
    }

    public OrderAdapter(List<Order> orderList, OnOrderActionListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (translationManager == null) {
            translationManager = TranslationManager.getInstance(context);
        }
        ItemOrderAdminBinding binding = ItemOrderAdminBinding.inflate(
            LayoutInflater.from(context), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orderList = newOrders;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private ItemOrderAdminBinding binding;

        public OrderViewHolder(ItemOrderAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order) {
            // Set order basic info
            binding.tvOrderId.setText(order.getFormattedOrderId());
            binding.tvOrderDate.setText(formatDate(order.getOrderDate()));
            
            // Set customer info
            binding.tvCustomerName.setText(order.getCustomerName());
            binding.tvCustomerPhone.setText(order.getCustomerPhone());

            // Set bouquet info (show first item if multiple)
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                OrderItem firstItem = order.getItems().get(0);
                binding.tvBouquetName.setText(firstItem.getBouquetName());
                binding.tvQuantity.setText(String.valueOf(order.getTotalQuantity()));
                binding.tvTotalPrice.setText(String.format("RM %.2f", order.getTotalAmount()));
                
                // Load bouquet image (placeholder for now)
                binding.ivBouquetImage.setImageResource(R.drawable.circular_logo_placeholder);
            }

            // Set status and status-specific UI
            setupStatusUI(order);
            setupActionButtons(order);
            setupPreparationStatus(order);

            // Set click listeners
            binding.btnViewDetails.setOnClickListener(v -> {
                if (listener != null) listener.onViewDetails(order);
            });

            binding.btnApprove.setOnClickListener(v -> {
                if (listener != null) listener.onApproveOrder(order);
            });

            binding.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onRejectOrder(order);
            });

            binding.btnUpdateStatus.setOnClickListener(v -> {
                String selectedStatus = binding.spinnerPreparationStatus.getSelectedItem().toString();
                if (listener != null) listener.onUpdatePreparationStatus(order, selectedStatus);
            });
        }

        private void setupStatusUI(Order order) {
            String status = order.getStatus();
            String statusText = "";
            int statusColor = Color.parseColor("#FF6B9D"); // Default pink

            switch (status) {
                case Order.Status.PENDING:
                    statusText = "Pending";
                    statusColor = Color.parseColor("#FF9800"); // Orange
                    break;
                case Order.Status.APPROVED:
                case Order.Status.IN_PREPARATION:
                    statusText = "In Progress";
                    statusColor = Color.parseColor("#2196F3"); // Blue
                    break;
                case Order.Status.READY:
                    statusText = "Ready";
                    statusColor = Color.parseColor("#4CAF50"); // Green
                    break;
                case Order.Status.COMPLETED:
                    statusText = "Completed";
                    statusColor = Color.parseColor("#4CAF50"); // Green
                    break;
                case Order.Status.REJECTED:
                    statusText = "Rejected";
                    statusColor = Color.parseColor("#F44336"); // Red
                    break;
            }

            // Apply translation if available
            if (translationManager != null) {
                translationManager.translateAndSetText(binding.tvOrderStatus, statusText);
            } else {
                binding.tvOrderStatus.setText(statusText);
            }
            binding.tvOrderStatus.setBackgroundColor(statusColor);
        }

        private void setupActionButtons(Order order) {
            // Hide all buttons first
            binding.btnApprove.setVisibility(View.GONE);
            binding.btnReject.setVisibility(View.GONE);

            // Show buttons based on order status
            if (order.isPending()) {
                binding.btnApprove.setVisibility(View.VISIBLE);
                binding.btnReject.setVisibility(View.VISIBLE);
            }
        }

        private void setupPreparationStatus(Order order) {
            if (order.isApproved()) {
                binding.layoutPreparationStatus.setVisibility(View.VISIBLE);
                setupPreparationSpinner(order);
            } else {
                binding.layoutPreparationStatus.setVisibility(View.GONE);
            }
        }

        private void setupPreparationSpinner(Order order) {
            String[] preparationStatuses = {
                "Not Started",
                "Preparing",
                "Ready for Pickup",
                "Completed"
            };

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, preparationStatuses);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerPreparationStatus.setAdapter(adapter);

            // Set current selection based on order status
            String currentStatus = order.getPreparationStatus();
            int position = 0;
            switch (currentStatus) {
                case Order.PreparationStatus.NOT_STARTED:
                    position = 0;
                    break;
                case Order.PreparationStatus.PREPARING:
                    position = 1;
                    break;
                case Order.PreparationStatus.READY_FOR_PICKUP:
                    position = 2;
                    break;
                case Order.PreparationStatus.COMPLETED:
                    position = 3;
                    break;
            }
            binding.spinnerPreparationStatus.setSelection(position);
        }

        private String formatDate(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}
