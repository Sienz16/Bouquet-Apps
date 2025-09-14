package com.example.bouquetbliss.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.bouquetbliss.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Helper class for Firebase operations
 * This class provides utility methods for common Firebase operations
 */
public class FirebaseHelper {
    
    private static final String TAG = "FirebaseHelper";
    private static final String USERS_COLLECTION = "users";
    
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    
    public FirebaseHelper() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Interface for user data callbacks
     */
    public interface UserDataCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }
    
    /**
     * Get current logged in user data from Firestore
     */
    public void getCurrentUserData(UserDataCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Pengguna tidak log masuk");
            return;
        }
        
        getUserData(currentUser.getUid(), callback);
    }
    
    /**
     * Get user data by UID from Firestore
     */
    public void getUserData(String uid, UserDataCallback callback) {
        db.collection(USERS_COLLECTION).document(uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                callback.onSuccess(user);
                            } else {
                                callback.onFailure("Error membaca data pengguna");
                            }
                        } else {
                            callback.onFailure("Data pengguna tidak dijumpai");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error getting user document", e);
                        callback.onFailure("Error: " + e.getMessage());
                    }
                });
    }
    
    /**
     * Save or update user data in Firestore
     */
    public void saveUserData(User user, OnCompleteListener<Void> callback) {
        if (user.getUid() == null || user.getUid().isEmpty()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                user.setUid(currentUser.getUid());
            }
        }
        
        db.collection(USERS_COLLECTION).document(user.getUid())
                .set(user)
                .addOnCompleteListener(callback);
    }
    
    /**
     * Update user type for a specific user (Admin only)
     */
    public void updateUserType(String uid, User.UserType newUserType, OnCompleteListener<Void> callback) {
        db.collection(USERS_COLLECTION).document(uid)
                .update("userType", newUserType, "updatedAt", System.currentTimeMillis())
                .addOnCompleteListener(callback);
    }
    
    /**
     * Check if current user is authenticated
     */
    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }
    
    /**
     * Get current Firebase user
     */
    public FirebaseUser getCurrentFirebaseUser() {
        return mAuth.getCurrentUser();
    }
    
    /**
     * Sign out current user
     */
    public void signOut() {
        mAuth.signOut();
    }
}
