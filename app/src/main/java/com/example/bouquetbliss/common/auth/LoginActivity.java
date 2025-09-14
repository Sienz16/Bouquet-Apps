package com.example.bouquetbliss.common.auth;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.admin.dashboard.AdminDashboardActivity;
import com.example.bouquetbliss.databinding.ActivityAuthLoginBinding;
import com.example.bouquetbliss.models.User;
import com.example.bouquetbliss.user.dashboard.UserDashboardActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    
    private static final String TAG = "LoginActivity";
    
    private ActivityAuthLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserRoleAndNavigate(currentUser.getUid());
        }
        
        setupClickListeners();
        setupAnimations();
    }
    
    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> performLogin());
        
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.tvForgotPassword.setOnClickListener(v -> {
            // TODO: Implement forgot password functionality
            Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }
    
    private void setupAnimations() {
        // Fade in animation for the email field
        binding.tilEmail.setAlpha(0f);
        binding.tilEmail.setTranslationY(30f);
        
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.tilEmail, "alpha", 0f, 1f);
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(binding.tilEmail, "translationY", 30f, 0f);
        
        fadeIn.setDuration(600);
        slideUp.setDuration(600);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        slideUp.setInterpolator(new DecelerateInterpolator());
        
        fadeIn.setStartDelay(200);
        slideUp.setStartDelay(200);
        
        fadeIn.start();
        slideUp.start();
        
        // Simple fade animation for the form
        binding.tilPassword.setAlpha(0f);
        binding.tilPassword.setTranslationY(20f);
        
        ObjectAnimator passwordFade = ObjectAnimator.ofFloat(binding.tilPassword, "alpha", 0f, 1f);
        ObjectAnimator passwordSlide = ObjectAnimator.ofFloat(binding.tilPassword, "translationY", 20f, 0f);
        
        passwordFade.setDuration(600);
        passwordSlide.setDuration(600);
        passwordFade.setStartDelay(400);
        passwordSlide.setStartDelay(400);
        
        passwordFade.start();
        passwordSlide.start();
    }
    
    private void performLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        
        // Reset error states
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        
        // Validate input
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email diperlukan");
            binding.etEmail.requestFocus();
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Format email tidak sah");
            binding.etEmail.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Kata laluan diperlukan");
            binding.etPassword.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            binding.tilPassword.setError("Kata laluan mestilah sekurang-kurangnya 6 aksara");
            binding.etPassword.requestFocus();
            return;
        }
        
        // Show loading
        showLoading(true);
        
        // Perform Firebase authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLoading(false);
                        
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkUserRoleAndNavigate(user.getUid());
                            }
                        } else {
                            // Sign in failed
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String errorMessage = "Log masuk gagal. Sila semak email dan kata laluan anda.";
                            
                            if (task.getException() != null) {
                                String exceptionMessage = task.getException().getMessage();
                                if (exceptionMessage != null) {
                                    if (exceptionMessage.contains("user-not-found")) {
                                        errorMessage = "Akaun tidak dijumpai. Sila daftar terlebih dahulu.";
                                    } else if (exceptionMessage.contains("wrong-password")) {
                                        errorMessage = "Kata laluan salah.";
                                    } else if (exceptionMessage.contains("user-disabled")) {
                                        errorMessage = "Akaun telah dinonaktifkan.";
                                    } else if (exceptionMessage.contains("too-many-requests")) {
                                        errorMessage = "Terlalu banyak percubaan. Sila cuba lagi kemudian.";
                                    }
                                }
                            }
                            
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    private void checkUserRoleAndNavigate(String uid) {
        showLoading(true);
        
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        showLoading(false);
                        
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                // Check if we need to update user role based on email
                                User.UserType correctRole = determineUserRoleFromEmail(user.getEmail());
                                if (correctRole != user.getUserType()) {
                                    // Update user role in Firestore
                                    updateUserRole(uid, correctRole);
                                    navigateToDashboard(correctRole);
                                } else {
                                    navigateToDashboard(user.getUserType());
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Error membaca data pengguna", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // User document doesn't exist, create default user profile
                            createDefaultUserProfile(uid);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showLoading(false);
                        Log.w(TAG, "Error getting user document", e);
                        Toast.makeText(LoginActivity.this, "Error membaca data pengguna: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private User.UserType determineUserRoleFromEmail(String email) {
        if (email != null) {
            if (email.equals("admin@test.com") || email.contains("admin")) {
                return User.UserType.ADMIN;
            }
        }
        return User.UserType.USER;
    }
    
    private void updateUserRole(String uid, User.UserType newRole) {
        db.collection("users").document(uid)
                .update("userType", newRole, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User role updated to: " + newRole);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating user role", e);
                });
    }
    
    private void createDefaultUserProfile(String uid) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;
        
        final User.UserType userType = determineUserRoleFromEmail(firebaseUser.getEmail());
        User user = new User(uid, firebaseUser.getEmail(), firebaseUser.getDisplayName(), userType);
        
        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User profile created successfully");
                        navigateToDashboard(userType);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error creating user profile", e);
                        Toast.makeText(LoginActivity.this, "Error mencipta profil pengguna: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void navigateToDashboard(User.UserType userType) {
        Intent intent;
        
        switch (userType) {
            case ADMIN:
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            case USER:
            default:
                intent = new Intent(this, UserDashboardActivity.class);
                break;
        }
        
        // Clear the back stack so user can't navigate back to login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showLoading(boolean show) {
        if (show) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnLogin.setEnabled(false);
            binding.btnLogin.setText("Signing in...");
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);
            binding.btnLogin.setText("Continue");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
