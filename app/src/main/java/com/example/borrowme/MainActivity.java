package com.example.borrowme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.borrowme.ui.dashboard.HomeActivity;
import com.example.borrowme.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private boolean isPasswordVisible = false;
    private ImageView profileImage;
    private Uri photoUri;
    private String currentPhotoPath;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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
                if (success && profileImage != null && photoUri != null) {
                    profileImage.setImageURI(photoUri);
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && profileImage != null) {
                    photoUri = uri;
                    profileImage.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);
            
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            View mainView = findViewById(R.id.main);
            if (mainView != null) {
                ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
            }

            setupUI();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
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
        View tvLogin = findViewById(R.id.tvLogin);
        View btnBack = findViewById(R.id.btnBack);

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
                String email = etEmail != null ? etEmail.getText().toString().trim() : "";
                String fullName = etFullName != null ? etFullName.getText().toString().trim() : "";
                String password = etPassword != null ? etPassword.getText().toString().trim() : "";

                if (fullName.isEmpty()) {
                    if (etFullName != null) etFullName.setError("Full name is required");
                    return;
                }

                if (email.isEmpty()) {
                    if (etEmail != null) etEmail.setError("Email is required");
                    return;
                }

                if (!email.toLowerCase().endsWith("@bmu.edu.in")) {
                    if (etEmail != null) etEmail.setError("Only @bmu.edu.in emails are allowed");
                    Toast.makeText(this, "Please use your university email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    if (etPassword != null) etPassword.setError("Password must be at least 6 characters");
                    return;
                }

                btnCreateAccount.setEnabled(false);
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    saveUserToFirestore(user, fullName, email);
                                }
                            } else {
                                btnCreateAccount.setEnabled(true);
                                String error = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                            }
                        });
            });
        }

        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            });
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void saveUserToFirestore(FirebaseUser user, String fullName, String email) {
        TextView tvHostel = findViewById(R.id.tvHostel);
        String hostel = tvHostel != null ? tvHostel.getText().toString() : "Not Selected";
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("hostel", hostel);
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("profileImage", null);
        userData.put("reputationScore", 100);
        userData.put("activeBorrowCount", 0);
        userData.put("activeLendCount", 0);

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore error", e);
                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                });
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Profile Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) launchCamera();
                    else pickImageLauncher.launch("image/*");
                })
                .show();
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePhotoLauncher.launch(photoUri);
        } catch (IOException ex) {
            Log.e(TAG, "Camera error", ex);
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void showHostelDialog() {
        String[] hostels = {
            "Bhagat Singh", "Ratan Tata", "Kalpana Chawla", 
            "APJ Abdul Kalam", "Gargi", "Homi Baba"
        };
        new AlertDialog.Builder(this)
                .setTitle("Select Hostel Block")
                .setItems(hostels, (dialog, which) -> {
                    TextView tvHostel = findViewById(R.id.tvHostel);
                    if (tvHostel != null) tvHostel.setText(hostels[which]);
                })
                .show();
    }
}
