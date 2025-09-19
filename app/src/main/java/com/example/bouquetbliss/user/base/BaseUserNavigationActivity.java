package com.example.bouquetbliss.user.base;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.user.dashboard.UserDashboardActivity;
import com.example.bouquetbliss.user.browse.BrowseBouquetsActivity;
import com.example.bouquetbliss.user.orders.UserOrdersActivity;
import com.example.bouquetbliss.common.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseUserNavigationActivity extends AppCompatActivity {

    protected void setupUserBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navBrowse = findViewById(R.id.navBrowse);
        LinearLayout navOrders = findViewById(R.id.navOrders);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> navigateToHome());
        navBrowse.setOnClickListener(v -> navigateToBrowse());
        navOrders.setOnClickListener(v -> navigateToOrders());
        navProfile.setOnClickListener(v -> showProfileMenu());
    }

    protected void setSelectedNavItem(String currentPage) {
        // Reset all items to default state
        resetNavItem(R.id.navHome);
        resetNavItem(R.id.navBrowse);
        resetNavItem(R.id.navOrders);
        resetNavItem(R.id.navProfile);

        // Set the current page as selected
        switch (currentPage) {
            case "home":
                setNavItemSelected(R.id.navHome);
                break;
            case "browse":
                setNavItemSelected(R.id.navBrowse);
                break;
            case "orders":
                setNavItemSelected(R.id.navOrders);
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
        if (!(this instanceof UserDashboardActivity)) {
            Intent intent = new Intent(this, UserDashboardActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "You're already on Home", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToBrowse() {
        if (!(this instanceof BrowseBouquetsActivity)) {
            Intent intent = new Intent(this, BrowseBouquetsActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "You're already browsing bouquets", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToOrders() {
        if (!(this instanceof UserOrdersActivity)) {
            Intent intent = new Intent(this, UserOrdersActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "You're already viewing orders", Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(this, com.example.bouquetbliss.user.profile.UserProfileActivity.class);
        startActivity(intent);
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
