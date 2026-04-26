package com.example.borrowme.ui.items;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.borrowme.R;
import com.example.borrowme.ui.profile.MyLendingsActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddItemActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnChooseFiles;
    private EditText etItemName;
    private TextView tvCategory;
    private TextView tvDaysValue;
    private Slider sliderDays;
    private MaterialSwitch switchHostelOnly;
    private MaterialButton btnSubmit;

    private View imageContainer;
    private View emptyUploadState;
    private ImageView ivItemPreview;
    private ImageButton btnRemoveImage;

    private Uri photoUri;
    private String currentPhotoPath;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showImageSourceDialog();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    showPreview(photoUri);
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    showPreview(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnChooseFiles = findViewById(R.id.btnChooseFiles);
        etItemName = findViewById(R.id.etItemName);
        tvCategory = findViewById(R.id.tvCategory);
        tvDaysValue = findViewById(R.id.tvDaysValue);
        sliderDays = findViewById(R.id.sliderDays);
        switchHostelOnly = findViewById(R.id.switchHostelOnly);
        btnSubmit = findViewById(R.id.btnSubmit);

        imageContainer = findViewById(R.id.imageContainer);
        emptyUploadState = findViewById(R.id.emptyUploadState);
        ivItemPreview = findViewById(R.id.ivItemPreview);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnChooseFiles.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                showImageSourceDialog();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnRemoveImage.setOnClickListener(v -> {
            photoUri = null;
            imageContainer.setVisibility(View.GONE);
            emptyUploadState.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.btnSelectCategory).setOnClickListener(v -> {
            showCategoryDialog();
        });

        sliderDays.addOnChangeListener((slider, value, fromUser) -> {
            tvDaysValue.setText((int) value + " Days");
        });

        btnSubmit.setOnClickListener(v -> {
            if (validateInput()) {
                submitItem();
            }
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                launchCamera();
            } else {
                pickImageLauncher.launch("image/*");
            }
        });
        builder.show();
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePhotoLauncher.launch(photoUri);
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void showPreview(Uri uri) {
        photoUri = uri;
        ivItemPreview.setImageURI(uri);
        imageContainer.setVisibility(View.VISIBLE);
        emptyUploadState.setVisibility(View.GONE);
    }

    private void showCategoryDialog() {
        String[] categories = {"Electronics", "Textbooks", "Lab Gear", "Sports", "Others"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Category");
        builder.setItems(categories, (dialog, which) -> {
            tvCategory.setText(categories[which]);
        });
        builder.show();
    }

    private boolean validateInput() {
        String itemName = etItemName.getText().toString().trim();
        if (itemName.isEmpty()) {
            etItemName.setError("Item name is required");
            return false;
        }

        if (tvCategory.getText().toString().equals("Select a category")) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (photoUri == null) {
            Toast.makeText(this, "Please upload a photo of the item", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void submitItem() {
        Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MyLendingsActivity.class);
        startActivity(intent);
        finish();
    }
}
