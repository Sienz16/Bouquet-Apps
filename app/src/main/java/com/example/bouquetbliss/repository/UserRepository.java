package com.example.bouquetbliss.repository;

import android.util.Log;

import com.example.bouquetbliss.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Repository pattern for centralized user data management
 * Provides clean separation of concerns and better testability
 */
public class UserRepository {
    
    private static final String TAG = "UserRepository";
    private static final String USERS_COLLECTION = "users";
    
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    
    // Singleton pattern
    private static UserRepository instance;
    
    private UserRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }
    
    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }
    
    /**
     * Get current authenticated user data from Firestore
     */
    public Task<User> getCurrentUserData() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            throw new IllegalStateException("No authenticated user");
        }
        
        return db.collection(USERS_COLLECTION)
                .document(firebaseUser.getUid())
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            return document.toObject(User.class);
                        } else {
                            // Create user profile if doesn't exist
                            return createUserProfile(firebaseUser);
                        }
                    } else {
                        throw task.getException();
                    }
                });
    }
    
    /**
     * Create new user profile in Firestore
     */
    private User createUserProfile(FirebaseUser firebaseUser) {
        User user = new User(
            firebaseUser.getUid(),
            firebaseUser.getEmail(),
            firebaseUser.getDisplayName(),
            User.UserType.USER // Default role
        );
        
        // Save to Firestore (fire and forget)
        db.collection(USERS_COLLECTION)
                .document(firebaseUser.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> 
                    Log.d(TAG, "User profile created: " + firebaseUser.getEmail()))
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Failed to create user profile", e));
        
        return user;
    }
    
    /**
     * Update user role (Admin only functionality)
     */
    public Task<Void> updateUserRole(String uid, User.UserType newRole) {
        return db.collection(USERS_COLLECTION)
                .document(uid)
                .update(
                    "userType", newRole,
                    "updatedAt", System.currentTimeMillis()
                );
    }
    
    /**
     * Check if current user is authenticated
     */
    public boolean isUserAuthenticated() {
        return auth.getCurrentUser() != null;
    }
    
    /**
     * Get current Firebase user
     */
    public FirebaseUser getCurrentFirebaseUser() {
        return auth.getCurrentUser();
    }
    
    /**
     * Sign out current user
     */
    public void signOut() {
        auth.signOut();
    }
}
