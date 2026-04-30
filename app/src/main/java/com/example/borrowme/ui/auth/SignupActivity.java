package com.example.borrowme.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    private static final String TAG = "SignupActivity";
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
            Log.e(TAG, "Error in onCreate", e);
        }
    }

    private void setupUI() {
        EditText etFullName = findViewById(R.id.etFullName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        ImageView ivTogglePassword = findViewById(R.id.ivTogglePassword);
        MaterialButton btnCreateAccount = findViewById(R.id.btnCreateAccount);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        
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

                if (!email.toLowerCase().endsWith("@bmu.edu.in") && !email.toLowerCase().endsWith("@university.edu")) {
                    if (etEmail != null) etEmail.setError("Use your college email");
                    return;
                }

                if (password.length() < 6) {
                    if (etPassword != null) etPassword.setError("Minimum 6 characters");
                    return;
                }

                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                btnCreateAccount.setEnabled(false);
                
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) saveUserToFirestore(user, fullName, email);
                            } else {
                                if (progressBar != null) progressBar.setVisibility(View.GONE);
                                btnCreateAccount.setEnabled(true);
                                String error = task.getException() != null ? task.getException().getMessage() : "Failed";
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

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
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
                    Toast.makeText(this, "Welcome to BorrowMe!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore error", e);
                    btnCreateAccountState(true);
                });
    }

    private void btnCreateAccountState(boolean enabled) {
        MaterialButton btn = findViewById(R.id.btnCreateAccount);
        ProgressBar pb = findViewById(R.id.progressBar);
        if (btn != null) btn.setEnabled(enabled);
        if (pb != null) pb.setVisibility(enabled ? View.GONE : View.VISIBLE);
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
