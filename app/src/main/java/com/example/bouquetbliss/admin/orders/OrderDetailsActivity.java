package com.example.bouquetbliss.admin.orders;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.adapters.OrderItemDetailsAdapter;
import com.example.bouquetbliss.admin.base.BaseAdminNavigationActivity;
import com.example.bouquetbliss.databinding.ActivityOrderDetailsBinding;
import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.models.OrderItem;
import com.example.bouquetbliss.models.StockTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for displaying detailed order information for admin
 */
public class OrderDetailsActivity extends BaseAdminNavigationActivity {

    private ActivityOrderDetailsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private OrderItemDetailsAdapter adapter;
    private Order currentOrder;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get order ID from intent
        orderId = getIntent().getStringExtra("order_id");
        if (orderId == null) {
            Toast.makeText(this, "Invalid order ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
        setupClickListeners();
        setupAdminBottomNavigation();
        setSelectedNavItem("orders");
        
        loadOrderDetails();
    }

    private void setupViews() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutOrderContent.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnApprove.setOnClickListener(v -> approveOrder());
        binding.btnReject.setOnClickListener(v -> rejectOrder());
        binding.btnUpdateStatus.setOnClickListener(v -> updatePreparationStatus());
        
        binding.btnCallCustomer.setOnClickListener(v -> {
            if (currentOrder != null && currentOrder.getCustomerPhone() != null) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(android.net.Uri.parse("tel:" + currentOrder.getCustomerPhone()));
                startActivity(callIntent);
            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnEmailCustomer.setOnClickListener(v -> {
            if (currentOrder != null && currentOrder.getCustomerEmail() != null) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{currentOrder.getCustomerEmail()});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Regarding your order " + currentOrder.getFormattedOrderId());
                startActivity(Intent.createChooser(emailIntent, "Send Email"));
            } else {
                Toast.makeText(this, "Email not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrderDetails() {
        db.collection("orders").document(orderId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentOrder = documentSnapshot.toObject(Order.class);
                    if (currentOrder != null) {
                        currentOrder.setId(documentSnapshot.getId());
                        displayOrderDetails();
                    }
                } else {
                    Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load order: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void displayOrderDetails() {
        binding.progressBar.setVisibility(View.GONE);
        binding.layoutOrderContent.setVisibility(View.VISIBLE);

        // Order basic info
        binding.tvOrderId.setText(currentOrder.getFormattedOrderId());
        binding.tvOrderStatus.setText(getStatusDisplayText(currentOrder.getStatus()));
        
        // Format and display dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        binding.tvOrderDate.setText(dateFormat.format(new Date(currentOrder.getOrderDate())));
        binding.tvDeliveryDate.setText(dateFormat.format(new Date(currentOrder.getDeliveryDate())));
        
        // Customer info
        binding.tvCustomerName.setText(currentOrder.getCustomerName());
        binding.tvCustomerEmail.setText(currentOrder.getCustomerEmail());
        binding.tvCustomerPhone.setText(currentOrder.getCustomerPhone() != null ? 
            currentOrder.getCustomerPhone() : "Not provided");
        binding.tvDeliveryAddress.setText(currentOrder.getDeliveryAddress());
        
        // Special instructions
        if (currentOrder.getSpecialInstructions() != null && 
            !currentOrder.getSpecialInstructions().trim().isEmpty()) {
            binding.tvSpecialInstructions.setText(currentOrder.getSpecialInstructions());
            binding.layoutSpecialInstructions.setVisibility(View.VISIBLE);
        } else {
            binding.layoutSpecialInstructions.setVisibility(View.GONE);
        }

        // Flower color preference
        if (currentOrder.getFlowerColorPreference() != null && 
            !currentOrder.getFlowerColorPreference().trim().isEmpty()) {
            binding.tvFlowerColorPreference.setText(currentOrder.getFlowerColorPreference());
            binding.layoutFlowerColorPreference.setVisibility(View.VISIBLE);
        } else {
            binding.layoutFlowerColorPreference.setVisibility(View.GONE);
        }
        
        // Rejection reason
        if (currentOrder.getRejectionReason() != null && 
            !currentOrder.getRejectionReason().trim().isEmpty()) {
            binding.tvRejectionReason.setText(currentOrder.getRejectionReason());
            binding.layoutRejectionReason.setVisibility(View.VISIBLE);
        } else {
            binding.layoutRejectionReason.setVisibility(View.GONE);
        }
        
        // Order total
        binding.tvOrderTotal.setText(String.format("RM %.2f", currentOrder.getTotalAmount()));
        
        // Setup order items
        setupOrderItems();
        
        // Setup status-specific UI
        setupStatusUI();
    }

    private void setupOrderItems() {
        adapter = new OrderItemDetailsAdapter(currentOrder.getItems());
        binding.recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewItems.setAdapter(adapter);
    }

    private void setupStatusUI() {
        // Set status color
        int statusColor;
        switch (currentOrder.getStatus()) {
            case Order.Status.PENDING:
                statusColor = getColor(R.color.status_limited);
                break;
            case Order.Status.APPROVED:
            case Order.Status.IN_PREPARATION:
                statusColor = getColor(R.color.pink_primary);
                break;
            case Order.Status.READY:
            case Order.Status.COMPLETED:
                statusColor = getColor(R.color.status_available);
                break;
            case Order.Status.REJECTED:
                statusColor = getColor(R.color.status_out_of_stock);
                break;
            default:
                statusColor = getColor(R.color.text_secondary);
                break;
        }
        binding.tvOrderStatus.setTextColor(statusColor);
        
        // Show/hide action buttons based on status
        setupActionButtons();
        
        // Setup preparation status if applicable
        if (currentOrder.isApproved()) {
            binding.layoutPreparationStatus.setVisibility(View.VISIBLE);
            setupPreparationStatusSpinner();
        } else {
            binding.layoutPreparationStatus.setVisibility(View.GONE);
        }
    }

    private void setupActionButtons() {
        if (currentOrder.isPending()) {
            binding.btnApprove.setVisibility(View.VISIBLE);
            binding.btnReject.setVisibility(View.VISIBLE);
        } else {
            binding.btnApprove.setVisibility(View.GONE);
            binding.btnReject.setVisibility(View.GONE);
        }
        
        binding.btnUpdateStatus.setVisibility(
            currentOrder.isApproved() ? View.VISIBLE : View.GONE);
    }

    private void setupPreparationStatusSpinner() {
        String[] preparationStatuses = {
            "Not Started",
            "Preparing", 
            "Ready for Pickup",
            "Completed"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, preparationStatuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPreparationStatus.setAdapter(adapter);

        // Set current selection
        String currentStatus = currentOrder.getPreparationStatus();
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

    private void approveOrder() {
        new AlertDialog.Builder(this)
            .setTitle("Approve Order")
            .setMessage("Are you sure you want to approve this order?")
            .setPositiveButton("Approve", (dialog, which) -> {
                currentOrder.setStatus(Order.Status.APPROVED);
                updateOrderInDatabase();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void rejectOrder() {
        EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setHint("Enter rejection reason");
        
        new AlertDialog.Builder(this)
            .setTitle("Reject Order")
            .setMessage("Please provide a reason for rejection:")
            .setView(editText)
            .setPositiveButton("Reject", (dialog, which) -> {
                String reason = editText.getText().toString().trim();
                if (reason.isEmpty()) {
                    Toast.makeText(this, "Please provide a rejection reason", Toast.LENGTH_SHORT).show();
                    return;
                }
                currentOrder.setStatus(Order.Status.REJECTED);
                currentOrder.setRejectionReason(reason);
                updateOrderInDatabase();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void updatePreparationStatus() {
        String selectedStatus = binding.spinnerPreparationStatus.getSelectedItem().toString();
        String statusValue = convertStatusToValue(selectedStatus);
        currentOrder.setPreparationStatus(statusValue);
        
        // Update main status if needed
        if (statusValue.equals(Order.PreparationStatus.COMPLETED)) {
            currentOrder.setStatus(Order.Status.COMPLETED);
        } else if (statusValue.equals(Order.PreparationStatus.READY_FOR_PICKUP)) {
            currentOrder.setStatus(Order.Status.READY);
        } else if (statusValue.equals(Order.PreparationStatus.PREPARING)) {
            currentOrder.setStatus(Order.Status.IN_PREPARATION);
        }
        
        updateOrderInDatabase();
    }

    private String convertStatusToValue(String displayStatus) {
        switch (displayStatus) {
            case "Not Started":
                return Order.PreparationStatus.NOT_STARTED;
            case "Preparing":
                return Order.PreparationStatus.PREPARING;
            case "Ready for Pickup":
                return Order.PreparationStatus.READY_FOR_PICKUP;
            case "Completed":
                return Order.PreparationStatus.COMPLETED;
            default:
                return Order.PreparationStatus.NOT_STARTED;
        }
    }

    private void updateOrderInDatabase() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", currentOrder.getStatus());
        updates.put("preparationStatus", currentOrder.getPreparationStatus());
        updates.put("updatedAt", System.currentTimeMillis());
        
        if (currentOrder.getRejectionReason() != null) {
            updates.put("rejectionReason", currentOrder.getRejectionReason());
        }

        db.collection("orders").document(orderId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Order updated successfully", Toast.LENGTH_SHORT).show();
                
                // Update UI
                displayOrderDetails();
                
                // Create stock transaction if order is completed
                if (currentOrder.getStatus().equals(Order.Status.COMPLETED)) {
                    createStockTransactionForOrder();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update order: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void createStockTransactionForOrder() {
        FirebaseUser currentUser = auth.getCurrentUser();
        String adminId = currentUser != null ? currentUser.getUid() : "system";
        String adminName = currentUser != null ? 
            (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Admin") : "System";

        // Create stock transactions for each item in the order
        for (OrderItem item : currentOrder.getItems()) {
            // First, get current stock level
            db.collection("bouquets").document(item.getBouquetId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int currentStock = documentSnapshot.getLong("quantity").intValue();
                        int quantityUsed = item.getQuantity();
                        int newStock = Math.max(0, currentStock - quantityUsed);
                        
                        // Update bouquet stock
                        Map<String, Object> stockUpdates = new HashMap<>();
                        stockUpdates.put("quantity", newStock);
                        stockUpdates.put("updatedAt", System.currentTimeMillis());
                        
                        // Update availability based on new stock
                        String availability;
                        if (newStock <= 0) {
                            availability = "Out of Stock";
                        } else if (newStock <= 5) {
                            availability = "Limited";
                        } else {
                            availability = "Available";
                        }
                        stockUpdates.put("availability", availability);
                        
                        db.collection("bouquets").document(item.getBouquetId())
                            .update(stockUpdates)
                            .addOnSuccessListener(aVoid -> {
                                // Create stock transaction record
                                StockTransaction transaction = new StockTransaction(
                                    item.getBouquetId(),
                                    item.getBouquetName(),
                                    StockTransaction.TransactionType.ORDER_FULFILLMENT,
                                    -quantityUsed, // Negative because stock is being used
                                    currentStock,
                                    newStock,
                                    "Order fulfilled: " + currentOrder.getFormattedOrderId(),
                                    adminId,
                                    adminName
                                );
                                transaction.setOrderId(currentOrder.getId());
                                
                                db.collection("stock_transactions")
                                    .add(transaction);
                            });
                    }
                });
        }
    }

    private String getStatusDisplayText(String status) {
        switch (status) {
            case Order.Status.PENDING:
                return "Pending Review";
            case Order.Status.APPROVED:
                return "Approved";
            case Order.Status.IN_PREPARATION:
                return "In Preparation";
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
