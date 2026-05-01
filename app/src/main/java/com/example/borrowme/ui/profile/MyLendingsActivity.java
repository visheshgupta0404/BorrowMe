package com.example.borrowme.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.borrowme.R;
import com.example.borrowme.databinding.ActivityMyLendingsBinding;
import com.example.borrowme.ui.auth.LoginActivity;
import com.example.borrowme.ui.borrowing.RequestsManagementActivity;
import com.example.borrowme.ui.dashboard.HomeActivity;
import com.example.borrowme.ui.items.AddItemActivity;
import com.example.borrowme.ui.items.ItemDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class MyLendingsActivity extends AppCompatActivity {

    private ActivityMyLendingsBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<DocumentSnapshot> lendingsList = new ArrayList<>();
    private MyLendingsGridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityMyLendingsBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            setupUI();
            setupClickListeners();
        } catch (Exception e) {
            Log.e("MyLendings", "Error in onCreate", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            loadMyLendings();
        }
    }

    private void setupUI() {
        binding.rvMyLendings.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new MyLendingsGridAdapter(lendingsList);
        binding.rvMyLendings.setAdapter(adapter);
    }

    private void loadMyLendings() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("items")
            .whereEqualTo("ownerId", user.getUid())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                lendingsList.clear();
                lendingsList.addAll(querySnapshot.getDocuments());
                adapter.notifyDataSetChanged();
                
                if (lendingsList.isEmpty()) {
                    Toast.makeText(this, "You haven't listed any items yet", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e("MyLendings", "Error loading items", e);
                Toast.makeText(this, "Failed to load lendings. Check internet.", Toast.LENGTH_SHORT).show();
            });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        
        binding.navLendings.setOnClickListener(v -> {
            // Already here
        });
        
        binding.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddItemActivity.class));
        });
        
        binding.navRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, RequestsManagementActivity.class));
            finish();
        });
        
        binding.navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    class MyLendingsGridAdapter extends RecyclerView.Adapter<MyLendingsGridAdapter.ViewHolder> {
        private final List<DocumentSnapshot> items;

        MyLendingsGridAdapter(List<DocumentSnapshot> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feed_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DocumentSnapshot doc = items.get(position);
            String title = doc.getString("title") != null ? doc.getString("title") : "Unknown Item";
            String category = doc.getString("category") != null ? doc.getString("category") : "Category";
            String status = doc.getString("status") != null ? doc.getString("status") : "AVAILABLE";
            String imageUrl = doc.getString("imageUrl");

            holder.tvItemName.setText(title);
            holder.tvCategory.setText(category.toUpperCase());
            holder.tvStatusBadge.setText(status);
            
            if ("AVAILABLE".equals(status)) {
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green);
            } else {
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange);
            }

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.ivItemImage);
            } else {
                holder.ivItemImage.setImageResource(R.drawable.ic_launcher_foreground);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MyLendingsActivity.this, ItemDetailActivity.class);
                intent.putExtra("itemId", doc.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivItemImage;
            final TextView tvCategory;
            final TextView tvItemName;
            final TextView tvStatusBadge;

            ViewHolder(View view) {
                super(view);
                ivItemImage = view.findViewById(R.id.ivItemImage);
                tvCategory = view.findViewById(R.id.tvCategory);
                tvItemName = view.findViewById(R.id.tvItemName);
                tvStatusBadge = view.findViewById(R.id.tvStatusBadge);
            }
        }
    }
}
