package com.example.bouquetbliss.user.orders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.adapters.OrderItemDetailsAdapter;
import com.example.bouquetbliss.databinding.ActivityUserOrderDetailsBinding;
import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.user.base.BaseUserNavigationActivity;
import com.example.bouquetbliss.user.payment.PaymentActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for displaying detailed order information for users
 */
public class UserOrderDetailsActivity extends BaseUserNavigationActivity {

    private ActivityUserOrderDetailsBinding binding;
    private FirebaseFirestore db;
    private OrderItemDetailsAdapter adapter;
    private Order currentOrder;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        // Get order ID from intent
        orderId = getIntent().getStringExtra("order_id");
        if (orderId == null) {
            Toast.makeText(this, "Invalid order ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
        setupClickListeners();
        setupUserBottomNavigation();
        setSelectedNavItem("orders");
        
        // Apply translations
        applyTranslations();
        
        loadOrderDetails();
    }

    private void setupViews() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutOrderContent.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnContactSupport.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@bouquetbliss.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request - Order " + 
                (currentOrder != null ? currentOrder.getFormattedOrderId() : ""));
            
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(emailIntent);
            } else {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnReorder.setOnClickListener(v -> {
            if (currentOrder != null) {
                // Navigate back to browse with intent to reorder
                Toast.makeText(this, "Reorder functionality coming soon!", Toast.LENGTH_SHORT).show();
            }
        });

        // Payment button click listener
        binding.btnPayNow.setOnClickListener(v -> {
            if (currentOrder != null) {
                Intent paymentIntent = new Intent(this, PaymentActivity.class);
                paymentIntent.putExtra("order_id", currentOrder.getId());
                startActivity(paymentIntent);
            }
        });

        // Download invoice button click listener
        binding.btnDownloadInvoice.setOnClickListener(v -> {
            if (currentOrder != null && currentOrder.getInvoicePath() != null) {
                openInvoiceFile(currentOrder.getInvoicePath());
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
        
        // Delivery info
        binding.tvDeliveryAddress.setText(currentOrder.getDeliveryAddress());
        
        // Special instructions
        if (currentOrder.getSpecialInstructions() != null && 
            !currentOrder.getSpecialInstructions().trim().isEmpty()) {
            binding.tvSpecialInstructions.setText(currentOrder.getSpecialInstructions());
            binding.layoutSpecialInstructions.setVisibility(View.VISIBLE);
        } else {
            binding.layoutSpecialInstructions.setVisibility(View.GONE);
        }
        
        // Rejection reason (if applicable)
        if (currentOrder.getRejectionReason() != null && 
            !currentOrder.getRejectionReason().trim().isEmpty()) {
            binding.tvRejectionReason.setText(currentOrder.getRejectionReason());
            binding.layoutRejectionReason.setVisibility(View.VISIBLE);
        } else {
            binding.layoutRejectionReason.setVisibility(View.GONE);
        }
        
        // Order total
        binding.tvOrderTotal.setText(String.format("RM %.2f", currentOrder.getTotalAmount()));
        
        // Payment status and actions
        setupPaymentStatus();
        
        // Setup order items
        setupOrderItems();
        
        // Setup status-specific UI
        setupStatusUI();
        
        // Setup progress indicator
        setupProgressIndicator();
    }

    private void setupOrderItems() {
        adapter = new OrderItemDetailsAdapter(currentOrder.getItems());
        binding.recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewItems.setAdapter(adapter);
    }

    private void setupStatusUI() {
        // Set status color and description
        int statusColor;
        String statusDescription;
        
        switch (currentOrder.getStatus()) {
            case Order.Status.PENDING:
                statusColor = getColor(R.color.status_limited);
                statusDescription = "Your order is being reviewed by our team";
                break;
            case Order.Status.APPROVED:
                statusColor = getColor(R.color.pink_primary);
                statusDescription = "Your order has been approved and will be prepared soon";
                break;
            case Order.Status.IN_PREPARATION:
                statusColor = getColor(R.color.pink_primary);
                statusDescription = "Your bouquet is being carefully prepared";
                break;
            case Order.Status.READY:
                statusColor = getColor(R.color.status_available);
                statusDescription = "Your order is ready for pickup!";
                break;
            case Order.Status.COMPLETED:
                statusColor = getColor(R.color.status_available);
                statusDescription = "Order completed successfully";
                binding.btnReorder.setVisibility(View.VISIBLE);
                break;
            case Order.Status.REJECTED:
                statusColor = getColor(R.color.status_out_of_stock);
                statusDescription = "Unfortunately, your order was rejected";
                break;
            default:
                statusColor = getColor(R.color.text_secondary);
                statusDescription = "Status unknown";
                break;
        }
        
        binding.tvOrderStatus.setTextColor(statusColor);
        binding.tvStatusDescription.setText(statusDescription);
    }

    private void setupProgressIndicator() {
        // Reset all steps
        resetProgressStep(binding.stepPending, binding.linePending);
        resetProgressStep(binding.stepApproved, binding.lineApproved);
        resetProgressStep(binding.stepPreparing, binding.linePreparing);
        resetProgressStep(binding.stepReady, binding.lineReady);
        resetProgressStep(binding.stepCompleted, null);
        
        // Set active steps based on current status
        switch (currentOrder.getStatus()) {
            case Order.Status.PENDING:
                setActiveProgressStep(binding.stepPending, binding.linePending);
                break;
            case Order.Status.APPROVED:
                setActiveProgressStep(binding.stepPending, binding.linePending);
                setActiveProgressStep(binding.stepApproved, binding.lineApproved);
                break;
            case Order.Status.IN_PREPARATION:
                setActiveProgressStep(binding.stepPending, binding.linePending);
                setActiveProgressStep(binding.stepApproved, binding.lineApproved);
                setActiveProgressStep(binding.stepPreparing, binding.linePreparing);
                break;
            case Order.Status.READY:
                setActiveProgressStep(binding.stepPending, binding.linePending);
                setActiveProgressStep(binding.stepApproved, binding.lineApproved);
                setActiveProgressStep(binding.stepPreparing, binding.linePreparing);
                setActiveProgressStep(binding.stepReady, binding.lineReady);
                break;
            case Order.Status.COMPLETED:
                setActiveProgressStep(binding.stepPending, binding.linePending);
                setActiveProgressStep(binding.stepApproved, binding.lineApproved);
                setActiveProgressStep(binding.stepPreparing, binding.linePreparing);
                setActiveProgressStep(binding.stepReady, binding.lineReady);
                setActiveProgressStep(binding.stepCompleted, null);
                break;
            case Order.Status.REJECTED:
                // Show only pending step as active, others as inactive
                setActiveProgressStep(binding.stepPending, null);
                break;
        }
    }

    private void resetProgressStep(View step, View line) {
        step.setBackgroundResource(R.drawable.progress_step_inactive);
        if (line != null) {
            line.setBackgroundColor(getColor(R.color.text_secondary));
        }
    }

    private void setActiveProgressStep(View step, View line) {
        step.setBackgroundResource(R.drawable.progress_step_active);
        if (line != null) {
            line.setBackgroundColor(getColor(R.color.pink_primary));
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

    private void setupPaymentStatus() {
        if (currentOrder.getPaymentStatus() != null) {
            String paymentStatus = currentOrder.getPaymentStatus();
            binding.tvPaymentStatus.setText(getPaymentStatusDisplayText(paymentStatus));
            
            // Set payment status color
            switch (paymentStatus) {
                case Order.PaymentStatus.PAID:
                    binding.tvPaymentStatus.setTextColor(getResources().getColor(R.color.success));
                    break;
                case Order.PaymentStatus.FAILED:
                    binding.tvPaymentStatus.setTextColor(getResources().getColor(R.color.error));
                    break;
                case Order.PaymentStatus.PROCESSING:
                    binding.tvPaymentStatus.setTextColor(getResources().getColor(R.color.warning));
                    break;
                default:
                    binding.tvPaymentStatus.setTextColor(getResources().getColor(R.color.text_secondary));
                    break;
            }
            
            // Show payment method if available
            if (currentOrder.getPaymentMethod() != null && !currentOrder.getPaymentMethod().isEmpty()) {
                String paymentMethod = getPaymentMethodDisplayText(currentOrder.getPaymentMethod());
                binding.tvPaymentMethod.setText(paymentMethod);
                binding.layoutPaymentMethod.setVisibility(View.VISIBLE);
            } else {
                binding.layoutPaymentMethod.setVisibility(View.GONE);
            }
            
            // Show transaction ID if available
            if (currentOrder.getTransactionId() != null && !currentOrder.getTransactionId().isEmpty()) {
                binding.tvTransactionId.setText(currentOrder.getTransactionId());
                binding.layoutTransactionId.setVisibility(View.VISIBLE);
            } else {
                binding.layoutTransactionId.setVisibility(View.GONE);
            }
            
            // Show payment date if available
            if (currentOrder.getPaymentDate() > 0) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
                binding.tvPaymentDate.setText(dateFormat.format(new Date(currentOrder.getPaymentDate())));
                binding.layoutPaymentDate.setVisibility(View.VISIBLE);
            } else {
                binding.layoutPaymentDate.setVisibility(View.GONE);
            }
            
            // Show/hide payment and invoice buttons
            setupPaymentButtons(paymentStatus);
            
            binding.layoutPaymentStatus.setVisibility(View.VISIBLE);
        } else {
            binding.layoutPaymentStatus.setVisibility(View.GONE);
        }
    }
    
    private void setupPaymentButtons(String paymentStatus) {
        // Pay Now button
        if (Order.PaymentStatus.PENDING.equals(paymentStatus) || Order.PaymentStatus.FAILED.equals(paymentStatus)) {
            binding.btnPayNow.setVisibility(View.VISIBLE);
        } else {
            binding.btnPayNow.setVisibility(View.GONE);
        }
        
        // Download Invoice button
        if (Order.PaymentStatus.PAID.equals(paymentStatus) && 
            currentOrder.getInvoicePath() != null && !currentOrder.getInvoicePath().isEmpty()) {
            binding.btnDownloadInvoice.setVisibility(View.VISIBLE);
        } else {
            binding.btnDownloadInvoice.setVisibility(View.GONE);
        }
    }
    
    private String getPaymentStatusDisplayText(String status) {
        switch (status) {
            case Order.PaymentStatus.PENDING:
                return "Payment Pending";
            case Order.PaymentStatus.PROCESSING:
                return "Processing Payment";
            case Order.PaymentStatus.PAID:
                return "Payment Successful";
            case Order.PaymentStatus.FAILED:
                return "Payment Failed";
            case Order.PaymentStatus.REFUNDED:
                return "Payment Refunded";
            default:
                return "Unknown Status";
        }
    }
    
    private String getPaymentMethodDisplayText(String method) {
        switch (method) {
            case Order.PaymentMethod.TOYYIBPAY:
                return "ToyyibPay";
            case Order.PaymentMethod.CASH_ON_DELIVERY:
                return "Cash on Delivery";
            default:
                return "Unknown Method";
        }
    }
    
    private void openInvoiceFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
                );
                intent.setDataAndType(uri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "No PDF viewer found. Please install a PDF reader app.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Invoice file not found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening invoice: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
