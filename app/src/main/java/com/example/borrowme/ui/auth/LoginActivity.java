package com.example.borrowme.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_login);

            mAuth = FirebaseAuth.getInstance();

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
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        ImageView ivTogglePassword = findViewById(R.id.ivTogglePassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        ProgressBar progressBar = findViewById(R.id.progressBar);

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

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                String email = etEmail != null ? etEmail.getText().toString().trim() : "";
                String password = etPassword != null ? etPassword.getText().toString().trim() : "";

                if (email.isEmpty()) {
                    if (etEmail != null) etEmail.setError("Email required");
                    return;
                }
                if (password.isEmpty()) {
                    if (etPassword != null) etPassword.setError("Password required");
                    return;
                }

                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                btnLogin.setEnabled(false);

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                checkUserDocument();
                            } else {
                                if (progressBar != null) progressBar.setVisibility(View.GONE);
                                btnLogin.setEnabled(true);
                                String error = task.getException() != null ? task.getException().getMessage() : "Login failed";
                                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        });
            });
        }

        View tvSignUp = findViewById(R.id.tvSignUp);
        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                finish();
            });
        }

        View tvForgotPassword = findViewById(R.id.tvForgotPassword);
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                String email = etEmail != null ? etEmail.getText().toString().trim() : "";
                if (email.isEmpty()) {
                    Toast.makeText(this, "Enter email to reset password", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Reset link sent", Toast.LENGTH_SHORT).show());
            });
        }
    }

    private void checkUserDocument() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Create missing document (e.g. if user signed up but doc failed)
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("uid", user.getUid());
                        userData.put("fullName", user.getDisplayName() != null ? user.getDisplayName() : "User");
                        userData.put("email", user.getEmail());
                        userData.put("hostel", "Not Selected");
                        userData.put("createdAt", System.currentTimeMillis());
                        userData.put("profileImage", null);
                        userData.put("reputationScore", 100);
                        userData.put("activeBorrowCount", 0);
                        userData.put("activeLendCount", 0);
                        
                        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).set(userData);
                    }
                    navigateToHome();
                })
                .addOnFailureListener(e -> navigateToHome());
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
