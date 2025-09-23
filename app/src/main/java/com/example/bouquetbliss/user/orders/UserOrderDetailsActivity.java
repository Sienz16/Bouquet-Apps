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
import com.example.bouquetbliss.dialogs.RatingDialog;
import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.models.OrderItem;
import com.example.bouquetbliss.models.Rating;
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

        // Confirm delivery button click listener
        binding.btnConfirmDelivery.setOnClickListener(v -> {
            if (currentOrder != null && Order.Status.READY.equals(currentOrder.getStatus())) {
                showConfirmDeliveryDialog();
            }
        });

        // Rate order button click listener
        binding.btnRateOrder.setOnClickListener(v -> {
            if (currentOrder != null && currentOrder.canBeRated()) {
                showRatingOptions();
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
                
                // Show confirm delivery button for ready orders
                binding.btnConfirmDelivery.setVisibility(View.VISIBLE);
                break;
            case Order.Status.COMPLETED:
                statusColor = getColor(R.color.status_available);
                statusDescription = "Order completed successfully";
                binding.btnReorder.setVisibility(View.VISIBLE);
                
                // Show rate button if order can be rated
                if (currentOrder.canBeRated()) {
                    binding.btnRateOrder.setVisibility(View.VISIBLE);
                } else {
                    binding.btnRateOrder.setVisibility(View.GONE);
                }
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
                // First try to open with PDF viewer
                Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
                );
                pdfIntent.setDataAndType(uri, "application/pdf");
                pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                if (pdfIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(pdfIntent);
                } else {
                    // No PDF viewer found, show options dialog
                    showPdfOptionsDialog(uri);
                }
            } else {
                Toast.makeText(this, "Invoice file not found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening invoice: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showPdfOptionsDialog(Uri fileUri) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Open Invoice")
               .setMessage("No PDF viewer app found. Choose an option:")
               .setPositiveButton("Get PDF Reader", (dialog, which) -> {
                   // Open Google Play Store to download a PDF reader
                   openPlayStoreForPdfReader();
               })
               .setNegativeButton("Share Invoice", (dialog, which) -> {
                   // Share the PDF file
                   shareInvoiceFile(fileUri);
               })
               .setNeutralButton("Save to Downloads", (dialog, which) -> {
                   // Save file to Downloads folder
                   saveInvoiceToDownloads(fileUri);
               })
               .show();
    }
    
    private void openPlayStoreForPdfReader() {
        try {
            // Try to open Play Store app first
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, 
                Uri.parse("market://search?q=pdf+reader&c=apps"));
            if (playStoreIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(playStoreIntent);
            } else {
                // Fallback to web browser
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/search?q=pdf+reader&c=apps"));
                if (webIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(webIntent);
                } else {
                    Toast.makeText(this, "Cannot open app store", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening app store: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void shareInvoiceFile(Uri fileUri) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Invoice - Order " + 
                (currentOrder != null ? currentOrder.getFormattedOrderId() : ""));
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Please find attached your order invoice.");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            Intent chooserIntent = Intent.createChooser(shareIntent, "Share Invoice");
            if (chooserIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooserIntent);
            } else {
                Toast.makeText(this, "No apps available to share the file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing invoice: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveInvoiceToDownloads(Uri fileUri) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // For Android 10 and above, use MediaStore
                saveInvoiceUsingMediaStore(fileUri);
            } else {
                // For older versions, save directly to Downloads folder
                saveInvoiceToLegacyDownloads(fileUri);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving invoice: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveInvoiceUsingMediaStore(Uri fileUri) {
        try {
            android.content.ContentResolver resolver = getContentResolver();
            android.content.ContentValues contentValues = new android.content.ContentValues();
            
            String fileName = "Invoice_Order_" + 
                (currentOrder != null ? currentOrder.getFormattedOrderId() : "Unknown") + ".pdf";
            
            contentValues.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            contentValues.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, 
                android.os.Environment.DIRECTORY_DOWNLOADS);
            
            Uri downloadUri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            
            if (downloadUri != null) {
                java.io.InputStream inputStream = resolver.openInputStream(fileUri);
                java.io.OutputStream outputStream = resolver.openOutputStream(downloadUri);
                
                if (inputStream != null && outputStream != null) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    
                    inputStream.close();
                    outputStream.close();
                    
                    Toast.makeText(this, "Invoice saved to Downloads folder", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Error accessing file streams", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error creating download file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving to Downloads: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveInvoiceToLegacyDownloads(Uri fileUri) {
        try {
            File downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS);
            
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            
            String fileName = "Invoice_Order_" + 
                (currentOrder != null ? currentOrder.getFormattedOrderId() : "Unknown") + ".pdf";
            File destinationFile = new File(downloadsDir, fileName);
            
            java.io.InputStream inputStream = getContentResolver().openInputStream(fileUri);
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(destinationFile);
            
            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                
                inputStream.close();
                outputStream.close();
                
                Toast.makeText(this, "Invoice saved to Downloads folder: " + fileName, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Error reading source file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving to Downloads: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showRatingOptions() {
        if (currentOrder.getItems() == null || currentOrder.getItems().isEmpty()) {
            Toast.makeText(this, "No items to rate", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // If order has only one item, show rating dialog directly
        if (currentOrder.getItems().size() == 1) {
            showRatingDialog(currentOrder.getItems().get(0));
            return;
        }
        
        // If multiple items, show selection dialog
        String[] itemNames = new String[currentOrder.getItems().size()];
        for (int i = 0; i < currentOrder.getItems().size(); i++) {
            OrderItem item = currentOrder.getItems().get(i);
            itemNames[i] = item.getBouquetName() + " (Qty: " + item.getQuantity() + ")";
        }
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Select Bouquet to Rate")
               .setItems(itemNames, (dialog, which) -> {
                   showRatingDialog(currentOrder.getItems().get(which));
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    private void showRatingDialog(OrderItem orderItem) {
        RatingDialog ratingDialog = new RatingDialog(this, currentOrder, orderItem, 
            new RatingDialog.RatingCallback() {
                @Override
                public void onRatingSubmitted(Rating rating) {
                    // Refresh the order details to hide rate button
                    displayOrderDetails();
                    Toast.makeText(UserOrderDetailsActivity.this, 
                        "Thank you for rating " + rating.getBouquetName() + "!", 
                        Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onRatingCancelled() {
                    // Do nothing
                }
            });
        
        ratingDialog.show();
    }
    
    private void showConfirmDeliveryDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Confirm Delivery")
               .setMessage("Have you received your order?\n\n" +
                          "Order: " + currentOrder.getFormattedOrderId() + "\n" +
                          "Please confirm only after you have received your bouquet.")
               .setPositiveButton("Yes, I received it", (dialog, which) -> {
                   confirmDelivery();
               })
               .setNegativeButton("Not yet", null)
               .setIcon(android.R.drawable.ic_dialog_info)
               .show();
    }
    
    private void confirmDelivery() {
        // Show loading state
        binding.btnConfirmDelivery.setEnabled(false);
        binding.btnConfirmDelivery.setText("Confirming...");
        
        // Update order status to completed
        currentOrder.setStatus(Order.Status.COMPLETED);
        currentOrder.setUpdatedAt(System.currentTimeMillis());
        
        // Add delivery confirmation timestamp
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", Order.Status.COMPLETED);
        updates.put("updatedAt", System.currentTimeMillis());
        updates.put("deliveryConfirmedAt", System.currentTimeMillis());
        updates.put("deliveryConfirmedBy", "customer");
        
        db.collection("orders").document(orderId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Success - refresh the order details
                    Toast.makeText(this, "Thank you! Your delivery has been confirmed.", Toast.LENGTH_LONG).show();
                    
                    // Reload order details to update UI
                    loadOrderDetails();
                    
                    // Create notification for admin (optional)
                    createAdminNotification();
                })
                .addOnFailureListener(e -> {
                    // Reset button state on failure
                    binding.btnConfirmDelivery.setEnabled(true);
                    binding.btnConfirmDelivery.setText("Confirm Delivery");
                    
                    Toast.makeText(this, "Failed to confirm delivery: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
    }
    
    private void createAdminNotification() {
        // Create a simple notification record for admin
        java.util.Map<String, Object> notification = new java.util.HashMap<>();
        notification.put("type", "delivery_confirmed");
        notification.put("orderId", currentOrder.getId());
        notification.put("orderNumber", currentOrder.getFormattedOrderId());
        notification.put("customerName", currentOrder.getCustomerName());
        notification.put("message", "Customer confirmed delivery for order " + currentOrder.getFormattedOrderId());
        notification.put("createdAt", System.currentTimeMillis());
        notification.put("isRead", false);
        
        db.collection("admin_notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    // Notification created successfully
                })
                .addOnFailureListener(e -> {
                    // Log error but don't show to user
                    android.util.Log.e("UserOrderDetails", "Failed to create admin notification", e);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
