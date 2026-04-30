package com.example.borrowme.ui.items;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.borrowme.R;
import com.example.borrowme.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ItemDetailActivity extends AppCompatActivity {
    private static final String TAG = "ItemDetailActivity";
    private String itemId;
    private FirebaseFirestore db;
    private String ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_item_detail);

            db = FirebaseFirestore.getInstance();
            itemId = getIntent().getStringExtra("itemId");

            findViewById(R.id.btnBack).setOnClickListener(v -> finish());

            if (itemId == null || itemId.isEmpty()) {
                Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            loadItemDetails();

            MaterialButton btnRequestBorrow = findViewById(R.id.btnRequestBorrow);
            if (btnRequestBorrow != null) {
                btnRequestBorrow.setOnClickListener(v -> requestBorrow());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            safeStartActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void safeStartActivity(Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start activity", e);
        }
    }

    private void loadItemDetails() {
        db.collection("items").document(itemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String category = documentSnapshot.getString("category");
                        String description = documentSnapshot.getString("description");
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        ownerId = documentSnapshot.getString("ownerId");

                        TextView tvItemTitle = findViewById(R.id.tvItemTitle);
                        TextView tvItemCategory = findViewById(R.id.tvItemCategory);
                        TextView tvItemDescription = findViewById(R.id.tvItemDescription);
                        ImageView ivItemDetailImage = findViewById(R.id.ivItemDetailImage);
                        TextView tvOwnedBadge = findViewById(R.id.tvOwnedBadge);

                        if (tvItemTitle != null) tvItemTitle.setText(title);
                        if (tvItemCategory != null) tvItemCategory.setText(category != null ? category.toUpperCase() : "");
                        if (tvItemDescription != null) tvItemDescription.setText(description);
                        
                        if (ivItemDetailImage != null) {
                            Glide.with(this)
                                    .load(imageUrl != null && !imageUrl.isEmpty() ? imageUrl : R.drawable.ic_launcher_foreground)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .into(ivItemDetailImage);
                        }

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        View bottomBar = findViewById(R.id.bottomBar);
                        
                        if (user != null && ownerId != null && user.getUid().equals(ownerId)) {
                            if (tvOwnedBadge != null) tvOwnedBadge.setVisibility(View.VISIBLE);
                            if (bottomBar != null) bottomBar.setVisibility(View.GONE);
                        } else {
                            if (tvOwnedBadge != null) tvOwnedBadge.setVisibility(View.GONE);
                            if (bottomBar != null) bottomBar.setVisibility(View.VISIBLE);
                        }

                        loadOwnerDetails();
                    } else {
                        Toast.makeText(this, "Item no longer available", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading item", e));
    }

    private void loadOwnerDetails() {
        if (ownerId == null || ownerId.isEmpty() || ownerId.equals("mock_owner")) {
            TextView tvOwnerName = findViewById(R.id.tvOwnerName);
            if (tvOwnerName != null) tvOwnerName.setText("System Listing");
            return;
        }

        db.collection("users").document(ownerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String profileImage = documentSnapshot.getString("profileImage");
                        String hostel = documentSnapshot.getString("hostel");
                        Long repScore = documentSnapshot.getLong("reputationScore");

                        TextView tvOwnerName = findViewById(R.id.tvOwnerName);
                        ImageView ivOwnerProfile = findViewById(R.id.ivOwnerProfile);
                        TextView tvOwnerReputation = findViewById(R.id.tvOwnerReputation);

                        if (tvOwnerName != null) {
                            String info = fullName != null ? fullName : "User";
                            if (hostel != null) info += " • " + hostel;
                            tvOwnerName.setText(info);
                        }
                        
                        if (ivOwnerProfile != null) {
                            Glide.with(this).load(profileImage != null && !profileImage.isEmpty() ? profileImage : R.drawable.ic_person)
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person)
                                    .circleCrop()
                                    .into(ivOwnerProfile);
                        }
                        
                        if (tvOwnerReputation != null && repScore != null) {
                            tvOwnerReputation.setText(String.format(Locale.getDefault(), "%.1f", repScore / 20.0));
                        }
                    }
                });
    }

    private void requestBorrow() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || ownerId == null) return;

        MaterialButton btnRequestBorrow = findViewById(R.id.btnRequestBorrow);
        if (btnRequestBorrow != null) {
            btnRequestBorrow.setEnabled(false);
            btnRequestBorrow.setText("Requesting...");
        }

        Map<String, Object> request = new HashMap<>();
        request.put("itemId", itemId);
        request.put("borrowerId", user.getUid());
        request.put("lenderId", ownerId);
        request.put("status", "PENDING");
        request.put("requestedAt", FieldValue.serverTimestamp());
        request.put("timestamp", FieldValue.serverTimestamp());

        db.collection("requests").add(request)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Borrow request sent!", Toast.LENGTH_SHORT).show();
                    if (btnRequestBorrow != null) btnRequestBorrow.setText("Request Sent");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Request error", e);
                    Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show();
                    if (btnRequestBorrow != null) {
                        btnRequestBorrow.setEnabled(true);
                        btnRequestBorrow.setText("Request to Borrow");
                    }
                });
    }
}
