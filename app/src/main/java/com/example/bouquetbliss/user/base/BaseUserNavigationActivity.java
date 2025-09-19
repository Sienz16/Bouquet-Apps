package com.example.bouquetbliss.user.base;

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
import com.example.bouquetbliss.user.dashboard.UserDashboardActivity;
import com.example.bouquetbliss.user.browse.BrowseBouquetsActivity;
import com.example.bouquetbliss.user.orders.UserOrdersActivity;
import com.example.bouquetbliss.user.favorites.UserFavoritesActivity;
import com.example.bouquetbliss.common.auth.LoginActivity;
import com.example.bouquetbliss.utils.TranslationManager;
import com.example.bouquetbliss.utils.UITranslator;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseUserNavigationActivity extends AppCompatActivity {

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
        String[] originalOptions = {"My Favorites", "View Profile", "Language", "Logout"};
        String originalTitle = "Profile Options";
        
        TranslationManager translationManager = TranslationManager.getInstance(this);
        
        if (translationManager.getCurrentLanguage().equals(TranslateLanguage.ENGLISH)) {
            // Show in English
            new AlertDialog.Builder(this)
                    .setTitle(originalTitle)
                    .setItems(originalOptions, (dialog, which) -> {
                        switch (which) {
                            case 0: // My Favorites
                                navigateToFavorites();
                                break;
                            case 1: // View Profile
                                navigateToProfile();
                                break;
                            case 2: // Language
                                showLanguageSelectionDialog();
                                break;
                            case 3: // Logout
                                showLogoutConfirmation();
                                break;
                        }
                    })
                    .show();
        } else {
            // Translate and show in Malay
            String[] translatedOptions = new String[originalOptions.length];
            final int[] translatedCount = {0};
            
            // Translate title
            translationManager.translateText(originalTitle, new TranslationManager.TranslationCallback() {
                @Override
                public void onTranslated(String translatedTitle) {
                    // Translate each option
                    for (int i = 0; i < originalOptions.length; i++) {
                        final int index = i;
                        translationManager.translateText(originalOptions[i], new TranslationManager.TranslationCallback() {
                            @Override
                            public void onTranslated(String translatedOption) {
                                translatedOptions[index] = translatedOption;
                                translatedCount[0]++;
                                
                                // Show dialog when all translations are complete
                                if (translatedCount[0] == originalOptions.length) {
                                    runOnUiThread(() -> {
                                        new AlertDialog.Builder(BaseUserNavigationActivity.this)
                                                .setTitle(translatedTitle)
                                                .setItems(translatedOptions, (dialog, which) -> {
                                                    switch (which) {
                                                        case 0: // My Favorites
                                                            navigateToFavorites();
                                                            break;
                                                        case 1: // View Profile
                                                            navigateToProfile();
                                                            break;
                                                        case 2: // Language
                                                            showLanguageSelectionDialog();
                                                            break;
                                                        case 3: // Logout
                                                            showLogoutConfirmation();
                                                            break;
                                                    }
                                                })
                                                .show();
                                    });
                                }
                            }

                            @Override
                            public void onError(String error) {
                                translatedOptions[index] = originalOptions[index]; // Fallback to original
                                translatedCount[0]++;
                                
                                if (translatedCount[0] == originalOptions.length) {
                                    runOnUiThread(() -> {
                                        new AlertDialog.Builder(BaseUserNavigationActivity.this)
                                                .setTitle(translatedTitle)
                                                .setItems(translatedOptions, (dialog, which) -> {
                                                    switch (which) {
                                                        case 0: // My Favorites
                                                            navigateToFavorites();
                                                            break;
                                                        case 1: // View Profile
                                                            navigateToProfile();
                                                            break;
                                                        case 2: // Language
                                                            showLanguageSelectionDialog();
                                                            break;
                                                        case 3: // Logout
                                                            showLogoutConfirmation();
                                                            break;
                                                    }
                                                })
                                                .show();
                                    });
                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    // Fallback to English if title translation fails
                    new AlertDialog.Builder(BaseUserNavigationActivity.this)
                            .setTitle(originalTitle)
                            .setItems(originalOptions, (dialog, which) -> {
                                switch (which) {
                                    case 0: // My Favorites
                                        navigateToFavorites();
                                        break;
                                    case 1: // View Profile
                                        navigateToProfile();
                                        break;
                                    case 2: // Language
                                        showLanguageSelectionDialog();
                                        break;
                                    case 3: // Logout
                                        showLogoutConfirmation();
                                        break;
                                }
                            })
                            .show();
                }
            });
        }
    }

    private void navigateToFavorites() {
        if (!(this instanceof UserFavoritesActivity)) {
            Intent intent = new Intent(this, UserFavoritesActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "You're already viewing favorites", Toast.LENGTH_SHORT).show();
        }
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

    private void showLanguageSelectionDialog() {
        TranslationManager translationManager = TranslationManager.getInstance(this);
        
        String[] languages = translationManager.getAvailableLanguages();
        String[] languageCodes = translationManager.getAvailableLanguageCodes();
        
        // Find current selection
        final int currentSelection;
        String currentLanguage = translationManager.getCurrentLanguage();
        int tempSelection = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLanguage)) {
                tempSelection = i;
                break;
            }
        }
        currentSelection = tempSelection;
        
        String dialogTitle = "Select Language";
        String cancelButton = "Cancel";
        String changeMessage = "Language changed to ";
        
        if (translationManager.getCurrentLanguage().equals(TranslateLanguage.ENGLISH)) {
            // Show in English
            new AlertDialog.Builder(this)
                    .setTitle(dialogTitle)
                    .setSingleChoiceItems(languages, currentSelection, (dialog, which) -> {
                        String selectedLanguageCode = languageCodes[which];
                        translationManager.setLanguage(selectedLanguageCode);
                        
                        Toast.makeText(this, changeMessage + languages[which], Toast.LENGTH_SHORT).show();
                        
                        // Restart activity to apply language changes
                        recreate();
                        
                        dialog.dismiss();
                    })
                    .setNegativeButton(cancelButton, null)
                    .show();
        } else {
            // Translate and show dialog elements
            translationManager.translateText(dialogTitle, new TranslationManager.TranslationCallback() {
                @Override
                public void onTranslated(String translatedTitle) {
                    translationManager.translateText(cancelButton, new TranslationManager.TranslationCallback() {
                        @Override
                        public void onTranslated(String translatedCancel) {
                            runOnUiThread(() -> {
                                new AlertDialog.Builder(BaseUserNavigationActivity.this)
                                        .setTitle(translatedTitle)
                                        .setSingleChoiceItems(languages, currentSelection, (dialog, which) -> {
                                            String selectedLanguageCode = languageCodes[which];
                                            translationManager.setLanguage(selectedLanguageCode);
                                            
                                            Toast.makeText(BaseUserNavigationActivity.this, changeMessage + languages[which], Toast.LENGTH_SHORT).show();
                                            
                                            // Restart activity to apply language changes
                                            recreate();
                                            
                                            dialog.dismiss();
                                        })
                                        .setNegativeButton(translatedCancel, null)
                                        .show();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            // Fallback to English
                            runOnUiThread(() -> {
                                new AlertDialog.Builder(BaseUserNavigationActivity.this)
                                        .setTitle(dialogTitle)
                                        .setSingleChoiceItems(languages, currentSelection, (dialog, which) -> {
                                            String selectedLanguageCode = languageCodes[which];
                                            translationManager.setLanguage(selectedLanguageCode);
                                            
                                            Toast.makeText(BaseUserNavigationActivity.this, changeMessage + languages[which], Toast.LENGTH_SHORT).show();
                                            
                                            recreate();
                                            
                                            dialog.dismiss();
                                        })
                                        .setNegativeButton(cancelButton, null)
                                        .show();
                            });
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    // Fallback to English
                    runOnUiThread(() -> {
                        new AlertDialog.Builder(BaseUserNavigationActivity.this)
                                .setTitle(dialogTitle)
                                .setSingleChoiceItems(languages, currentSelection, (dialog, which) -> {
                                    String selectedLanguageCode = languageCodes[which];
                                    translationManager.setLanguage(selectedLanguageCode);
                                    
                                    Toast.makeText(BaseUserNavigationActivity.this, changeMessage + languages[which], Toast.LENGTH_SHORT).show();
                                    
                                    recreate();
                                    
                                    dialog.dismiss();
                                })
                                .setNegativeButton(cancelButton, null)
                                .show();
                    });
                }
            });
        }
    }
}
