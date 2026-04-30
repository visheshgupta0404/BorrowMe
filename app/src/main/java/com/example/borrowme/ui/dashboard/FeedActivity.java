package com.example.borrowme.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.borrowme.R;
import com.example.borrowme.ui.auth.LoginActivity;
import com.example.borrowme.ui.borrowing.RequestsManagementActivity;
import com.example.borrowme.ui.items.AddItemActivity;
import com.example.borrowme.ui.items.ItemDetailActivity;
import com.example.borrowme.ui.profile.MyLendingsActivity;
import com.example.borrowme.ui.profile.ProfileActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {
    private static final String TAG = "FeedActivity";
    private RecyclerView rvFeedItems;
    private FeedAdapter adapter;
    private List<DocumentSnapshot> allItems = new ArrayList<>();
    private List<DocumentSnapshot> filteredItems = new ArrayList<>();
    private FirebaseFirestore db;
    private EditText etSearch;
    private String currentCategory = "All Items";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_feed);
            
            View root = findViewById(R.id.feed_root);
            if (root != null) {
                ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
            }

            db = FirebaseFirestore.getInstance();
            etSearch = findViewById(R.id.etSearch);
            rvFeedItems = findViewById(R.id.rvFeedItems);

            if (rvFeedItems != null) {
                rvFeedItems.setLayoutManager(new GridLayoutManager(this, 2));
                adapter = new FeedAdapter(filteredItems);
                rvFeedItems.setAdapter(adapter);
            }

            setupUI();
            seedShowcaseItems();
            loadItems();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void seedShowcaseItems() {
        String[] titles = {"Graphing Calculator", "Physics Textbook", "Lab Coat (Size M)"};
        String[] categories = {"Electronics", "Textbooks", "Lab Gear"};
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            String category = categories[i];
            String id = "showcase_" + i;
            
            db.collection("items").document(id).get().addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("itemId", id);
                    item.put("title", title);
                    item.put("category", category);
                    item.put("description", "A mock showcase item for demonstration.");
                    item.put("status", "AVAILABLE");
                    item.put("isShowcase", true);
                    item.put("ownerId", "mock_owner");
                    item.put("imageUrl", "");
                    db.collection("items").document(id).set(item);
                }
            });
        }
    }

    private void loadItems() {
        db.collection("items")
                .whereEqualTo("status", "AVAILABLE")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allItems.clear();
                    allItems.addAll(queryDocumentSnapshots.getDocuments());
                    
                    String initialQuery = getIntent().getStringExtra("searchQuery");
                    if (initialQuery != null && !initialQuery.isEmpty()) {
                        if (etSearch != null) etSearch.setText(initialQuery);
                        applyFilters(initialQuery);
                    } else {
                        applyFilters("");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load items", e));
    }

    private void applyFilters(String query) {
        filteredItems.clear();
        String lowerQuery = query.toLowerCase();
        for (DocumentSnapshot doc : allItems) {
            String title = doc.getString("title");
            String category = doc.getString("category");
            
            boolean matchesSearch = title != null && title.toLowerCase().contains(lowerQuery);
            boolean matchesCategory = currentCategory.equals("All Items") || (category != null && category.equalsIgnoreCase(currentCategory));
            
            if (matchesSearch && matchesCategory) {
                filteredItems.add(doc);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void setupUI() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilters(s.toString());
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Category Buttons
        setupCategoryButton(R.id.btnCatAll, "All Items");
        setupCategoryButton(R.id.btnCatTextbooks, "Textbooks");
        setupCategoryButton(R.id.btnCatElectronics, "Electronics");
        setupCategoryButton(R.id.btnCatLabGear, "Lab Gear");

        // Profile navigation - Fixed: R.id.btnProfileFeed to R.id.ivProfileFeed
        View ivProfileFeed = findViewById(R.id.ivProfileFeed);
        if (ivProfileFeed != null) {
            ivProfileFeed.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }

        // Load profile image
        loadProfileImage();

        // Uniform Bottom Navigation
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        findViewById(R.id.navLendings).setOnClickListener(v -> {
            startActivity(new Intent(this, MyLendingsActivity.class));
            finish();
        });
        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            startActivity(new Intent(this, AddItemActivity.class));
        });
        findViewById(R.id.navRequests).setOnClickListener(v -> {
            startActivity(new Intent(this, RequestsManagementActivity.class));
            finish();
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    private void setupCategoryButton(int resId, String category) {
        MaterialButton btn = findViewById(resId);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                currentCategory = category;
                applyFilters(etSearch != null ? etSearch.getText().toString() : "");
                updateCategoryUI();
            });
        }
    }

    private void updateCategoryUI() {
        int[] ids = {R.id.btnCatAll, R.id.btnCatTextbooks, R.id.btnCatElectronics, R.id.btnCatLabGear};
        String[] cats = {"All Items", "Textbooks", "Electronics", "Lab Gear"};
        for (int i = 0; i < ids.length; i++) {
            MaterialButton btn = findViewById(ids[i]);
            if (btn != null) {
                if (cats[i].equals(currentCategory)) {
                    btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary_blue)));
                    btn.setTextColor(getResources().getColor(R.color.white));
                } else {
                    btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF1F5F9)); // Match updated XML
                    btn.setTextColor(0xFF475569);
                }
            }
        }
    }

    private void loadProfileImage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String url = doc.getString("profileImage");
                    ImageView iv = findViewById(R.id.ivProfileFeed);
                    if (iv != null && url != null && !url.isEmpty()) {
                        Glide.with(this).load(url)
                            .circleCrop()
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(iv);
                    }
                }
            });
        }
    }

    private class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
        private List<DocumentSnapshot> items;

        public FeedAdapter(List<DocumentSnapshot> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_card, parent, false);
            return new FeedViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
            DocumentSnapshot doc = items.get(position);
            String title = doc.getString("title");
            String imageUrl = doc.getString("imageUrl");
            String category = doc.getString("category");
            String itemId = doc.getId();

            holder.tvItemName.setText(title != null ? title : "Unknown Item");
            if (holder.tvCategory != null) holder.tvCategory.setText(category);
            
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
                Intent intent = new Intent(FeedActivity.this, ItemDetailActivity.class);
                intent.putExtra("itemId", itemId);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class FeedViewHolder extends RecyclerView.ViewHolder {
            ImageView ivItemImage;
            TextView tvItemName, tvCategory;

            public FeedViewHolder(@NonNull View itemView) {
                super(itemView);
                ivItemImage = itemView.findViewById(R.id.ivItemImage);
                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvCategory = itemView.findViewById(R.id.tvCategory);
            }
        }
    }
}
