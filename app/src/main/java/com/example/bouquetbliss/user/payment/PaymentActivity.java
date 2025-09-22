package com.example.bouquetbliss.user.payment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bouquetbliss.databinding.ActivityPaymentBinding;
import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.services.InvoiceGeneratorService;
import com.example.bouquetbliss.services.ToyyibPayService;
import com.example.bouquetbliss.user.orders.UserOrderDetailsActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class PaymentActivity extends AppCompatActivity {
    private static final String TAG = "PaymentActivity";
    
    private ActivityPaymentBinding binding;
    private FirebaseFirestore db;
    private ToyyibPayService toyyibPayService;
    private InvoiceGeneratorService invoiceGenerator;
    
    private String orderId;
    private Order currentOrder;
    private Handler statusCheckHandler;
    private Runnable statusCheckRunnable;
    private boolean paymentCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        toyyibPayService = new ToyyibPayService();
        invoiceGenerator = new InvoiceGeneratorService(this);
        statusCheckHandler = new Handler(Looper.getMainLooper());

        // Get order ID from intent
        orderId = getIntent().getStringExtra("order_id");
        if (orderId == null) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupWebView();
        setupClickListeners();
        loadOrderAndInitiatePayment();
    }

    private void setupWebView() {
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setDomStorageEnabled(true);
        binding.webView.getSettings().setSupportZoom(false);
        
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "URL loading: " + url);
                
                // Check if this is our return URL
                if (url.startsWith("bouquetbliss://payment/return")) {
                    Uri uri = Uri.parse(url);
                    String returnOrderId = uri.getQueryParameter("order_id");
                    
                    if (orderId.equals(returnOrderId)) {
                        // Parse payment result from return URL parameters
                        handlePaymentReturn(uri);
                        return true;
                    }
                }
                
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                binding.progressBar.setVisibility(android.view.View.GONE);
                binding.webView.setVisibility(android.view.View.VISIBLE);
            }
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> {
            showCancelPaymentDialog();
        });

        binding.btnRefresh.setOnClickListener(v -> {
            checkPaymentStatus();
        });
    }

    private void loadOrderAndInitiatePayment() {
        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        binding.textStatus.setText("Loading order details...");

        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentOrder = documentSnapshot.toObject(Order.class);
                        if (currentOrder != null) {
                            currentOrder.setId(documentSnapshot.getId());
                            
                            // Check if payment URL already exists
                            if (currentOrder.hasPaymentUrl()) {
                                loadPaymentUrl(currentOrder.getPaymentUrl());
                                startStatusChecking();
                            } else {
                                createPaymentBill();
                            }
                        }
                    } else {
                        showError("Order not found");
                    }
                })
                .addOnFailureListener(e -> {
                    showError("Failed to load order: " + e.getMessage());
                });
    }

    private void createPaymentBill() {
        binding.textStatus.setText("Creating payment bill...");
        
        String returnUrl = ToyyibPayService.generateReturnUrl(orderId);
        String description = "Payment for Bouquet Bliss Order " + currentOrder.getFormattedOrderId();
        
        toyyibPayService.createBill(
                orderId,
                currentOrder.getCustomerName(),
                currentOrder.getCustomerEmail(),
                currentOrder.getCustomerPhone(),
                currentOrder.getTotalAmount(),
                description,
                returnUrl,
                new ToyyibPayService.PaymentCallback() {
                    @Override
                    public void onSuccess(String billCode, String paymentUrl) {
                        runOnUiThread(() -> {
                            // Update order with payment details
                            updateOrderWithPaymentDetails(billCode, paymentUrl);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showError("Failed to create payment: " + error);
                        });
                    }
                }
        );
    }

    private void updateOrderWithPaymentDetails(String billCode, String paymentUrl) {
        currentOrder.setBillCode(billCode);
        currentOrder.setPaymentUrl(paymentUrl);
        currentOrder.setPaymentMethod(Order.PaymentMethod.TOYYIBPAY);
        currentOrder.setPaymentStatus(Order.PaymentStatus.PROCESSING);

        db.collection("orders").document(orderId)
                .set(currentOrder)
                .addOnSuccessListener(aVoid -> {
                    loadPaymentUrl(paymentUrl);
                    startStatusChecking();
                })
                .addOnFailureListener(e -> {
                    showError("Failed to update order: " + e.getMessage());
                });
    }

    private void loadPaymentUrl(String paymentUrl) {
        binding.textStatus.setText("Loading payment page...");
        binding.webView.loadUrl(paymentUrl);
    }

    private void startStatusChecking() {
        statusCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (!paymentCompleted) {
                    checkPaymentStatus();
                    statusCheckHandler.postDelayed(this, 10000); // Check every 10 seconds
                }
            }
        };
        statusCheckHandler.postDelayed(statusCheckRunnable, 10000);
    }

    private void handlePaymentReturn(Uri uri) {
        // Parse payment result from ToyyibPay return URL
        String statusId = uri.getQueryParameter("status_id");
        String msg = uri.getQueryParameter("msg");
        String transactionId = uri.getQueryParameter("transaction_id");
        String billCode = uri.getQueryParameter("billcode");
        
        runOnUiThread(() -> {
            if ("1".equals(statusId) && "ok".equals(msg) && transactionId != null) {
                // Payment successful
                if (!paymentCompleted) {
                    paymentCompleted = true;
                    stopStatusChecking();
                    
                    // Update bill code if available
                    if (billCode != null && currentOrder != null) {
                        currentOrder.setBillCode(billCode);
                    }
                    
                    // Use the total amount from the order as paid amount
                    double paidAmount = currentOrder != null ? currentOrder.getTotalAmount() : 0.0;
                    handlePaymentSuccess(transactionId, paidAmount);
                }
            } else {
                // Payment failed or cancelled
                if (!paymentCompleted) {
                    paymentCompleted = true;
                    stopStatusChecking();
                    handlePaymentFailure();
                }
            }
            
            // Update status text
            String statusText = ("1".equals(statusId) && "ok".equals(msg)) ? "PAID" : "FAILED";
            binding.textStatus.setText("Payment Status: " + statusText);
        });
    }

    private void checkPaymentStatus() {
        if (currentOrder == null || currentOrder.getBillCode() == null) return;

        toyyibPayService.getBillStatus(currentOrder.getBillCode(), new ToyyibPayService.BillStatusCallback() {
            @Override
            public void onSuccess(String billStatus, String transactionId, double paidAmount) {
                runOnUiThread(() -> {
                    String paymentStatus = ToyyibPayService.convertToPaymentStatus(billStatus);
                    
                    if ("paid".equals(paymentStatus) && !paymentCompleted) {
                        paymentCompleted = true;
                        stopStatusChecking();
                        handlePaymentSuccess(transactionId, paidAmount);
                    } else if ("failed".equals(paymentStatus)) {
                        paymentCompleted = true;
                        stopStatusChecking();
                        handlePaymentFailure();
                    }
                    
                    // Update status text
                    binding.textStatus.setText("Payment Status: " + paymentStatus.toUpperCase());
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error checking payment status: " + error);
                });
            }
        });
    }

    private void handlePaymentSuccess(String transactionId, double paidAmount) {
        // Update order with payment success
        currentOrder.setPaymentStatus(Order.PaymentStatus.PAID);
        currentOrder.setTransactionId(transactionId);
        currentOrder.setPaidAmount(paidAmount);
        currentOrder.setPaymentDate(System.currentTimeMillis());

        db.collection("orders").document(orderId)
                .set(currentOrder)
                .addOnSuccessListener(aVoid -> {
                    // Generate invoice
                    generateInvoice();
                })
                .addOnFailureListener(e -> {
                    showPaymentSuccessDialog(false);
                });
    }

    private void generateInvoice() {
        binding.textStatus.setText("Generating invoice...");
        
        invoiceGenerator.generateInvoice(currentOrder, new InvoiceGeneratorService.InvoiceGenerationCallback() {
            @Override
            public void onSuccess(String filePath) {
                runOnUiThread(() -> {
                    // Update order with invoice path
                    currentOrder.setInvoicePath(filePath);
                    
                    db.collection("orders").document(orderId)
                            .set(currentOrder)
                            .addOnSuccessListener(aVoid -> {
                                showPaymentSuccessDialog(true);
                            })
                            .addOnFailureListener(e -> {
                                showPaymentSuccessDialog(false);
                            });
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Failed to generate invoice: " + error);
                    showPaymentSuccessDialog(false);
                });
            }
        });
    }

    private void handlePaymentFailure() {
        currentOrder.setPaymentStatus(Order.PaymentStatus.FAILED);
        
        db.collection("orders").document(orderId)
                .set(currentOrder)
                .addOnSuccessListener(aVoid -> {
                    showPaymentFailureDialog();
                })
                .addOnFailureListener(e -> {
                    showPaymentFailureDialog();
                });
    }

    private void showPaymentSuccessDialog(boolean invoiceGenerated) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payment Successful!");
        
        String message = "Your payment has been processed successfully.\n\nOrder: " + 
                        currentOrder.getFormattedOrderId() + 
                        "\nAmount Paid: RM " + String.format("%.2f", currentOrder.getPaidAmount());
        
        if (invoiceGenerated) {
            message += "\n\nInvoice has been generated and saved.";
        }
        
        builder.setMessage(message)
                .setPositiveButton("View Order", (dialog, which) -> {
                    Intent intent = new Intent(this, UserOrderDetailsActivity.class);
                    intent.putExtra("order_id", orderId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false);
        
        builder.show();
    }

    private void showPaymentFailureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payment Failed")
                .setMessage("Your payment could not be processed. Please try again or contact support.")
                .setPositiveButton("Try Again", (dialog, which) -> {
                    // Reset payment status and try again
                    currentOrder.setPaymentStatus(Order.PaymentStatus.PENDING);
                    currentOrder.setBillCode(null);
                    currentOrder.setPaymentUrl(null);
                    createPaymentBill();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false);
        
        builder.show();
    }

    private void showCancelPaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Payment")
                .setMessage("Are you sure you want to cancel the payment? Your order will remain unpaid.")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Continue Payment", null);
        
        builder.show();
    }

    private void showError(String message) {
        binding.progressBar.setVisibility(android.view.View.GONE);
        binding.textStatus.setText("Error: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void stopStatusChecking() {
        if (statusCheckHandler != null && statusCheckRunnable != null) {
            statusCheckHandler.removeCallbacks(statusCheckRunnable);
        }
    }

    @Override
    public void onBackPressed() {
        showCancelPaymentDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopStatusChecking();
        binding = null;
    }
}
