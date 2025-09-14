package com.example.bouquetbliss.utils;

import android.content.Context;
import android.content.Intent;

import com.example.bouquetbliss.admin.dashboard.AdminDashboardActivity;
import com.example.bouquetbliss.user.dashboard.UserDashboardActivity;
import com.example.bouquetbliss.models.User;

/**
 * Utility class for navigation logic
 * Centralizes dashboard routing based on user roles
 */
public class NavigationHelper {
    
    /**
     * Navigate to appropriate dashboard based on user role
     */
    public static void navigateToDashboard(Context context, User.UserType userType) {
        Intent intent = getDashboardIntent(context, userType);
        
        // Clear the back stack so user can't navigate back to login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        context.startActivity(intent);
    }
    
    /**
     * Get dashboard intent based on user role
     */
    private static Intent getDashboardIntent(Context context, User.UserType userType) {
        switch (userType) {
            case ADMIN:
                return new Intent(context, AdminDashboardActivity.class);
            case USER:
            default:
                return new Intent(context, UserDashboardActivity.class);
        }
    }
    
    /**
     * Get dashboard class based on user role (for checks)
     */
    public static Class<?> getDashboardClass(User.UserType userType) {
        switch (userType) {
            case ADMIN:
                return AdminDashboardActivity.class;
            case USER:
            default:
                return UserDashboardActivity.class;
        }
    }
}
