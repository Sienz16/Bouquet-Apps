package com.example.bouquetbliss.admin.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.admin.dashboard.AdminDashboardActivity;
import com.example.bouquetbliss.admin.orders.AdminOrderManagementActivity;
import com.example.bouquetbliss.admin.profile.AdminProfileActivity;
import com.example.bouquetbliss.admin.reports.ReportsActivity;
import com.example.bouquetbliss.admin.stock.StockManagementActivity;
import com.example.bouquetbliss.common.auth.LoginActivity;
import com.example.bouquetbliss.utils.UITranslator;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseAdminNavigationActivity extends AppCompatActivity {

    protected UITranslator uiTranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiTranslator = new UITranslator(this);
    }

    protected void applyTranslations() {
        // Apply translations to the current activity
        uiTranslator.translateActivity();
        
        // Preload common translations for better performance
        uiTranslator.translateCommonElements();
    }

    protected void setupAdminBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navOrders = findViewById(R.id.navOrders);
        LinearLayout navStock = findViewById(R.id.navStock);
        LinearLayout navReports = findViewById(R.id.navReports);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> navigateToHome());
        navOrders.setOnClickListener(v -> navigateToOrders());
        navStock.setOnClickListener(v -> navigateToStock());
        navReports.setOnClickListener(v -> navigateToReports());
        navProfile.setOnClickListener(v -> showProfileMenu());
    }

    protected void setSelectedNavItem(String currentPage) {
        // Reset all items to default state
        resetNavItem(R.id.navHome);
        resetNavItem(R.id.navOrders);
        resetNavItem(R.id.navStock);
        resetNavItem(R.id.navReports);
        resetNavItem(R.id.navProfile);

        // Set the current page as selected
        switch (currentPage) {
            case "home":
                setNavItemSelected(R.id.navHome);
                break;
            case "orders":
                setNavItemSelected(R.id.navOrders);
                break;
            case "stock":
                setNavItemSelected(R.id.navStock);
                break;
            case "reports":
                setNavItemSelected(R.id.navReports);
                break;
            case "profile":
                setNavItemSelected(R.id.navProfile);
                break;
        }
    }

    private void resetNavItem(int navId) {
        LinearLayout navItem = findViewById(navId);
        if (navItem != null) {
            // Find ImageView and TextView within the nav item
            updateNavItemViews(navItem, R.color.text_secondary, android.graphics.Typeface.NORMAL);
        }
    }

    private void setNavItemSelected(int navId) {
        LinearLayout navItem = findViewById(navId);
        if (navItem != null) {
            // Find ImageView and TextView within the nav item  
            updateNavItemViews(navItem, R.color.pink_primary, android.graphics.Typeface.BOLD);
        }
    }

    private void updateNavItemViews(LinearLayout navItem, int colorRes, int typeface) {
        for (int i = 0; i < navItem.getChildCount(); i++) {
            View child = navItem.getChildAt(i);
            if (child instanceof ImageView) {
                ((ImageView) child).setColorFilter(getResources().getColor(colorRes));
            } else if (child instanceof TextView) {
                ((TextView) child).setTextColor(getResources().getColor(colorRes));
                ((TextView) child).setTypeface(null, typeface);
            } else if (child instanceof LinearLayout) {
                // Handle nested layouts (for nested ImageViews and TextViews)
                updateNavItemViews((LinearLayout) child, colorRes, typeface);
            }
        }
    }

    private void navigateToHome() {
        if (!(this instanceof AdminDashboardActivity)) {
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "You're already on Home", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToOrders() {
        if (!(this instanceof AdminOrderManagementActivity)) {
            Intent intent = new Intent(this, AdminOrderManagementActivity.class);
            startActivity(intent);
        }
    }

    private void navigateToStock() {
        if (!(this instanceof StockManagementActivity)) {
            Intent intent = new Intent(this, StockManagementActivity.class);
            startActivity(intent);
        }
    }

    private void navigateToReports() {
        if (!(this instanceof ReportsActivity)) {
            Intent intent = new Intent(this, ReportsActivity.class);
            startActivity(intent);
        }
    }

    private void showProfileMenu() {
        String[] options = {"View Profile", "Settings", "Logout"};
        
        new AlertDialog.Builder(this)
                .setTitle("Profile Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // View Profile
                            navigateToProfile();
                            break;
                        case 1: // Settings
                            Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show();
                            break;
                        case 2: // Logout
                            showLogoutConfirmation();
                            break;
                    }
                })
                .show();
    }

    private void navigateToProfile() {
        if (!(this instanceof AdminProfileActivity)) {
            Intent intent = new Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        }
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();
    }
}
