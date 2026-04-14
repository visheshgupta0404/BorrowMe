package com.example.borrowme.ui.auth;

import android.os.Bundle;
import android.text.InputType;
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
import com.google.android.material.button.MaterialButton;

public class SignupActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupUI();
    }

    private void setupUI() {
        EditText etFullName = findViewById(R.id.etFullName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        ImageView ivTogglePassword = findViewById(R.id.ivTogglePassword);
        MaterialButton btnCreateAccount = findViewById(R.id.btnCreateAccount);
        findViewById(R.id.btnSelectHostel).setOnClickListener(v -> showHostelDialog());

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

        btnCreateAccount.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Account created for " + name, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.tvLogin).setOnClickListener(v -> {
            // Logic for navigating to login will be here
            Toast.makeText(this, "Navigate to Login", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void showHostelDialog() {
        String[] hostels = {"Block A", "Block B", "Block C", "Block D", "Block E"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Hostel Block");
        builder.setItems(hostels, (dialog, which) -> {
            ((android.widget.TextView) findViewById(R.id.tvHostel)).setText(hostels[which]);
        });
        builder.show();
    }
}
