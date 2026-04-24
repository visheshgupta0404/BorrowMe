package com.example.borrowme;

import android.content.Intent;
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

import com.example.borrowme.ui.items.AddItemActivity;
import com.example.borrowme.ui.items.ItemDetailActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupUI();
        
        // TEMPORARY: For testing screens
        Toast.makeText(this, "Click Create Account to see Add Item Screen", Toast.LENGTH_LONG).show();
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
            // Navigate to Add Item Screen for testing
            Intent intent = new Intent(this, AddItemActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tvLogin).setOnClickListener(v -> {
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
