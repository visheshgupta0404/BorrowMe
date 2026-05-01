package com.example.borrowme.ui.profile;

import android.Manifest;
import android.util.Log;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.borrowme.R;
import com.example.borrowme.databinding.ActivityProfileBinding;
import com.example.borrowme.ui.auth.LoginActivity;
import com.example.borrowme.ui.auth.SignupActivity;
import com.example.borrowme.ui.borrowing.RequestsManagementActivity;
import com.example.borrowme.ui.dashboard.HomeActivity;
import com.example.borrowme.ui.items.AddItemActivity;
import com.example.borrowme.utils.ImgBBUploader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private Uri photoUri;
    private String currentPhotoPath;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<DocumentSnapshot> activityList = new ArrayList<>();
    private RecentActivityAdapter activityAdapter;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showImageSourceDialog();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && photoUri != null) {
                    uploadImageToImgBB(photoUri);
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    photoUri = uri;
                    uploadImageToImgBB(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        loadUserData();
        loadRecentActivity();
        setupClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void setupRecyclerView() {
        binding.rvRecentActivity.setLayoutManager(new LinearLayoutManager(this));
        activityAdapter = new RecentActivityAdapter(activityList);
        binding.rvRecentActivity.setAdapter(activityAdapter);
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        db.collection("users").document(user.getUid())
            .get()
            .addOnSuccessListener(document -> {
                if (document != null && document.exists()) {
                    String fullName = document.getString("fullName");
                    String hostel = document.getString("hostel");
                    Long repScore = document.getLong("reputationScore");
                    if (repScore == null) repScore = 100L;
                    Long borrowCount = document.getLong("activeBorrowCount");
                    if (borrowCount == null) borrowCount = 0L;
                    
                    binding.tvUserName.setText(fullName != null ? fullName : "User");
                    binding.tvLocation.setText(hostel != null ? hostel : "Not Selected");
                    binding.tvReputationScore.setText(String.format(Locale.getDefault(), "%.1f", repScore / 20.0));
                    binding.tvActiveBorrowsCount.setText(String.valueOf(borrowCount));
                    
                    String profileImageUrl = document.getString("profileImage");
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(binding.ivProfile);
                    }
                }
            });
    }

    private void loadRecentActivity() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        db.collection("users").document(user.getUid()).collection("activity")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener(snapshots -> {
                activityList.clear();
                activityList.addAll(snapshots.getDocuments());
                activityAdapter.notifyDataSetChanged();
                
                if (activityList.isEmpty()) {
                    seedMockActivity();
                }
            });
    }

    private void seedMockActivity() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        Map<String, Object> mock = new HashMap<>();
        mock.put("type", "LENT");
        mock.put("itemTitle", "Calculus Textbook");
        mock.put("otherUserName", "Sarah M.");
        mock.put("timestamp", new Date());
        mock.put("status", "ACTIVE");
        
        db.collection("users").document(user.getUid()).collection("activity").add(mock)
            .addOnSuccessListener(ref -> loadRecentActivity());
    }

    private void uploadImageToImgBB(Uri uri) {
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
        
        ImgBBUploader.uploadImage(this, uri, new ImgBBUploader.UploadCallback() {
            @Override
            public void onSuccess(String url) {
                runOnUiThread(() -> saveProfileImageUrlToFirestore(url));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Log.e("ProfileActivity", "ImgBB Upload failed: " + error);
                    Toast.makeText(ProfileActivity.this, "Upload failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveProfileImageUrlToFirestore(String url) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        db.collection("users").document(user.getUid())
            .update("profileImage", url)
            .addOnSuccessListener(aVoid -> {
                Glide.with(this).load(url).placeholder(R.drawable.ic_person).error(R.drawable.ic_person).into(binding.ivProfile);
                Toast.makeText(this, "Profile image updated!", Toast.LENGTH_SHORT).show();
            });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnEditImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                showImageSourceDialog();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, SignupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        binding.btnSeeAll.setOnClickListener(v -> {
            startActivity(new Intent(this, RequestsManagementActivity.class));
        });

        binding.navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        binding.navLendings.setOnClickListener(v -> {
            startActivity(new Intent(this, MyLendingsActivity.class));
            finish();
        });
        binding.navRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, RequestsManagementActivity.class));
            finish();
        });
        binding.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddItemActivity.class));
        });
    }

    private void showEditProfileDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        EditText etName = view.findViewById(R.id.etEditName);
        EditText etHostel = view.findViewById(R.id.etEditHostel);
        
        etName.setText(binding.tvUserName.getText());
        etHostel.setText(binding.tvLocation.getText());

        new AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(view)
            .setPositiveButton("Save", (dialog, which) -> {
                String newName = etName.getText().toString().trim();
                String newHostel = etHostel.getText().toString().trim();
                if (!newName.isEmpty()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("fullName", newName);
                        updates.put("hostel", newHostel);
                        db.collection("users").document(user.getUid())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                binding.tvUserName.setText(newName);
                                binding.tvLocation.setText(newHostel);
                                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                            });
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
            .setTitle("Select Profile Image")
            .setItems(options, (dialog, which) -> {
                if (which == 0) launchCamera(); else pickImageLauncher.launch("image/*");
            })
            .show();
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePhotoLauncher.launch(photoUri);
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {
        private final List<DocumentSnapshot> activities;

        RecentActivityAdapter(List<DocumentSnapshot> activities) {
            this.activities = activities;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DocumentSnapshot doc = activities.get(position);
            String type = doc.getString("type");
            if (type == null) type = "ACTIVITY";
            String itemTitle = doc.getString("itemTitle");
            if (itemTitle == null) itemTitle = "Item";
            String otherUser = doc.getString("otherUserName");
            if (otherUser == null) otherUser = "someone";
            String status = doc.getString("status");
            if (status == null) status = "DONE";
            
            holder.tvTitle.setText("LENT".equals(type) ? "Lent \"" + itemTitle + "\"" : "Borrowed \"" + itemTitle + "\"");
            holder.tvSubtitle.setText("LENT".equals(type) ? "To " + otherUser : "From " + otherUser);
            holder.tvStatus.setText(status);
            
            String color = "ACTIVE".equals(status) ? "#10B981" : "#64748B";
            holder.tvStatus.setTextColor(Color.parseColor(color));
        }

        @Override
        public int getItemCount() {
            return activities.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView tvTitle;
            final TextView tvSubtitle;
            final TextView tvStatus;
            final ImageView ivImage;

            ViewHolder(View view) {
                super(view);
                tvTitle = view.findViewById(R.id.tvItemTitle);
                tvSubtitle = view.findViewById(R.id.tvOtherUserName);
                tvStatus = view.findViewById(R.id.tvStatusBadge);
                ivImage = view.findViewById(R.id.ivItemImage);
            }
        }
    }
}
