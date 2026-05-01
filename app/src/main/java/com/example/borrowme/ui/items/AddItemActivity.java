package com.example.borrowme.ui.items;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.borrowme.R;
import com.example.borrowme.ui.auth.LoginActivity;
import com.example.borrowme.ui.profile.MyLendingsActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AddItemActivity extends AppCompatActivity {
    private static final String TAG = "AddItemActivity";

    private EditText etItemName, etItemDescription;
    private TextView tvCategory;
    private TextView tvDaysValue;
    private Slider sliderDays;
    private MaterialButton btnSubmit;
    private View imageContainer;
    private View emptyUploadState;
    private ImageView ivItemPreview;

    private Uri photoUri;
    private String existingItemId = null;
    private String existingImageUrl = null;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) showImageSourceDialog();
                else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<Uri> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) showPreview(photoUri);
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) showPreview(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_add_item);
            db = FirebaseFirestore.getInstance();
            // Explicitly using the bucket from google-services.json to avoid 404
            storageRef = FirebaseStorage.getInstance("gs://borrowme-c3ca8.firebasestorage.app").getReference();

            initViews();
            setupListeners();
            
            existingItemId = getIntent().getStringExtra("itemId");
            if (existingItemId != null) loadExistingItemData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }

    private void initViews() {
        etItemName = findViewById(R.id.etItemName);
        etItemDescription = findViewById(R.id.etItemDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvDaysValue = findViewById(R.id.tvDaysValue);
        sliderDays = findViewById(R.id.sliderDays);
        btnSubmit = findViewById(R.id.btnSubmit);
        imageContainer = findViewById(R.id.imageContainer);
        emptyUploadState = findViewById(R.id.emptyUploadState);
        ivItemPreview = findViewById(R.id.ivItemPreview);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnChooseFiles).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                showImageSourceDialog();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        findViewById(R.id.btnRemoveImage).setOnClickListener(v -> {
            photoUri = null;
            existingImageUrl = null;
            imageContainer.setVisibility(View.GONE);
            emptyUploadState.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.btnSelectCategory).setOnClickListener(v -> showCategoryDialog());

        if (sliderDays != null) {
            sliderDays.addOnChangeListener((slider, value, fromUser) -> tvDaysValue.setText((int) value + " Days"));
        }

        btnSubmit.setOnClickListener(v -> {
            if (validateInput()) submitItem();
        });
    }

    private void showCategoryDialog() {
        String[] categories = {"Electronics", "Textbooks", "Lab Gear", "Sports", "Others"};
        new AlertDialog.Builder(this)
                .setTitle("Select Category")
                .setItems(categories, (dialog, which) -> tvCategory.setText(categories[which]))
                .show();
    }

    private void showPreview(Uri uri) {
        photoUri = uri;
        if (ivItemPreview != null) {
            ivItemPreview.setImageURI(uri);
            imageContainer.setVisibility(View.VISIBLE);
            emptyUploadState.setVisibility(View.GONE);
        }
    }

    private boolean validateInput() {
        if (etItemName.getText().toString().trim().isEmpty()) {
            etItemName.setError("Title required");
            return false;
        }
        if (tvCategory.getText().toString().contains("Select")) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (photoUri == null && existingImageUrl == null) {
            Toast.makeText(this, "Photo required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void submitItem() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Saving...");

        if (photoUri != null) {
            com.example.borrowme.utils.ImgBBUploader.uploadImage(this, photoUri, new com.example.borrowme.utils.ImgBBUploader.UploadCallback() {
                @Override
                public void onSuccess(String url) {
                    runOnUiThread(() -> saveToFirestore(url, user.getUid()));
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "ImgBB Upload failed: " + error);
                        Toast.makeText(AddItemActivity.this, "Upload failed: " + error, Toast.LENGTH_LONG).show();
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit");
                    });
                }
            });
        } else {
            saveToFirestore(existingImageUrl, user.getUid());
        }
    }

    private void saveToFirestore(String imageUrl, String uid) {
        Map<String, Object> data = new HashMap<>();
        data.put("ownerId", uid);
        data.put("title", etItemName.getText().toString().trim());
        data.put("description", etItemDescription.getText().toString().trim());
        data.put("category", tvCategory.getText().toString());
        data.put("imageUrl", imageUrl);
        data.put("maxBorrowDays", sliderDays.getValue());
        data.put("status", "AVAILABLE");
        data.put("createdAt", FieldValue.serverTimestamp());

        if (existingItemId != null) {
            db.collection("items").document(existingItemId).update(data)
                    .addOnSuccessListener(v -> finish());
        } else {
            db.collection("items").add(data)
                    .addOnSuccessListener(ref -> {
                        db.collection("users").document(uid).update("activeLendCount", FieldValue.increment(1));
                        startActivity(new Intent(this, MyLendingsActivity.class));
                        finish();
                    });
        }
    }

    private void loadExistingItemData() {
        db.collection("items").document(existingItemId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                etItemName.setText(doc.getString("title"));
                etItemDescription.setText(doc.getString("description"));
                tvCategory.setText(doc.getString("category"));
                existingImageUrl = doc.getString("imageUrl");
                if (existingImageUrl != null && ivItemPreview != null) {
                    com.bumptech.glide.Glide.with(this).load(existingImageUrl).into(ivItemPreview);
                    imageContainer.setVisibility(View.VISIBLE);
                    emptyUploadState.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this).setTitle("Select Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) launchCamera();
                    else pickImageLauncher.launch("image/*");
                }).show();
    }

    private void launchCamera() {
        try {
            File file = File.createTempFile("IMG_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            takePhotoLauncher.launch(photoUri);
        } catch (IOException e) { Log.e(TAG, "File error", e); }
    }
}
