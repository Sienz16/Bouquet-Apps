package com.example.bouquetbliss.admin.stock;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.adapters.StockReceivingAdapter;
import com.example.bouquetbliss.admin.base.BaseAdminNavigationActivity;
import com.example.bouquetbliss.databinding.ActivityStockReceivingBinding;
import com.example.bouquetbliss.models.Bouquet;
import com.example.bouquetbliss.models.StockTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for receiving and updating stock for bouquet orders
 */
public class StockReceivingActivity extends BaseAdminNavigationActivity implements StockReceivingAdapter.OnStockActionListener {

    private ActivityStockReceivingBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StockReceivingAdapter adapter;
    private List<Bouquet> bouquetList;
    private List<Bouquet> filteredList;
    
    private String selectedCategory = "All Categories";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStockReceivingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        bouquetList = new ArrayList<>();
        filteredList = new ArrayList<>();

        setupViews();
        setupRecyclerView();
        setupSpinner();
        setupClickListeners();
        setupAdminBottomNavigation();
        setSelectedNavItem("stock");
        loadBouquets();
    }

    private void setupViews() {
        binding.layoutEmptyState.setVisibility(View.GONE);
        
        // Setup search functionality
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                filterBouquets();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new StockReceivingAdapter(filteredList, this);
        binding.recyclerViewStock.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewStock.setAdapter(adapter);
    }

    private void setupSpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        categories.add("Low Stock Only");
        categories.add("Out of Stock");
        for (String category : Bouquet.Categories.getAllCategories()) {
            categories.add(category);
        }
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories.get(position);
                filterBouquets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnBulkReceive.setOnClickListener(v -> showBulkReceiveDialog());
        
        binding.btnViewTransactions.setOnClickListener(v -> {
            // TODO: Navigate to stock transaction history
            Toast.makeText(this, "Transaction history coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadBouquets() {
        db.collection("bouquets")
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bouquetList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Bouquet bouquet = document.toObject(Bouquet.class);
                        bouquet.setId(document.getId());
                        bouquetList.add(bouquet);
                    }
                    filterBouquets();
                    updateStatistics();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load bouquets: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }

    private void filterBouquets() {
        filteredList.clear();

        for (Bouquet bouquet : bouquetList) {
            boolean matchesCategory = false;
            boolean matchesSearch = searchQuery.isEmpty() || 
                bouquet.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                bouquet.getCategory().toLowerCase().contains(searchQuery.toLowerCase());

            switch (selectedCategory) {
                case "All Categories":
                    matchesCategory = true;
                    break;
                case "Low Stock Only":
                    matchesCategory = bouquet.getQuantity() <= 5 && bouquet.getQuantity() > 0;
                    break;
                case "Out of Stock":
                    matchesCategory = bouquet.getQuantity() <= 0;
                    break;
                default:
                    matchesCategory = bouquet.getCategory().equals(selectedCategory);
                    break;
            }

            if (matchesCategory && matchesSearch) {
                filteredList.add(bouquet);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateStatistics() {
        int totalItems = bouquetList.size();
        int lowStockItems = 0;
        int outOfStockItems = 0;

        for (Bouquet bouquet : bouquetList) {
            if (bouquet.getQuantity() <= 0) {
                outOfStockItems++;
            } else if (bouquet.getQuantity() <= 5) {
                lowStockItems++;
            }
        }

        binding.tvTotalItems.setText(String.valueOf(totalItems));
        binding.tvLowStock.setText(String.valueOf(lowStockItems));
        binding.tvOutOfStock.setText(String.valueOf(outOfStockItems));
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            binding.recyclerViewStock.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            
            if (!searchQuery.isEmpty()) {
                binding.tvEmptyMessage.setText("No bouquets found matching your search");
            } else {
                binding.tvEmptyMessage.setText("No bouquets available for the selected category");
            }
        } else {
            binding.recyclerViewStock.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void showBulkReceiveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bulk Stock Receive");
        builder.setMessage("This feature allows you to receive stock for multiple bouquets at once. Coming soon!");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    // StockReceivingAdapter.OnStockActionListener implementations
    @Override
    public void onReceiveStock(Bouquet bouquet) {
        showReceiveStockDialog(bouquet);
    }

    @Override
    public void onAdjustStock(Bouquet bouquet) {
        showAdjustStockDialog(bouquet);
    }

    @Override
    public void onViewStockHistory(Bouquet bouquet) {
        Intent intent = new Intent(this, StockHistoryActivity.class);
        intent.putExtra("bouquet_id", bouquet.getId());
        intent.putExtra("bouquet_name", bouquet.getName());
        startActivity(intent);
    }

    private void showReceiveStockDialog(Bouquet bouquet) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_receive_stock, null);
        
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        EditText etReason = dialogView.findViewById(R.id.etReason);
        
        etReason.setText("New stock delivery");

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Receive Stock: " + bouquet.getName())
            .setView(dialogView)
            .setPositiveButton("Receive", null)
            .setNegativeButton("Cancel", null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String quantityStr = etQuantity.getText().toString().trim();
                String reason = etReason.getText().toString().trim();

                if (quantityStr.isEmpty()) {
                    etQuantity.setError("Please enter quantity");
                    return;
                }

                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity <= 0) {
                        etQuantity.setError("Quantity must be greater than 0");
                        return;
                    }

                    receiveStock(bouquet, quantity, reason);
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    etQuantity.setError("Please enter a valid number");
                }
            });
        });

        dialog.show();
    }

    private void showAdjustStockDialog(Bouquet bouquet) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_adjust_stock, null);
        
        EditText etNewQuantity = dialogView.findViewById(R.id.etNewQuantity);
        EditText etReason = dialogView.findViewById(R.id.etReason);
        
        etNewQuantity.setText(String.valueOf(bouquet.getQuantity()));

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Adjust Stock: " + bouquet.getName())
            .setView(dialogView)
            .setPositiveButton("Adjust", null)
            .setNegativeButton("Cancel", null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String quantityStr = etNewQuantity.getText().toString().trim();
                String reason = etReason.getText().toString().trim();

                if (quantityStr.isEmpty()) {
                    etNewQuantity.setError("Please enter quantity");
                    return;
                }

                if (reason.isEmpty()) {
                    etReason.setError("Please enter adjustment reason");
                    return;
                }

                try {
                    int newQuantity = Integer.parseInt(quantityStr);
                    if (newQuantity < 0) {
                        etNewQuantity.setError("Quantity cannot be negative");
                        return;
                    }

                    adjustStock(bouquet, newQuantity, reason);
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    etNewQuantity.setError("Please enter a valid number");
                }
            });
        });

        dialog.show();
    }

    private void receiveStock(Bouquet bouquet, int quantity, String reason) {
        int previousQuantity = bouquet.getQuantity();
        int newQuantity = previousQuantity + quantity;
        
        updateBouquetStock(bouquet, newQuantity, StockTransaction.TransactionType.STOCK_IN, 
                          quantity, previousQuantity, reason);
    }

    private void adjustStock(Bouquet bouquet, int newQuantity, String reason) {
        int previousQuantity = bouquet.getQuantity();
        int quantityChange = newQuantity - previousQuantity;
        
        StockTransaction.TransactionType transactionType = quantityChange > 0 ? 
            StockTransaction.TransactionType.STOCK_IN : 
            StockTransaction.TransactionType.ADJUSTMENT;
            
        updateBouquetStock(bouquet, newQuantity, transactionType, 
                          quantityChange, previousQuantity, reason);
    }

    private void updateBouquetStock(Bouquet bouquet, int newQuantity, 
                                   StockTransaction.TransactionType transactionType,
                                   int quantityChange, int previousQuantity, String reason) {
        
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update bouquet quantity
        bouquet.setQuantity(newQuantity);
        
        // Prepare updates for Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("quantity", newQuantity);
        updates.put("availability", bouquet.getAvailability());
        updates.put("updatedAt", System.currentTimeMillis());

        // Update bouquet in Firestore
        db.collection("bouquets").document(bouquet.getId())
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                // Create stock transaction record
                createStockTransaction(bouquet, transactionType, quantityChange, 
                                     previousQuantity, newQuantity, reason, 
                                     currentUser.getUid(), currentUser.getDisplayName());
                
                // Update local data and UI
                loadBouquets(); // Refresh the list
                Toast.makeText(this, "Stock updated successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update stock: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void createStockTransaction(Bouquet bouquet, StockTransaction.TransactionType type,
                                      int quantityChange, int previousQuantity, int newQuantity,
                                      String reason, String adminId, String adminName) {
        
        StockTransaction transaction = new StockTransaction(
            bouquet.getId(),
            bouquet.getName(),
            type,
            quantityChange,
            previousQuantity,
            newQuantity,
            reason,
            adminId,
            adminName != null ? adminName : "Admin"
        );

        db.collection("stock_transactions")
            .add(transaction)
            .addOnFailureListener(e -> {
                // Log the error but don't show to user as main operation succeeded
                e.printStackTrace();
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
