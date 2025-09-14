package com.example.bouquetbliss.admin.stock;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bouquetbliss.R;
import com.example.bouquetbliss.databinding.ActivityAddEditBouquetBinding;
import com.example.bouquetbliss.models.Bouquet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AddEditBouquetActivity extends AppCompatActivity {

    private ActivityAddEditBouquetBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isEditMode = false;
    private String bouquetId = null;
    private Bouquet currentBouquet = null;
    private Uri selectedImageUri = null;
    private String currentImageUrl = null;
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    // Activity result launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditBouquetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Check if we're in edit mode
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);
        bouquetId = getIntent().getStringExtra("bouquet_id");

        setupActivityResultLaunchers();
        setupViews();
        setupSpinners();
        setupClickListeners();

        if (isEditMode && bouquetId != null) {
            loadBouquetData();
        }
    }

    private void setupActivityResultLaunchers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getExtras() != null) {
                            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                            if (imageBitmap != null) {
                                displaySelectedImage(imageBitmap);
                            }
                        }
                    }
                }
            }
        );

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            selectedImageUri = data.getData();
                            binding.ivBouquetImagePreview.setImageURI(selectedImageUri);
                            showImagePreview(true);
                        }
                    }
                }
            }
        );
    }

    private void setupViews() {
        binding.tvTitle.setText(isEditMode ? "Edit Bouquet" : "Add New Bouquet");
        binding.btnSave.setText(isEditMode ? "Update Bouquet" : "Add Bouquet");
        
        // Initially show upload layout
        showImagePreview(false);
    }

    private void setupSpinners() {
        // Category Spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                Bouquet.Categories.getAllCategories());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        // Size Spinner
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                Bouquet.Sizes.getAllSizes());
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSize.setAdapter(sizeAdapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                if (isEditMode) {
                    updateBouquet();
                } else {
                    addBouquet();
                }
            }
        });

        // Image upload click listeners
        binding.layoutImageUpload.setOnClickListener(v -> showImagePickerDialog());
        binding.btnChangeImage.setOnClickListener(v -> showImagePickerDialog());
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validate name
        if (TextUtils.isEmpty(binding.etBouquetName.getText())) {
            binding.etBouquetName.setError("Bouquet name is required");
            isValid = false;
        }

        // Validate description
        if (TextUtils.isEmpty(binding.etBouquetDescription.getText())) {
            binding.etBouquetDescription.setError("Description is required");
            isValid = false;
        }

        // Validate price
        if (TextUtils.isEmpty(binding.etBouquetPrice.getText())) {
            binding.etBouquetPrice.setError("Price is required");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(binding.etBouquetPrice.getText().toString());
                if (price <= 0) {
                    binding.etBouquetPrice.setError("Price must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                binding.etBouquetPrice.setError("Invalid price format");
                isValid = false;
            }
        }

        // Validate quantity
        if (TextUtils.isEmpty(binding.etBouquetQuantity.getText())) {
            binding.etBouquetQuantity.setError("Quantity is required");
            isValid = false;
        } else {
            try {
                int quantity = Integer.parseInt(binding.etBouquetQuantity.getText().toString());
                if (quantity < 0) {
                    binding.etBouquetQuantity.setError("Quantity cannot be negative");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                binding.etBouquetQuantity.setError("Invalid quantity format");
                isValid = false;
            }
        }

        return isValid;
    }

    private void loadBouquetData() {
        showLoading(true);
        
        db.collection("bouquets").document(bouquetId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    if (documentSnapshot.exists()) {
                        currentBouquet = documentSnapshot.toObject(Bouquet.class);
                        if (currentBouquet != null) {
                            populateFields(currentBouquet);
                        }
                    } else {
                        Toast.makeText(this, "Bouquet not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to load bouquet: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateFields(Bouquet bouquet) {
        binding.etBouquetName.setText(bouquet.getName());
        binding.etBouquetDescription.setText(bouquet.getDescription());
        binding.etBouquetPrice.setText(String.valueOf(bouquet.getPrice()));
        binding.etBouquetQuantity.setText(String.valueOf(bouquet.getQuantity()));
        binding.etBouquetOccasion.setText(bouquet.getOccasion());

        // Set flower types
        if (bouquet.getFlowerTypes() != null && !bouquet.getFlowerTypes().isEmpty()) {
            binding.etFlowerTypes.setText(String.join(", ", bouquet.getFlowerTypes()));
        }

        // Set category spinner
        String[] categories = Bouquet.Categories.getAllCategories();
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(bouquet.getCategory())) {
                binding.spinnerCategory.setSelection(i);
                break;
            }
        }

        // Set size spinner
        String[] sizes = Bouquet.Sizes.getAllSizes();
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i].equals(bouquet.getSize())) {
                binding.spinnerSize.setSelection(i);
                break;
            }
        }

        // Load existing image if available
        if (bouquet.getImageUrls() != null && !bouquet.getImageUrls().isEmpty()) {
            currentImageUrl = bouquet.getImageUrls().get(0);
            // TODO: Load image using Glide or similar image loading library
            // For now, show that an image exists
            showImagePreview(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, retry the action
                if (permissions[0].equals(Manifest.permission.CAMERA)) {
                    openCamera();
                } else if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                          permissions[0].equals(Manifest.permission.READ_MEDIA_IMAGES)) {
                    openGallery();
                }
            } else {
                String permissionType = permissions[0].equals(Manifest.permission.CAMERA) ? "camera" : "gallery";
                Toast.makeText(this, "Permission denied. Cannot access " + permissionType + 
                    ". Please grant permission in Settings.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addBouquet() {
        showLoading(true);
        Bouquet bouquet = createBouquetFromInput();
        uploadImageAndSaveBouquet(bouquet, false);
    }

    private void updateBouquet() {
        if (bouquetId == null) return;
        
        showLoading(true);

        Bouquet bouquet = createBouquetFromInput();
        // Keep the original creation data
        if (currentBouquet != null) {
            bouquet.setId(bouquetId);
            bouquet.setCreatedAt(currentBouquet.getCreatedAt());
            bouquet.setCreatedBy(currentBouquet.getCreatedBy());
        }
        bouquet.setUpdatedAt(System.currentTimeMillis());

        uploadImageAndSaveBouquet(bouquet, true);
    }

    private Bouquet createBouquetFromInput() {
        String name = binding.etBouquetName.getText().toString().trim();
        String description = binding.etBouquetDescription.getText().toString().trim();
        double price = Double.parseDouble(binding.etBouquetPrice.getText().toString());
        int quantity = Integer.parseInt(binding.etBouquetQuantity.getText().toString());
        String category = binding.spinnerCategory.getSelectedItem().toString();
        String size = binding.spinnerSize.getSelectedItem().toString();
        String occasion = binding.etBouquetOccasion.getText().toString().trim();

        // Parse flower types
        List<String> flowerTypes = new ArrayList<>();
        String flowerTypesText = binding.etFlowerTypes.getText().toString().trim();
        if (!TextUtils.isEmpty(flowerTypesText)) {
            String[] types = flowerTypesText.split(",");
            for (String type : types) {
                flowerTypes.add(type.trim());
            }
        }

        // Create bouquet
        Bouquet bouquet = new Bouquet(name, description, price, quantity, 
                                    category, new ArrayList<>(), size, occasion);
        bouquet.setFlowerTypes(flowerTypes);
        
        // Set creator
        if (auth.getCurrentUser() != null) {
            bouquet.setCreatedBy(auth.getCurrentUser().getUid());
        }

        return bouquet;
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        
        new AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Camera
                            if (checkCameraPermission()) {
                                openCamera();
                            } else {
                                requestCameraPermission();
                            }
                            break;
                        case 1: // Gallery
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Android 13+ doesn't need storage permission for ACTION_GET_CONTENT
                                openGallery();
                            } else if (checkStoragePermission()) {
                                openGallery();
                            } else {
                                requestStoragePermission();
                            }
                            break;
                    }
                })
                .show();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12 and below use READ_EXTERNAL_STORAGE
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ request READ_MEDIA_IMAGES
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
        } else {
            // Android 12 and below request READ_EXTERNAL_STORAGE
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+, use ACTION_GET_CONTENT which works better with scoped storage
            galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            // For older versions, use ACTION_PICK
            galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("image/*");
        }
        
        try {
            galleryLauncher.launch(galleryIntent);
        } catch (Exception e) {
            // Fallback to ACTION_GET_CONTENT if ACTION_PICK fails
            Intent fallbackIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fallbackIntent.setType("image/*");
            fallbackIntent.addCategory(Intent.CATEGORY_OPENABLE);
            galleryLauncher.launch(fallbackIntent);
        }
    }

    private void displaySelectedImage(Bitmap bitmap) {
        binding.ivBouquetImagePreview.setImageBitmap(bitmap);
        
        // Convert bitmap to URI for upload - save to cache directory
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
            
            // Create a temporary file in cache directory
            File cacheDir = getCacheDir();
            File imageFile = new File(cacheDir, "temp_bouquet_" + System.currentTimeMillis() + ".jpg");
            
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(bytes.toByteArray());
            fos.close();
            
            selectedImageUri = Uri.fromFile(imageFile);
        } catch (Exception e) {
            Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        
        showImagePreview(true);
    }

    private void showImagePreview(boolean hasImage) {
        if (hasImage) {
            binding.layoutImageUpload.setVisibility(View.GONE);
            binding.ivBouquetImagePreview.setVisibility(View.VISIBLE);
            binding.btnChangeImage.setVisibility(View.VISIBLE);
        } else {
            binding.layoutImageUpload.setVisibility(View.VISIBLE);
            binding.ivBouquetImagePreview.setVisibility(View.GONE);
            binding.btnChangeImage.setVisibility(View.GONE);
        }
    }

    private void uploadImageAndSaveBouquet(Bouquet bouquet, boolean isUpdate) {
        if (selectedImageUri == null) {
            // No image to upload, save bouquet directly
            if (currentImageUrl != null) {
                List<String> imageUrls = new ArrayList<>();
                imageUrls.add(currentImageUrl);
                bouquet.setImageUrls(imageUrls);
            }
            saveBouquetToFirestore(bouquet, isUpdate);
            return;
        }

        // Save image locally (FREE alternative to Firebase Storage)
        saveImageLocallyAndContinue(selectedImageUri, bouquet, isUpdate);
    }

    private void saveImageLocallyAndContinue(Uri imageUri, Bouquet bouquet, boolean isUpdate) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                showLoading(false);
                Toast.makeText(this, "Cannot read image file", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create unique filename
            String imageFileName = "bouquet_" + UUID.randomUUID().toString() + ".jpg";
            
            // Save to app's internal storage (private directory)
            File imagesDir = new File(getFilesDir(), "bouquet_images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }
            
            File imageFile = new File(imagesDir, imageFileName);
            
            // Copy image data to local file
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            
            // Create local file path (we'll use this instead of Firebase URL)
            String localImagePath = "local://bouquet_images/" + imageFileName;
            
            Toast.makeText(this, "✅ Image saved locally!", Toast.LENGTH_SHORT).show();
            
            // Add local path to bouquet
            List<String> imageUrls = new ArrayList<>();
            imageUrls.add(localImagePath);
            bouquet.setImageUrls(imageUrls);
            
            // Save bouquet to Firestore
            saveBouquetToFirestore(bouquet, isUpdate);

        } catch (Exception e) {
            showLoading(false);
            Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void saveBouquetToFirestore(Bouquet bouquet, boolean isUpdate) {
        if (isUpdate && bouquetId != null) {
            db.collection("bouquets").document(bouquetId)
                    .set(bouquet)
                    .addOnSuccessListener(aVoid -> {
                        showLoading(false);
                        Toast.makeText(this, "Bouquet updated successfully!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        Toast.makeText(this, "Failed to update bouquet: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("bouquets")
                    .add(bouquet)
                    .addOnSuccessListener(documentReference -> {
                        showLoading(false);
                        Toast.makeText(this, "Bouquet added successfully!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        Toast.makeText(this, "Failed to add bouquet: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!show);
        
        // Disable all input fields during loading
        binding.etBouquetName.setEnabled(!show);
        binding.etBouquetDescription.setEnabled(!show);
        binding.etBouquetPrice.setEnabled(!show);
        binding.etBouquetQuantity.setEnabled(!show);
        binding.etBouquetOccasion.setEnabled(!show);
        binding.etFlowerTypes.setEnabled(!show);
        binding.spinnerCategory.setEnabled(!show);
        binding.spinnerSize.setEnabled(!show);
        binding.layoutImageUpload.setEnabled(!show);
        binding.btnChangeImage.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
