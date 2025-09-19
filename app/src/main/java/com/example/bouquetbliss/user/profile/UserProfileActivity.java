package com.example.bouquetbliss.user.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.user.base.BaseUserNavigationActivity;
import com.example.bouquetbliss.databinding.ActivityUserProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends BaseUserNavigationActivity {

    private ActivityUserProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        setupImagePicker();
        setupClickListeners();
        setupUserBottomNavigation();
        setSelectedNavItem("profile");
        loadUserProfile();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        binding.ivProfileImage.setImageURI(imageUri);
                        uploadProfileImage(imageUri);
                    }
                }
            }
        );
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnChangePhoto.setOnClickListener(v -> showImagePickerDialog());

        binding.btnEditPhone.setOnClickListener(v -> showEditPhoneDialog());

        binding.btnEditName.setOnClickListener(v -> showEditNameDialog());
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            // Load basic Firebase Auth info
            binding.tvEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "Not available");
            binding.tvUserName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
            binding.tvFullName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");

            // Load additional info from Firestore
            loadUserFromFirestore();
        }
    }

    private void loadUserFromFirestore() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        String fullName = documentSnapshot.getString("fullName");
                        
                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            binding.tvPhoneNumber.setText(phoneNumber);
                        } else {
                            binding.tvPhoneNumber.setText("Not set");
                        }
                        
                        if (fullName != null && !fullName.isEmpty()) {
                            binding.tvFullName.setText(fullName);
                            binding.tvUserName.setText(fullName);
                        }
                    } else {
                        // Create user document if it doesn't exist
                        createUserDocument();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void createUserDocument() {
        if (currentUser != null) {
            Map<String, Object> user = new HashMap<>();
            user.put("email", currentUser.getEmail());
            user.put("fullName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
            user.put("role", "user");
            user.put("createdAt", System.currentTimeMillis());

            db.collection("users").document(currentUser.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile created successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create profile: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Gallery", "Camera"};
        
        new AlertDialog.Builder(this)
            .setTitle("Change Profile Picture")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Gallery
                        openGallery();
                        break;
                    case 1: // Camera
                        Toast.makeText(this, "Camera functionality coming soon!", Toast.LENGTH_SHORT).show();
                        break;
                }
            })
            .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        // For now, just update the ImageView
        // In a full implementation, you would upload to Firebase Storage
        Toast.makeText(this, "Profile image updated locally. Cloud upload coming soon!", 
            Toast.LENGTH_SHORT).show();
    }

    private void showEditPhoneDialog() {
        EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_PHONE);
        editText.setText(binding.tvPhoneNumber.getText().toString().equals("Not set") ? 
            "" : binding.tvPhoneNumber.getText().toString());
        editText.setHint("Enter phone number (e.g., +60 12-345 6789)");

        new AlertDialog.Builder(this)
            .setTitle("Edit Phone Number")
            .setView(editText)
            .setPositiveButton("Save", (dialog, which) -> {
                String phoneNumber = editText.getText().toString().trim();
                if (!phoneNumber.isEmpty()) {
                    updatePhoneNumber(phoneNumber);
                } else {
                    Toast.makeText(this, "Phone number cannot be empty", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showEditNameDialog() {
        EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editText.setText(binding.tvFullName.getText().toString());
        editText.setHint("Enter full name");

        new AlertDialog.Builder(this)
            .setTitle("Edit Full Name")
            .setView(editText)
            .setPositiveButton("Save", (dialog, which) -> {
                String fullName = editText.getText().toString().trim();
                if (!fullName.isEmpty()) {
                    updateFullName(fullName);
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void updatePhoneNumber(String phoneNumber) {
        if (currentUser != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("phoneNumber", phoneNumber);
            updates.put("updatedAt", System.currentTimeMillis());

            db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    binding.tvPhoneNumber.setText(phoneNumber);
                    Toast.makeText(this, "Phone number updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update phone number: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void updateFullName(String fullName) {
        if (currentUser != null) {
            // Update Firebase Auth profile
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

            currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update Firestore
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("fullName", fullName);
                        updates.put("updatedAt", System.currentTimeMillis());

                        db.collection("users").document(currentUser.getUid())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                binding.tvFullName.setText(fullName);
                                binding.tvUserName.setText(fullName);
                                Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update name in database: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                    } else {
                        Toast.makeText(this, "Failed to update name: " + task.getException().getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
