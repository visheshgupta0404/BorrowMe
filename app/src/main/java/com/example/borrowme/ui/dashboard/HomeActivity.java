package com.example.borrowme.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.borrowme.R;
import com.example.borrowme.ui.auth.LoginActivity;
import com.example.borrowme.ui.borrowing.RequestsManagementActivity;
import com.example.borrowme.ui.items.AddItemActivity;
import com.example.borrowme.ui.items.ItemDetailActivity;
import com.example.borrowme.ui.profile.MyLendingsActivity;
import com.example.borrowme.ui.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeRunnable;
    private RecyclerView rvActiveRequests;
    private ActiveRequestsAdapter requestsAdapter;
    private List<DocumentSnapshot> activeRequestsList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_home);
            
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

            rvActiveRequests = findViewById(R.id.rvActiveRequests);
            if (rvActiveRequests != null) {
                rvActiveRequests.setLayoutManager(new LinearLayoutManager(this));
                requestsAdapter = new ActiveRequestsAdapter(activeRequestsList);
                rvActiveRequests.setAdapter(requestsAdapter);
            }

            setupButtons();
            loadUserData();
            loadActiveRequests();
            
            timeRunnable = new Runnable() {
                @Override
                public void run() {
                    updateDateTime();
                    timeHandler.postDelayed(this, 60000);
                }
            };
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            safeStartActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        timeHandler.post(timeRunnable);
        loadUserData();
        loadActiveRequests();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timeHandler.removeCallbacks(timeRunnable);
    }

    private void safeStartActivity(Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start activity", e);
            Toast.makeText(this, "Operation failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDateTime() {
        TextView tvCurrentDateTime = findViewById(R.id.tvCurrentDateTime);
        if (tvCurrentDateTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d • h:mm a", Locale.getDefault());
            tvCurrentDateTime.setText(sdf.format(new Date()));
        }
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String fullName = document.getString("fullName");
                        String profileImageUrl = document.getString("profileImage");
                        Long repScore = document.getLong("reputationScore");
                        Long lendCount = document.getLong("activeLendCount");
                        
                        if (fullName != null) {
                            TextView tvWelcomeName = findViewById(R.id.tvWelcomeName);
                            if (tvWelcomeName != null) {
                                tvWelcomeName.setText("Hello " + fullName.split(" ")[0] + "!!");
                            }
                        }

                        ImageView ivProfileHome = findViewById(R.id.ivProfileHome);
                        ImageView ivTrustCardProfile = findViewById(R.id.ivTrustCardProfile);
                        
                        if (ivProfileHome != null) {
                            Glide.with(this).load(profileImageUrl != null && !profileImageUrl.isEmpty() ? profileImageUrl : R.drawable.ic_person)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .circleCrop()
                                .into(ivProfileHome);
                        }
                        if (ivTrustCardProfile != null) {
                            Glide.with(this).load(profileImageUrl != null && !profileImageUrl.isEmpty() ? profileImageUrl : R.drawable.ic_person)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(ivTrustCardProfile);
                        }

                        TextView tvTrustScoreHome = findViewById(R.id.tvTrustScoreHome);
                        TextView tvCommunityLends = findViewById(R.id.tvCommunityLends);
                        if (tvTrustScoreHome != null && repScore != null) {
                            tvTrustScoreHome.setText(String.format(Locale.getDefault(), "%.1f", repScore / 20.0));
                        }
                        if (tvCommunityLends != null) {
                            tvCommunityLends.setText("Based on " + (lendCount != null ? lendCount : 0) + " community lends");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading user", e));
        }
    }

    private void loadActiveRequests() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        db.collection("requests")
                .whereEqualTo("borrowerId", uid)
                .whereIn("status", java.util.Arrays.asList("PENDING", "APPROVED"))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(borrowerSnaps -> {
                    activeRequestsList.clear();
                    activeRequestsList.addAll(borrowerSnaps.getDocuments());
                    
                    db.collection("requests")
                            .whereEqualTo("lenderId", uid)
                            .whereIn("status", java.util.Arrays.asList("PENDING", "APPROVED"))
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(5)
                            .get()
                            .addOnSuccessListener(lenderSnaps -> {
                                activeRequestsList.addAll(lenderSnaps.getDocuments());
                                // Sort by timestamp if possible
                                if (requestsAdapter != null) requestsAdapter.notifyDataSetChanged();
                            });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading requests", e));
    }

    private void setupButtons() {
        View btnSearchGo = findViewById(R.id.btnSearchGo);
        EditText etSearchHome = findViewById(R.id.etSearchHome);
        if (btnSearchGo != null && etSearchHome != null) {
            btnSearchGo.setOnClickListener(v -> {
                String query = etSearchHome.getText().toString().trim();
                Intent intent = new Intent(HomeActivity.this, FeedActivity.class);
                intent.putExtra("searchQuery", query);
                safeStartActivity(intent);
            });
        }

        ImageView ivProfileHome = findViewById(R.id.ivProfileHome);
        if (ivProfileHome != null) {
            ivProfileHome.setOnClickListener(v -> safeStartActivity(new Intent(this, ProfileActivity.class)));
        }

        findViewById(R.id.btnViewDetails).setOnClickListener(v -> safeStartActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.btnBorrow).setOnClickListener(v -> safeStartActivity(new Intent(this, FeedActivity.class)));
        findViewById(R.id.btnLend).setOnClickListener(v -> safeStartActivity(new Intent(this, AddItemActivity.class)));
        findViewById(R.id.fabAdd).setOnClickListener(v -> safeStartActivity(new Intent(this, AddItemActivity.class)));
        findViewById(R.id.tvSeeAll).setOnClickListener(v -> safeStartActivity(new Intent(this, RequestsManagementActivity.class)));

        // Sticky Bottom Navigation
        findViewById(R.id.navHome).setOnClickListener(v -> {}); // Already on Home
        findViewById(R.id.navLendings).setOnClickListener(v -> safeStartActivity(new Intent(this, MyLendingsActivity.class)));
        findViewById(R.id.navRequests).setOnClickListener(v -> safeStartActivity(new Intent(this, RequestsManagementActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> safeStartActivity(new Intent(this, ProfileActivity.class)));
    }

    private class ActiveRequestsAdapter extends RecyclerView.Adapter<ActiveRequestsAdapter.RequestViewHolder> {
        private List<DocumentSnapshot> requests;

        public ActiveRequestsAdapter(List<DocumentSnapshot> requests) {
            this.requests = requests;
        }

        @NonNull
        @Override
        public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_request, parent, false);
            return new RequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
            DocumentSnapshot doc = requests.get(position);
            String itemId = doc.getString("itemId");
            String status = doc.getString("status");
            String borrowerId = doc.getString("borrowerId");
            
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getUid().equals(borrowerId)) {
                holder.tvStatusBadge.setText("BORROWING");
                holder.tvStatusBadge.setTextColor(0xFF10B981);
                holder.tvStatusBadge.setBackgroundColor(0x1A10B981);
                holder.tvOtherUserName.setText("From Lender");
            } else {
                holder.tvStatusBadge.setText("LENDING");
                holder.tvStatusBadge.setTextColor(0xFF2563EB);
                holder.tvStatusBadge.setBackgroundColor(0x1A2563EB);
                holder.tvOtherUserName.setText("To Borrower");
            }

            if (itemId != null) {
                db.collection("items").document(itemId).get().addOnSuccessListener(itemDoc -> {
                    if (itemDoc.exists()) {
                        holder.tvItemTitle.setText(itemDoc.getString("title"));
                        String imageUrl = itemDoc.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(holder.itemView.getContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(holder.ivItemImage);
                        }
                    }
                });
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ItemDetailActivity.class);
                intent.putExtra("itemId", itemId);
                safeStartActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return Math.min(requests.size(), 3);
        }

        class RequestViewHolder extends RecyclerView.ViewHolder {
            ImageView ivItemImage;
            TextView tvItemTitle, tvDueDate, tvStatusBadge, tvOtherUserName;

            public RequestViewHolder(@NonNull View itemView) {
                super(itemView);
                ivItemImage = itemView.findViewById(R.id.ivItemImage);
                tvItemTitle = itemView.findViewById(R.id.tvItemTitle);
                tvDueDate = itemView.findViewById(R.id.tvDueDate);
                tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
                tvOtherUserName = itemView.findViewById(R.id.tvOtherUserName);
            }
        }
    }
}
