package com.example.bouquetbliss.user.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
        
        // Apply translations after UI is set up
        applyTranslations();
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

        binding.btnEditAddress.setOnClickListener(v -> showEditAddressDialog());
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
                        
                        // Address fields
                        String streetAddress = documentSnapshot.getString("streetAddress");
                        String city = documentSnapshot.getString("city");
                        String state = documentSnapshot.getString("state");
                        String postalCode = documentSnapshot.getString("postalCode");
                        
                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            binding.tvPhoneNumber.setText(phoneNumber);
                        } else {
                            binding.tvPhoneNumber.setText("Not set");
                        }
                        
                        if (fullName != null && !fullName.isEmpty()) {
                            binding.tvFullName.setText(fullName);
                            binding.tvUserName.setText(fullName);
                        }
                        
                        // Update address display
                        updateAddressDisplay(streetAddress, city, state, postalCode);
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

    private void updateAddressDisplay(String streetAddress, String city, String state, String postalCode) {
        StringBuilder address = new StringBuilder();
        
        if (streetAddress != null && !streetAddress.trim().isEmpty()) {
            address.append(streetAddress);
        }
        
        if (city != null && !city.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(city);
        }
        
        if (postalCode != null && !postalCode.trim().isEmpty()) {
            if (address.length() > 0) address.append(" ");
            address.append(postalCode);
        }
        
        if (state != null && !state.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(state);
        }
        
        if (address.length() > 0) {
            binding.tvAddress.setText(address.toString());
        } else {
            binding.tvAddress.setText("No address set");
        }
    }

    private void showEditAddressDialog() {
        // Create custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_address, null);
        
        EditText etStreetAddress = dialogView.findViewById(R.id.etStreetAddress);
        EditText etCity = dialogView.findViewById(R.id.etCity);
        EditText etPostalCode = dialogView.findViewById(R.id.etPostalCode);
        Spinner spinnerState = dialogView.findViewById(R.id.spinnerState);
        
        // Setup state spinner with only Penang and Kedah
        String[] states = {"Select State", "Penang", "Kedah"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, states);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(adapter);
        
        // Load current address data if exists
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String streetAddress = documentSnapshot.getString("streetAddress");
                        String city = documentSnapshot.getString("city");
                        String state = documentSnapshot.getString("state");
                        String postalCode = documentSnapshot.getString("postalCode");
                        
                        if (streetAddress != null) etStreetAddress.setText(streetAddress);
                        if (city != null) etCity.setText(city);
                        if (postalCode != null) etPostalCode.setText(postalCode);
                        
                        // Set spinner selection
                        if (state != null) {
                            for (int i = 0; i < states.length; i++) {
                                if (states[i].equals(state)) {
                                    spinnerState.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                });
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Edit Address")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String streetAddress = etStreetAddress.getText().toString().trim();
                String city = etCity.getText().toString().trim();
                String postalCode = etPostalCode.getText().toString().trim();
                String selectedState = spinnerState.getSelectedItem().toString();
                
                // Validation
                if (streetAddress.isEmpty()) {
                    Toast.makeText(this, "Street address is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (city.isEmpty()) {
                    Toast.makeText(this, "City is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (postalCode.isEmpty()) {
                    Toast.makeText(this, "Postal code is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (selectedState.equals("Select State")) {
                    Toast.makeText(this, "Please select a state", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                updateAddress(streetAddress, city, selectedState, postalCode);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void updateAddress(String streetAddress, String city, String state, String postalCode) {
        if (currentUser != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("streetAddress", streetAddress);
            updates.put("city", city);
            updates.put("state", state);
            updates.put("postalCode", postalCode);
            updates.put("updatedAt", System.currentTimeMillis());

            db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    updateAddressDisplay(streetAddress, city, state, postalCode);
                    Toast.makeText(this, "Address updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update address: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
