package com.example.borrowme.ui.dashboard;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.borrowme.R;

public class FeedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feed);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupUI();
    }

    private void setupUI() {
        findViewById(R.id.btnFilter).setOnClickListener(v -> 
            Toast.makeText(this, "Opening Filter Options", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.fabAdd).setOnClickListener(v -> 
            Toast.makeText(this, "Opening Add Item Screen", Toast.LENGTH_SHORT).show()
        );

        // Navigation Placeholders
        findViewById(R.id.navLendings).setOnClickListener(v -> Toast.makeText(this, "Lendings Tab", Toast.LENGTH_SHORT).show());
        findViewById(R.id.navRequests).setOnClickListener(v -> Toast.makeText(this, "Requests Tab", Toast.LENGTH_SHORT).show());
        findViewById(R.id.navProfile).setOnClickListener(v -> Toast.makeText(this, "Profile Tab", Toast.LENGTH_SHORT).show());
    }
}
