package com.example.bouquetbliss.user.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.bouquetbliss.databinding.ActivityBouquetDetailBinding;
import com.example.bouquetbliss.models.Bouquet;
import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.models.OrderItem;
import com.example.bouquetbliss.user.base.BaseUserNavigationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BouquetDetailActivity extends BaseUserNavigationActivity {

    private ActivityBouquetDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Bouquet currentBouquet;
    private int selectedQuantity = 1;
    private String flowerColorPreference = "";
    private String specialInstructions = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBouquetDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupUserBottomNavigation();
        setSelectedNavItem("browse");

        // Get bouquet data from intent
        getBouquetFromIntent();
        setupClickListeners();
        
        // Apply translations
        applyTranslations();
    }

    private void getBouquetFromIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra("bouquet_id")) {
            String bouquetId = intent.getStringExtra("bouquet_id");
            loadBouquetDetails(bouquetId);
        } else if (intent.hasExtra("bouquet_name")) {
            // For direct bouquet object passing (fallback)
            String name = intent.getStringExtra("bouquet_name");
            String description = intent.getStringExtra("bouquet_description");
            double price = intent.getDoubleExtra("bouquet_price", 0.0);
            int quantity = intent.getIntExtra("bouquet_quantity", 0);
            String category = intent.getStringExtra("bouquet_category");
            String size = intent.getStringExtra("bouquet_size");
            
            currentBouquet = new Bouquet();
            currentBouquet.setName(name);
            currentBouquet.setDescription(description);
            currentBouquet.setPrice(price);
            currentBouquet.setQuantity(quantity);
            currentBouquet.setCategory(category);
            currentBouquet.setSize(size);
            
            displayBouquetDetails();
        }
    }

    private void loadBouquetDetails(String bouquetId) {
        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        
        db.collection("bouquets").document(bouquetId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    binding.progressBar.setVisibility(android.view.View.GONE);
                    
                    if (documentSnapshot.exists()) {
                        currentBouquet = documentSnapshot.toObject(Bouquet.class);
                        if (currentBouquet != null) {
                            currentBouquet.setId(documentSnapshot.getId());
                            displayBouquetDetails();
                        }
                    } else {
                        Toast.makeText(this, "Bouquet not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Error loading bouquet: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayBouquetDetails() {
        if (currentBouquet == null) return;

        // Set bouquet information
        binding.textBouquetName.setText(currentBouquet.getName());
        binding.textPrice.setText(String.format("RM %.2f", currentBouquet.getPrice()));
        binding.textDescription.setText(currentBouquet.getDescription());
        binding.textCategory.setText(currentBouquet.getCategory());
        binding.textSize.setText(currentBouquet.getSize());
        
        // Set stock status
        if (currentBouquet.getQuantity() > 0) {
            binding.textStock.setText(String.format("%d in stock", currentBouquet.getQuantity()));
            binding.textStock.setTextColor(getResources().getColor(com.example.bouquetbliss.R.color.success));
            binding.btnRequestOrder.setEnabled(true);
        } else {
            binding.textStock.setText("Out of stock");
            binding.textStock.setTextColor(getResources().getColor(com.example.bouquetbliss.R.color.error));
            binding.btnRequestOrder.setEnabled(false);
        }

        // Set availability status
        binding.textAvailability.setText(currentBouquet.getAvailability());
        
        // Set flower types if available
        if (currentBouquet.getFlowerTypes() != null && !currentBouquet.getFlowerTypes().isEmpty()) {
            binding.textFlowerTypes.setText(String.join(", ", currentBouquet.getFlowerTypes()));
        } else {
            binding.textFlowerTypes.setText("Not specified");
        }

        // Set occasion
        if (currentBouquet.getOccasion() != null && !currentBouquet.getOccasion().isEmpty()) {
            binding.textOccasion.setText(currentBouquet.getOccasion());
        } else {
            binding.textOccasion.setText("General");
        }

        // Update quantity selector
        updateQuantityDisplay();
        
        // TODO: Load images if URLs are available
        // For now, set placeholder
        binding.imageBouquet.setImageResource(com.example.bouquetbliss.R.drawable.image_placeholder_background);
    }

    private void setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Quantity controls
        binding.btnDecrease.setOnClickListener(v -> {
            if (selectedQuantity > 1) {
                selectedQuantity--;
                updateQuantityDisplay();
            }
        });

        binding.btnIncrease.setOnClickListener(v -> {
            if (currentBouquet != null && selectedQuantity < currentBouquet.getQuantity()) {
                selectedQuantity++;
                updateQuantityDisplay();
            } else {
                Toast.makeText(this, "Cannot exceed available stock", Toast.LENGTH_SHORT).show();
            }
        });

        // Request order button
        binding.btnRequestOrder.setOnClickListener(v -> showOrderRequestDialog());
    }

    private void updateQuantityDisplay() {
        binding.textQuantity.setText(String.valueOf(selectedQuantity));
        
        if (currentBouquet != null) {
            double totalPrice = currentBouquet.getPrice() * selectedQuantity;
            binding.textTotalPrice.setText(String.format("Total: RM %.2f", totalPrice));
        }
    }

    private void showOrderRequestDialog() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to place an order", Toast.LENGTH_SHORT).show();
            return;
        }

        // First show order customization dialog
        showOrderCustomizationDialog();
    }

    private void showOrderCustomizationDialog() {
        // Create custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(com.example.bouquetbliss.R.layout.dialog_order_customization, null);
        
        EditText etFlowerColor = dialogView.findViewById(com.example.bouquetbliss.R.id.etFlowerColor);
        EditText etSpecialInstructions = dialogView.findViewById(com.example.bouquetbliss.R.id.etSpecialInstructions);
        
        // Pre-fill with existing values if any
        etFlowerColor.setText(flowerColorPreference);
        etSpecialInstructions.setText(specialInstructions);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Customize Your Order")
                .setView(dialogView)
                .setPositiveButton("Continue", (dialog, which) -> {
                    flowerColorPreference = etFlowerColor.getText().toString().trim();
                    specialInstructions = etSpecialInstructions.getText().toString().trim();
                    showOrderConfirmationDialog();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showOrderConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Order Request");
        
        StringBuilder message = new StringBuilder();
        message.append(String.format("Bouquet: %s\nQuantity: %d\nTotal Price: RM %.2f", 
                currentBouquet.getName(), selectedQuantity, currentBouquet.getPrice() * selectedQuantity));
        
        if (!flowerColorPreference.isEmpty()) {
            message.append("\nFlower Colors: ").append(flowerColorPreference);
        }
        
        if (!specialInstructions.isEmpty()) {
            message.append("\nSpecial Instructions: ").append(specialInstructions);
        }
        
        message.append("\n\nDo you want to submit this order request?");
        
        builder.setMessage(message.toString())
                .setPositiveButton("Submit Request", (dialog, which) -> submitOrderRequest())
                .setNegativeButton("Back", (dialog, which) -> showOrderCustomizationDialog());
        
        builder.show();
    }

    private void submitOrderRequest() {
        if (auth.getCurrentUser() == null) return;

        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        binding.btnRequestOrder.setEnabled(false);

        // Create order item
        String imageUrl = "";
        if (currentBouquet.getImageUrls() != null && !currentBouquet.getImageUrls().isEmpty()) {
            imageUrl = currentBouquet.getImageUrls().get(0);
        }
        
        OrderItem orderItem = new OrderItem(
                currentBouquet.getId(),
                currentBouquet.getName(),
                imageUrl,
                currentBouquet.getPrice(),
                selectedQuantity
        );

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);

        // Create order
        String customerName = auth.getCurrentUser().getDisplayName();
        if (customerName == null || customerName.isEmpty()) {
            customerName = auth.getCurrentUser().getEmail();
        }
        
        Order order = new Order(
                auth.getCurrentUser().getUid(),
                customerName,
                "", // Phone number - to be collected later
                auth.getCurrentUser().getEmail(),
                orderItems,
                currentBouquet.getPrice() * selectedQuantity,
                System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000), // Delivery date: 7 days from now
                "To be confirmed", // Delivery address - to be collected later
                specialInstructions, // Special instructions
                flowerColorPreference // Flower color preference
        );

        // Submit to Firebase
        db.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    binding.progressBar.setVisibility(android.view.View.GONE);
                    binding.btnRequestOrder.setEnabled(true);
                    
                    Toast.makeText(this, "Order request submitted successfully!", Toast.LENGTH_LONG).show();
                    
                    // Show success dialog with order ID
                    showSuccessDialog(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(android.view.View.GONE);
                    binding.btnRequestOrder.setEnabled(true);
                    
                    Toast.makeText(this, "Failed to submit order: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showSuccessDialog(String orderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Request Submitted!")
                .setMessage("Your order request has been submitted successfully.\n\nOrder ID: " + orderId + 
                           "\n\nYou will be notified when the admin reviews your request.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false);
        
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
