package com.example.borrowme.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.borrowme.R;
import com.example.borrowme.ui.items.AddItemActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupButtons();
    }

    private void setupButtons() {
        findViewById(R.id.btnBorrow).setOnClickListener(v -> {
            // TODO: Navigate to Feed/Borrow Screen
            Toast.makeText(this, "Opening Borrow Feed", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnLend).setOnClickListener(v -> {
            // Navigate to Add Item Screen
            Intent intent = new Intent(HomeActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnViewDetails).setOnClickListener(v -> {
            // TODO: Navigate to Trust Profile
            Toast.makeText(this, "Viewing Trust Score Details", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            // Quick action to add item
            Intent intent = new Intent(HomeActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tvSeeAll).setOnClickListener(v -> {
            // TODO: Navigate to All Requests
            Toast.makeText(this, "Showing all active requests", Toast.LENGTH_SHORT).show();
        });

        // Navigation Placeholders
        findViewById(R.id.navLendings).setOnClickListener(v -> Toast.makeText(this, "Lendings Tab", Toast.LENGTH_SHORT).show());
        findViewById(R.id.navRequests).setOnClickListener(v -> Toast.makeText(this, "Requests Tab", Toast.LENGTH_SHORT).show());
        findViewById(R.id.navProfile).setOnClickListener(v -> Toast.makeText(this, "Profile Tab", Toast.LENGTH_SHORT).show());
    }
}
