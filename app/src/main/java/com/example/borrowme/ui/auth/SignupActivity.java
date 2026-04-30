package com.example.borrowme.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.borrowme.R;
import com.example.borrowme.ui.dashboard.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_signup);
            
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
            android.util.Log.e("SignupActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error starting Signup: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupUI() {
        EditText etFullName = findViewById(R.id.etFullName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        ImageView ivTogglePassword = findViewById(R.id.ivTogglePassword);
        MaterialButton btnCreateAccount = findViewById(R.id.btnCreateAccount);
        
        View btnSelectHostel = findViewById(R.id.btnSelectHostel);
        if (btnSelectHostel != null) {
            btnSelectHostel.setOnClickListener(v -> showHostelDialog());
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

                // Create user in Firebase
                android.widget.ProgressBar progressBar = findViewById(R.id.progressBar);
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                
                btnCreateAccount.setEnabled(false);
                android.util.Log.d("SignupActivity", "Starting Firebase Auth for: " + email);
                
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                android.util.Log.d("SignupActivity", "Auth successful");
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    saveUserToFirestore(user, fullName, email);
                                }
                            } else {
                                android.util.Log.e("SignupActivity", "Auth failed", task.getException());
                                if (progressBar != null) progressBar.setVisibility(View.GONE);
                                btnCreateAccount.setEnabled(true);
                                String error = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                                Toast.makeText(SignupActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        });
            });
        }

        View tvLogin = findViewById(R.id.tvLogin);
        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            });
        }

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveUserToFirestore(FirebaseUser user, String fullName, String email) {
        android.widget.TextView tvHostel = findViewById(R.id.tvHostel);
        String hostel = tvHostel != null ? tvHostel.getText().toString() : "Not Selected";
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("hostel", hostel);
        userData.put("createdAt", System.currentTimeMillis());

        android.util.Log.d("SignupActivity", "Saving user to Firestore: " + user.getUid());
        
        // Start saving to Firestore in the background
        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        android.util.Log.e("SignupActivity", "Background Firestore write failed", task.getException());
                    }
                });

        // IMMEDIATELY navigate to Home. 
        // Firestore will continue saving in the background even as the screen changes.
        android.widget.ProgressBar progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "Welcome to BorrowMe!", Toast.LENGTH_SHORT).show();
        navigateToHome();
    }

    private void showHostelDialog() {
        String[] hostels = {
            "Bhagat Singh Hostel", 
            "Ratan Tata Hostel", 
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
