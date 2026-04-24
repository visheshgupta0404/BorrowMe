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
import com.example.borrowme.ui.borrowing.RequestsManagementActivity;
import com.example.borrowme.ui.items.AddItemActivity;
import com.example.borrowme.ui.profile.MyLendingsActivity;
import com.example.borrowme.ui.profile.ProfileActivity;

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

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddItemActivity.class);
            startActivity(intent);
        });

        // Navigation Links
        findViewById(R.id.navLendings).setOnClickListener(v -> {
            Intent intent = new Intent(this, MyLendingsActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.navRequests).setOnClickListener(v -> {
            Intent intent = new Intent(this, RequestsManagementActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
        
        // Feed cards are static in layout, but typically they'd lead to ItemDetailActivity
        // In a real app, you'd set click listeners on the included items.
    }
}
