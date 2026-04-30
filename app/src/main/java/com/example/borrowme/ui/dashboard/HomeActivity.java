package com.example.borrowme.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        
        View mainView = findViewById(android.R.id.content);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        setupButtons();
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String fullName = document.getString("fullName");
                        if (fullName != null && !fullName.isEmpty()) {
                            TextView tvWelcomeName = findViewById(R.id.tvWelcomeName);
                            if (tvWelcomeName != null) {
                                tvWelcomeName.setText("Hello " + fullName.split(" ")[0] + "!!");
                            }
                        }
                    }
                });
        }
    }

    private void setupButtons() {
        View btnBorrow = findViewById(R.id.btnBorrow);
        if (btnBorrow != null) {
            btnBorrow.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, FeedActivity.class);
                startActivity(intent);
            });
        }

        View btnLend = findViewById(R.id.btnLend);
        if (btnLend != null) {
            btnLend.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, AddItemActivity.class);
                startActivity(intent);
            });
        }

        View btnViewDetails = findViewById(R.id.btnViewDetails);
        if (btnViewDetails != null) {
            btnViewDetails.setOnClickListener(v -> {
                Toast.makeText(this, "Viewing Trust Score Details", Toast.LENGTH_SHORT).show();
            });
        }

        View fabAdd = findViewById(R.id.fabAdd);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, AddItemActivity.class);
                startActivity(intent);
            });
        }

        View tvSeeAll = findViewById(R.id.tvSeeAll);
        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, RequestsManagementActivity.class);
                startActivity(intent);
            });
        }

        // Navigation Links
        View navLendings = findViewById(R.id.navLendings);
        if (navLendings != null) {
            navLendings.setOnClickListener(v -> {
                Intent intent = new Intent(this, MyLendingsActivity.class);
                startActivity(intent);
            });
        }
        
        View navRequests = findViewById(R.id.navRequests);
        if (navRequests != null) {
            navRequests.setOnClickListener(v -> {
                Intent intent = new Intent(this, RequestsManagementActivity.class);
                startActivity(intent);
            });
        }
        
        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }
}
