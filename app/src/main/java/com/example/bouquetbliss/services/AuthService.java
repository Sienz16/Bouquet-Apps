package com.example.bouquetbliss.services;

import com.example.bouquetbliss.models.User;
import com.example.bouquetbliss.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Service layer for authentication operations
 * Provides clean separation between UI and business logic
 */
public class AuthService {
    
    private final FirebaseAuth auth;
    private final UserRepository userRepository;
    
    // Singleton pattern
    private static AuthService instance;
    
    private AuthService() {
        this.auth = FirebaseAuth.getInstance();
        this.userRepository = UserRepository.getInstance();
    }
    
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    /**
     * Login user with email and password
     */
    public Task<AuthResult> loginWithEmailAndPassword(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }
    
    /**
     * Register new user with email and password
     */
    public Task<AuthResult> registerWithEmailAndPassword(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }
    
    /**
     * Get user data after successful authentication
     */
    public Task<User> getUserDataAfterAuth() {
        return userRepository.getCurrentUserData();
    }
    
    /**
     * Sign out current user
     */
    public void signOut() {
        userRepository.signOut();
    }
    
    /**
     * Check if user is authenticated
     */
    public boolean isUserAuthenticated() {
        return userRepository.isUserAuthenticated();
    }
    
    /**
     * Send password reset email
     */
    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }
}
