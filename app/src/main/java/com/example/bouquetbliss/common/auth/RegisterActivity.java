package com.example.bouquetbliss.common.auth;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.admin.dashboard.AdminDashboardActivity;
import com.example.bouquetbliss.databinding.ActivityAuthRegisterBinding;
import com.example.bouquetbliss.models.User;
import com.example.bouquetbliss.utils.UITranslator;
import com.example.bouquetbliss.user.dashboard.UserDashboardActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private ActivityAuthRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UITranslator uiTranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupClickListeners();
        setupAnimations();
        
        // Initialize translator and apply translations
        uiTranslator = new UITranslator(this);
        applyTranslations();
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> performRegistration());
        
        binding.tvLoginBottom.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        
    }

    private void setupAnimations() {
        // Fade in animation for the form
        binding.tilFullName.setAlpha(0f);
        binding.tilFullName.setTranslationY(30f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.tilFullName, "alpha", 0f, 1f);
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(binding.tilFullName, "translationY", 30f, 0f);

        fadeIn.setDuration(600);
        slideUp.setDuration(600);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        slideUp.setInterpolator(new DecelerateInterpolator());

        fadeIn.setStartDelay(200);
        slideUp.setStartDelay(200);

        fadeIn.start();
        slideUp.start();
    }

    private void performRegistration() {
        String fullName = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Reset error states
        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        // Validate input
        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.setError("Full name is required");
            binding.etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email is required");
            binding.etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Please enter a valid email address");
            binding.etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Password is required");
            binding.etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            binding.etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError("Please confirm your password");
            binding.etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            binding.etConfirmPassword.requestFocus();
            return;
        }

        // All new registrations are USER type
        User.UserType userType = User.UserType.USER;

        // Show loading
        showLoading(true);

        // Create Firebase user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration success
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                createUserProfile(firebaseUser.getUid(), fullName, email, userType);
                            }
                        } else {
                            // Registration failed
                            showLoading(false);
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            String errorMessage = "Registration failed. Please try again.";

                            if (task.getException() != null) {
                                String exceptionMessage = task.getException().getMessage();
                                if (exceptionMessage != null) {
                                    if (exceptionMessage.contains("already-in-use")) {
                                        errorMessage = "This email is already registered. Please use a different email.";
                                    } else if (exceptionMessage.contains("weak-password")) {
                                        errorMessage = "Password is too weak. Please choose a stronger password.";
                                    } else if (exceptionMessage.contains("invalid-email")) {
                                        errorMessage = "Please enter a valid email address.";
                                    }
                                }
                            }

                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void createUserProfile(String uid, String fullName, String email, User.UserType userType) {
        User user = new User(uid, email, fullName, userType);

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showLoading(false);
                        Log.d(TAG, "User profile created successfully");
                        Toast.makeText(RegisterActivity.this, "Account created successfully! Welcome to Bouquet Bliss!", Toast.LENGTH_SHORT).show();

                        // Navigate to appropriate dashboard
                        navigateToDashboard(userType);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showLoading(false);
                        Log.w(TAG, "Error creating user profile", e);
                        Toast.makeText(RegisterActivity.this, "Registration completed, but failed to create profile. Please try logging in.", Toast.LENGTH_LONG).show();
                        
                        // Navigate to login page
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
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

        // Clear the back stack so user can't navigate back to registration
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        if (show) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnRegister.setEnabled(false);
            binding.btnRegister.setText("Creating account...");
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setEnabled(true);
            binding.btnRegister.setText("Create Account");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
    
    private void applyTranslations() {
        if (uiTranslator != null) {
            // Apply translations to the current activity
            uiTranslator.translateActivity();
            
            // Preload common translations for better performance
            uiTranslator.translateCommonElements();
        }
    }
}
