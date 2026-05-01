package com.example.borrowme.ui.borrowing;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.borrowme.R;
import com.example.borrowme.ui.auth.LoginActivity;
import com.example.borrowme.ui.dashboard.HomeActivity;
import com.example.borrowme.ui.items.AddItemActivity;
import com.example.borrowme.ui.items.ItemDetailActivity;
import com.example.borrowme.ui.profile.MyLendingsActivity;
import com.example.borrowme.ui.profile.ProfileActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RequestsManagementActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView rvRequests;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<DocumentSnapshot> allRequests = new ArrayList<>();
    private final List<DocumentSnapshot> filteredRequests = new ArrayList<>();
    private RequestsAdapter adapter;
    private String currentTab = "PENDING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_requests_management);

            initViews();
            setupListeners();
            setupRecyclerView();
            seedMockRequests();
        } catch (Exception e) {
            Log.e("RequestsActivity", "Error in onCreate", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            loadRequests();
        }
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        rvRequests = findViewById(R.id.rvRequests);
    }

    private void setupListeners() {
        View backBtn = findViewById(R.id.btnBack);
        if (backBtn != null) backBtn.setOnClickListener(v -> finish());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String text = tab.getText() != null ? tab.getText().toString().toUpperCase() : "PENDING";
                currentTab = "RETURN PENDING".equals(text) ? "RETURN_PENDING" : text;
                filterRequests();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        View navHome = findViewById(R.id.navHome);
        if (navHome != null) navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        View navLendings = findViewById(R.id.navLendings);
        if (navLendings != null) navLendings.setOnClickListener(v -> {
            startActivity(new Intent(this, MyLendingsActivity.class));
            finish();
        });

        View navRequests = findViewById(R.id.navRequests);
        if (navRequests != null) navRequests.setOnClickListener(v -> loadRequests());

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });

        View fabAdd = findViewById(R.id.fabAdd);
        if (fabAdd != null) fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddItemActivity.class));
        });
    }

    private void setupRecyclerView() {
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestsAdapter(filteredRequests);
        rvRequests.setAdapter(adapter);
    }

    private void loadRequests() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        db.collection("requests").whereEqualTo("borrowerId", user.getUid())
            .get()
            .addOnSuccessListener(borrowerSnap -> {
                List<DocumentSnapshot> tempRequests = new ArrayList<>(borrowerSnap.getDocuments());
                db.collection("requests").whereEqualTo("lenderId", user.getUid())
                    .get()
                    .addOnSuccessListener(lenderSnap -> {
                        tempRequests.addAll(lenderSnap.getDocuments());
                        
                        // Distinct by ID
                        Map<String, DocumentSnapshot> uniqueMap = new HashMap<>();
                        for (DocumentSnapshot doc : tempRequests) {
                            uniqueMap.put(doc.getId(), doc);
                        }
                        allRequests = new ArrayList<>(uniqueMap.values());
                        filterRequests();
                    });
            })
            .addOnFailureListener(e -> Log.e("RequestsActivity", "Load error", e));
    }

    private void filterRequests() {
        filteredRequests.clear();
        for (DocumentSnapshot doc : allRequests) {
            if (currentTab.equals(doc.getString("status"))) {
                filteredRequests.add(doc);
            }
        }
        
        Collections.sort(filteredRequests, (d1, d2) -> {
            Date date1 = d1.getDate("timestamp");
            if (date1 == null) date1 = d1.getDate("requestedAt");
            if (date1 == null) date1 = new Date(0);
            
            Date date2 = d2.getDate("timestamp");
            if (date2 == null) date2 = d2.getDate("requestedAt");
            if (date2 == null) date2 = new Date(0);
            
            return date2.compareTo(date1); // Descending
        });
        
        adapter.notifyDataSetChanged();
    }

    private void updateRequestStatus(String requestId, String newStatus, String borrowerId, String lenderId) {
        db.collection("requests").document(requestId)
            .update("status", newStatus)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Request " + newStatus, Toast.LENGTH_SHORT).show();
                if ("APPROVED".equals(newStatus)) {
                    if (borrowerId != null) db.collection("users").document(borrowerId).update("activeBorrowCount", FieldValue.increment(1));
                    if (lenderId != null) db.collection("users").document(lenderId).update("activeLendCount", FieldValue.increment(1));
                }
                loadRequests();
            });
    }

    private void seedMockRequests() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        db.collection("requests").limit(1).get().addOnSuccessListener(snapshots -> { 
            if (snapshots.isEmpty()) {
                Map<String, Object> mock1 = new HashMap<>();
                mock1.put("itemId", "showcase_0");
                mock1.put("borrowerId", "dummy_id");
                mock1.put("lenderId", user.getUid());
                mock1.put("status", "PENDING");
                mock1.put("timestamp", FieldValue.serverTimestamp());
                db.collection("requests").add(mock1);
            }
        });
    }

    class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {
        private final List<DocumentSnapshot> items;

        RequestsAdapter(List<DocumentSnapshot> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DocumentSnapshot req = items.get(position);
            String itemId = req.getString("itemId");
            String borrowerId = req.getString("borrowerId");
            String lenderId = req.getString("lenderId");
            String status = req.getString("status");
            if (status == null) status = "PENDING";
            Date timestamp = req.getDate("timestamp");
            if (timestamp == null) timestamp = req.getDate("requestedAt");

            if (timestamp != null && holder.tvRequestDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                holder.tvRequestDate.setText("Requested on: " + sdf.format(timestamp));
            }

            if (itemId != null) {
                db.collection("items").document(itemId).get().addOnSuccessListener(doc -> { 
                    if (doc.exists()) {
                        if (holder.tvItemName != null) holder.tvItemName.setText(doc.getString("title"));
                        String url = doc.getString("imageUrl");
                        if (url != null && !url.isEmpty() && holder.ivItemImage != null) {
                            Glide.with(holder.itemView.getContext()).load(url).placeholder(R.drawable.ic_launcher_foreground).into(holder.ivItemImage);
                        }
                    }
                });
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String otherId = (currentUser != null && currentUser.getUid().equals(lenderId)) ? borrowerId : lenderId;
            if (otherId != null) {
                db.collection("users").document(otherId).get().addOnSuccessListener(doc -> { 
                    if (doc.exists()) {
                        String name = doc.getString("fullName");
                        if (name == null) name = "User";
                        String hostel = doc.getString("hostel");
                        if (hostel == null) hostel = "";
                        String profileImage = doc.getString("profileImage");
                        if (holder.tvUserInfo != null) {
                            holder.tvUserInfo.setText(!hostel.isEmpty() ? name + " • " + hostel : name);
                        }
                        
                        if (profileImage != null && !profileImage.isEmpty() && holder.ivUserSmall != null) {
                            Glide.with(holder.itemView.getContext())
                                .load(profileImage)
                                .circleCrop()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(holder.ivUserSmall);
                        }
                    }
                });
            }

            if (holder.tvStatus != null) holder.tvStatus.setText(status.replace("_", " "));
            if (holder.btnApprove != null) holder.btnApprove.setVisibility(View.GONE);
            if (holder.btnReject != null) holder.btnReject.setVisibility(View.GONE);

            switch (status) {
                case "PENDING":
                    if (holder.tvStatus != null) {
                        holder.tvStatus.setTextColor(Color.parseColor("#F59E0B"));
                        holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_subtle);
                    }
                    if (holder.statusIndicator != null) holder.statusIndicator.setBackgroundColor(Color.parseColor("#F59E0B"));
                    if (currentUser != null && currentUser.getUid().equals(lenderId)) {
                        if (holder.btnApprove != null) {
                            holder.btnApprove.setVisibility(View.VISIBLE);
                            holder.btnApprove.setOnClickListener(v -> updateRequestStatus(req.getId(), "APPROVED", borrowerId, lenderId));
                        }
                        if (holder.btnReject != null) {
                            holder.btnReject.setVisibility(View.VISIBLE);
                            holder.btnReject.setOnClickListener(v -> updateRequestStatus(req.getId(), "REJECTED", null, null));
                        }
                    }
                    break;
                case "APPROVED":
                    if (holder.tvStatus != null) {
                        holder.tvStatus.setTextColor(Color.parseColor("#10B981"));
                        holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_subtle);
                    }
                    if (holder.statusIndicator != null) holder.statusIndicator.setBackgroundColor(Color.parseColor("#10B981"));
                    break;
                case "REJECTED":
                    if (holder.tvStatus != null) {
                        holder.tvStatus.setTextColor(Color.parseColor("#EF4444"));
                        holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_subtle);
                    }
                    if (holder.statusIndicator != null) holder.statusIndicator.setBackgroundColor(Color.parseColor("#EF4444"));
                    break;
                case "RETURN_PENDING":
                    if (holder.tvStatus != null) {
                        holder.tvStatus.setTextColor(Color.parseColor("#2563EB"));
                        holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_subtle);
                    }
                    if (holder.statusIndicator != null) holder.statusIndicator.setBackgroundColor(Color.parseColor("#2563EB"));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView tvItemName;
            final TextView tvUserInfo;
            final ImageView ivItemImage;
            final Button btnApprove;
            final Button btnReject;
            final TextView tvStatus;
            final View statusIndicator;
            final TextView tvRequestDate;
            final ImageView ivUserSmall;

            ViewHolder(View view) {
                super(view);
                tvItemName = view.findViewById(R.id.tvItemName);
                tvUserInfo = view.findViewById(R.id.tvUserInfo);
                ivItemImage = view.findViewById(R.id.ivItemImage);
                btnApprove = view.findViewById(R.id.btnApprove);
                btnReject = view.findViewById(R.id.btnReject);
                tvStatus = view.findViewById(R.id.tvStatus);
                statusIndicator = view.findViewById(R.id.statusIndicator);
                tvRequestDate = view.findViewById(R.id.tvRequestDate);
                ivUserSmall = view.findViewById(R.id.ivUserSmall);
            }
        }
    }
}
