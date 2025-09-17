package com.example.bouquetbliss.admin.reports;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.adapters.BookingHistoryAdapter;
import com.example.bouquetbliss.admin.base.BaseAdminNavigationActivity;
import com.example.bouquetbliss.databinding.ActivityReportsBinding;
import com.example.bouquetbliss.models.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity for generating reports and viewing booking history
 */
public class ReportsActivity extends BaseAdminNavigationActivity {
    
    private ActivityReportsBinding binding;
    private FirebaseFirestore db;
    private BookingHistoryAdapter historyAdapter;
    private List<Order> orderList;
    private List<Order> filteredOrderList;
    
    private Calendar startDate;
    private Calendar endDate;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat displayDateFormat;
    
    // Activity result launcher for creating PDF file
    private ActivityResultLauncher<Intent> createDocumentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();
        filteredOrderList = new ArrayList<>();
        
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        
        // Set default date range to current month
        startDate.set(Calendar.DAY_OF_MONTH, 1);
        
        // Initialize the document creation launcher
        createDocumentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        generatePdfToUri(uri);
                    }
                }
            }
        );
        
        setupViews();
        setupClickListeners();
        setupAdminBottomNavigation();
        setSelectedNavItem("reports");
        
        loadBookingHistory();
        updateDateDisplay();
    }

    private void setupViews() {
        // Setup RecyclerView for booking history
        historyAdapter = new BookingHistoryAdapter(filteredOrderList);
        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewHistory.setAdapter(historyAdapter);
        
        // Initialize statistics
        updateStatistics();
    }

    private void setupClickListeners() {
        // Date range selection
        binding.btnStartDate.setOnClickListener(v -> showStartDatePicker());
        binding.btnEndDate.setOnClickListener(v -> showEndDatePicker());
        
        // Filter buttons
        binding.btnFilterAll.setOnClickListener(v -> filterOrders("ALL"));
        binding.btnFilterPending.setOnClickListener(v -> filterOrders("PENDING"));
        binding.btnFilterCompleted.setOnClickListener(v -> filterOrders("COMPLETED"));
        binding.btnFilterRejected.setOnClickListener(v -> filterOrders("REJECTED"));
        
        // Report generation buttons
        binding.btnGenerateMonthlyReport.setOnClickListener(v -> generateMonthlyReport());
        binding.btnGenerateCustomReport.setOnClickListener(v -> generateCustomReport());
        binding.btnExportToPdf.setOnClickListener(v -> exportHistoryToPdf());
        
        // Refresh button
        binding.btnRefresh.setOnClickListener(v -> {
            loadBookingHistory();
            Toast.makeText(this, "Data refreshed", Toast.LENGTH_SHORT).show();
        });
    }

    private void showStartDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                startDate.set(year, month, dayOfMonth);
                updateDateDisplay();
                filterOrdersByDateRange();
            },
            startDate.get(Calendar.YEAR),
            startDate.get(Calendar.MONTH),
            startDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                endDate.set(year, month, dayOfMonth);
                updateDateDisplay();
                filterOrdersByDateRange();
            },
            endDate.get(Calendar.YEAR),
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        binding.btnStartDate.setText(displayDateFormat.format(startDate.getTime()));
        binding.btnEndDate.setText(displayDateFormat.format(endDate.getTime()));
    }

    private void loadBookingHistory() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        db.collection("orders")
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                orderList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Order order = document.toObject(Order.class);
                    order.setId(document.getId());
                    orderList.add(order);
                }
                
                filterOrdersByDateRange();
                updateStatistics();
                binding.progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load booking history: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            });
    }

    private void filterOrdersByDateRange() {
        filteredOrderList.clear();
        
        long startTime = startDate.getTimeInMillis();
        long endTime = endDate.getTimeInMillis() + (24 * 60 * 60 * 1000 - 1); // End of day
        
        for (Order order : orderList) {
            long orderTime = order.getOrderDate();
            if (orderTime >= startTime && orderTime <= endTime) {
                filteredOrderList.add(order);
            }
        }
        
        historyAdapter.notifyDataSetChanged();
        updateStatistics();
    }

    private void filterOrders(String status) {
        // Reset filter buttons
        resetFilterButtons();
        
        // Set active filter button
        switch (status) {
            case "ALL":
                binding.btnFilterAll.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.pink_primary));
                break;
            case "PENDING":
                binding.btnFilterPending.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.pink_primary));
                break;
            case "COMPLETED":
                binding.btnFilterCompleted.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.pink_primary));
                break;
            case "REJECTED":
                binding.btnFilterRejected.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.pink_primary));
                break;
        }
        
        // Apply filter
        List<Order> tempList = new ArrayList<>();
        for (Order order : filteredOrderList) {
            if (status.equals("ALL") || order.getStatus().equalsIgnoreCase(status)) {
                tempList.add(order);
            }
        }
        
        historyAdapter.updateOrders(tempList);
        updateFilteredStatistics(tempList);
    }

    private void resetFilterButtons() {
        binding.btnFilterAll.setBackgroundTintList(
            ContextCompat.getColorStateList(this, R.color.text_secondary));
        binding.btnFilterPending.setBackgroundTintList(
            ContextCompat.getColorStateList(this, R.color.text_secondary));
        binding.btnFilterCompleted.setBackgroundTintList(
            ContextCompat.getColorStateList(this, R.color.text_secondary));
        binding.btnFilterRejected.setBackgroundTintList(
            ContextCompat.getColorStateList(this, R.color.text_secondary));
    }

    private void updateStatistics() {
        int totalOrders = filteredOrderList.size();
        int pendingOrders = 0;
        int completedOrders = 0;
        int rejectedOrders = 0;
        double totalRevenue = 0.0;
        
        for (Order order : filteredOrderList) {
            switch (order.getStatus()) {
                case Order.Status.PENDING:
                    pendingOrders++;
                    break;
                case Order.Status.COMPLETED:
                    completedOrders++;
                    totalRevenue += order.getTotalAmount();
                    break;
                case Order.Status.REJECTED:
                    rejectedOrders++;
                    break;
            }
        }
        
        binding.tvTotalOrders.setText(String.valueOf(totalOrders));
        binding.tvPendingOrders.setText(String.valueOf(pendingOrders));
        binding.tvCompletedOrders.setText(String.valueOf(completedOrders));
        binding.tvRejectedOrders.setText(String.valueOf(rejectedOrders));
        binding.tvTotalRevenue.setText(String.format("RM %.2f", totalRevenue));
    }

    private void updateFilteredStatistics(List<Order> orders) {
        int totalOrders = orders.size();
        double totalRevenue = 0.0;
        
        for (Order order : orders) {
            if (order.getStatus().equals(Order.Status.COMPLETED)) {
                totalRevenue += order.getTotalAmount();
            }
        }
        
        binding.tvFilteredCount.setText("Showing " + totalOrders + " orders");
        binding.tvFilteredRevenue.setText(String.format("Revenue: RM %.2f", totalRevenue));
        binding.tvFilteredCount.setVisibility(View.VISIBLE);
        binding.tvFilteredRevenue.setVisibility(View.VISIBLE);
    }

    private void generateMonthlyReport() {
        // Set date range to current month
        Calendar now = Calendar.getInstance();
        startDate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1);
        endDate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 
            now.getActualMaximum(Calendar.DAY_OF_MONTH));
        
        updateDateDisplay();
        filterOrdersByDateRange();
        
        Toast.makeText(this, "Monthly report generated for " + 
            displayDateFormat.format(startDate.getTime()), Toast.LENGTH_SHORT).show();
    }

    private void generateCustomReport() {
        if (startDate.after(endDate)) {
            Toast.makeText(this, "Start date cannot be after end date", Toast.LENGTH_SHORT).show();
            return;
        }
        
        filterOrdersByDateRange();
        Toast.makeText(this, "Custom report generated for selected date range", 
            Toast.LENGTH_SHORT).show();
    }

    private void exportHistoryToPdf() {
        String fileName = "BouquetBliss_Report_" + dateFormat.format(new Date()) + ".pdf";
        
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        
        createDocumentLauncher.launch(intent);
    }

    private void generatePdfToUri(Uri uri) {
        try {
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            
            // Title
            paint.setTextSize(24);
            paint.setFakeBoldText(true);
            canvas.drawText("Bouquet Bliss - Booking Report", 50, 50, paint);
            
            // Date range
            paint.setTextSize(14);
            paint.setFakeBoldText(false);
            canvas.drawText("Report Period: " + binding.btnStartDate.getText() + 
                " to " + binding.btnEndDate.getText(), 50, 80, paint);
            
            // Statistics
            paint.setTextSize(16);
            paint.setFakeBoldText(true);
            canvas.drawText("Summary Statistics", 50, 120, paint);
            
            paint.setTextSize(12);
            paint.setFakeBoldText(false);
            canvas.drawText("Total Orders: " + binding.tvTotalOrders.getText(), 50, 150, paint);
            canvas.drawText("Completed Orders: " + binding.tvCompletedOrders.getText(), 50, 170, paint);
            canvas.drawText("Pending Orders: " + binding.tvPendingOrders.getText(), 50, 190, paint);
            canvas.drawText("Rejected Orders: " + binding.tvRejectedOrders.getText(), 50, 210, paint);
            canvas.drawText("Total Revenue: " + binding.tvTotalRevenue.getText(), 50, 230, paint);
            
            // Order details header
            paint.setTextSize(16);
            paint.setFakeBoldText(true);
            canvas.drawText("Order Details", 50, 280, paint);
            
            // Order list
            paint.setTextSize(10);
            paint.setFakeBoldText(false);
            int yPosition = 310;
            
            for (Order order : filteredOrderList) {
                if (yPosition > 800) break; // Prevent overflow
                
                String orderLine = order.getFormattedOrderId() + " | " + 
                    order.getCustomerName() + " | " + 
                    order.getStatus() + " | RM " + 
                    String.format("%.2f", order.getTotalAmount());
                
                canvas.drawText(orderLine, 50, yPosition, paint);
                yPosition += 15;
            }
            
            pdfDocument.finishPage(page);
            
            // Save PDF to the selected URI
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            pdfDocument.writeTo(outputStream);
            pdfDocument.close();
            outputStream.close();
            
            Toast.makeText(this, "PDF exported successfully!", Toast.LENGTH_LONG).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
