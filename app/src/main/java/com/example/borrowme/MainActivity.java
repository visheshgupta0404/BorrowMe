package com.example.borrowme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.borrowme.ui.dashboard.HomeActivity;
import com.example.borrowme.ui.items.AddItemActivity;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    private ImageView profileImage;
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
                    profileImage.setImageURI(photoUri);
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    photoUri = uri;
                    profileImage.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        View mainView = findViewById(android.R.id.content);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        setupUI();
    }

    private void setupUI() {
        EditText etFullName = findViewById(R.id.etFullName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        ImageView ivTogglePassword = findViewById(R.id.ivTogglePassword);
        MaterialButton btnCreateAccount = findViewById(R.id.btnCreateAccount);
        profileImage = findViewById(R.id.profileImage);
        View btnAddImage = findViewById(R.id.btnAddImage);
        
        View btnSelectHostel = findViewById(R.id.btnSelectHostel);
        if (btnSelectHostel != null) {
            btnSelectHostel.setOnClickListener(v -> showHostelDialog());
        }

        if (btnAddImage != null) {
            btnAddImage.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    showImageSourceDialog();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            });
        }

        if (ivTogglePassword != null && etPassword != null) {
            ivTogglePassword.setOnClickListener(v -> {
                isPasswordVisible = !isPasswordVisible;
                if (isPasswordVisible) {
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ivTogglePassword.setImageResource(R.drawable.ic_visibility);
                } else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ivTogglePassword.setImageResource(R.drawable.ic_visibility_off);
                }
                etPassword.setSelection(etPassword.getText().length());
            });
        }

        if (btnCreateAccount != null) {
            btnCreateAccount.setOnClickListener(v -> {
                String email = etEmail.getText().toString().trim();
                String fullName = etFullName.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (fullName.isEmpty()) {
                    etFullName.setError("Full name is required");
                    return;
                }

                if (email.isEmpty()) {
                    etEmail.setError("Email is required");
                    return;
                }

                if (!email.toLowerCase().endsWith("@bmu.edu.in")) {
                    etEmail.setError("Only @bmu.edu.in emails are allowed");
                    Toast.makeText(this, "Please use your university email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    etPassword.setError("Password must be at least 6 characters");
                    return;
                }

                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            });
        }

        View tvLogin = findViewById(R.id.tvLogin);
        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> {
                Toast.makeText(this, "Navigate to Login", Toast.LENGTH_SHORT).show();
            });
        }

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Profile Image");
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

    private void showHostelDialog() {
        String[] hostels = {
            "Bhagat Singh",
            "Ratan Tata",
            "Kalpana Chawla",
            "APJ Abdul Kalam",
            "Gargi",
            "Homi Baba"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Hostel Block");
        builder.setItems(hostels, (dialog, which) -> {
            android.widget.TextView tvHostel = findViewById(R.id.tvHostel);
            if (tvHostel != null) {
                tvHostel.setText(hostels[which]);
            }
        });
        builder.show();
    }
}
