package com.example.borrowme.ui.profile

import android.Manifest
import android.util.Log
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.borrowme.R
import com.example.borrowme.databinding.ActivityProfileBinding
import com.example.borrowme.ui.auth.LoginActivity
import com.example.borrowme.ui.auth.SignupActivity
import com.example.borrowme.ui.borrowing.RequestsManagementActivity
import com.example.borrowme.ui.dashboard.HomeActivity
import com.example.borrowme.ui.items.AddItemActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var photoUri: Uri? = null
    private var currentPhotoPath: String? = null
    private val db = FirebaseFirestore.getInstance()
    private val activityList = mutableListOf<DocumentSnapshot>()
    private lateinit var activityAdapter: RecentActivityAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showImageSourceDialog()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoUri?.let { uploadImageToImgBB(it) }
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                photoUri = it
                uploadImageToImgBB(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadUserData()
        loadRecentActivity()
        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.rvRecentActivity.layoutManager = LinearLayoutManager(this)
        activityAdapter = RecentActivityAdapter(activityList)
        binding.rvRecentActivity.adapter = activityAdapter
    }

    private fun loadUserData() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val fullName = document.getString("fullName")
                    val hostel = document.getString("hostel")
                    val repScore = document.getLong("reputationScore") ?: 100L
                    val borrowCount = document.getLong("activeBorrowCount") ?: 0L
                    
                    binding.tvUserName.text = fullName ?: "User"
                    binding.tvLocation.text = hostel ?: "Not Selected"
                    binding.tvReputationScore.text = String.format(Locale.getDefault(), "%.1f", repScore / 20.0)
                    binding.tvActiveBorrowsCount.text = borrowCount.toString()
                    
                    val profileImageUrl = document.getString("profileImage")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(binding.ivProfile)
                        }
                }
            }
    }

    private fun loadRecentActivity() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        db.collection("users").document(user.uid).collection("activity")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { snapshots ->
                activityList.clear()
                activityList.addAll(snapshots.documents)
                activityAdapter.notifyDataSetChanged()
                
                if (activityList.isEmpty()) {
                    seedMockActivity()
                }
            }
    }

    private fun seedMockActivity() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val mock = hashMapOf(
            "type" to "LENT",
            "itemTitle" to "Calculus Textbook",
            "otherUserName" to "Sarah M.",
            "timestamp" to Date(),
            "status" to "ACTIVE"
        )
        db.collection("users").document(user.uid).collection("activity").add(mock)
            .addOnSuccessListener { loadRecentActivity() }
    }

    private fun uploadImageToImgBB(uri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()
        
        com.example.borrowme.utils.ImgBBUploader.uploadImage(this, uri, object : com.example.borrowme.utils.ImgBBUploader.UploadCallback {
            override fun onSuccess(url: String) {
                runOnUiThread {
                    saveProfileImageUrlToFirestore(url)
                }
            }

            override fun onFailure(error: String) {
                runOnUiThread {
                    Log.e("ProfileActivity", "ImgBB Upload failed: $error")
                    Toast.makeText(this@ProfileActivity, "Upload failed: $error", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun saveProfileImageUrlToFirestore(url: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        db.collection("users").document(user.uid)
            .update("profileImage", url)
            .addOnSuccessListener {
                Glide.with(this).load(url).placeholder(R.drawable.ic_person).error(R.drawable.ic_person).into(binding.ivProfile)
                Toast.makeText(this, "Profile image updated!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnEditImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                showImageSourceDialog()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnEditProfile.setOnClickListener { showEditProfileDialog() }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SignupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.btnSeeAll.setOnClickListener {
            startActivity(Intent(this, RequestsManagementActivity::class.java))
        }

        binding.navHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        binding.navLendings.setOnClickListener {
            startActivity(Intent(this, MyLendingsActivity::class.java))
            finish()
        }
        binding.navRequests.setOnClickListener {
            startActivity(Intent(this, RequestsManagementActivity::class.java))
            finish()
        }
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }
    }

    private fun showEditProfileDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = view.findViewById<android.widget.EditText>(R.id.etEditName)
        val etHostel = view.findViewById<android.widget.EditText>(R.id.etEditHostel)
        
        etName.setText(binding.tvUserName.text)
        etHostel.setText(binding.tvLocation.text)

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val newName = etName.text.toString().trim()
                val newHostel = etHostel.text.toString().trim()
                if (newName.isNotEmpty()) {
                    db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .update(mapOf("fullName" to newName, "hostel" to newHostel))
                        .addOnSuccessListener {
                            binding.tvUserName.text = newName
                            binding.tvLocation.text = newHostel
                            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Select Profile Image")
            .setItems(options) { _, which ->
                if (which == 0) launchCamera() else pickImageLauncher.launch("image/*")
            }
            .show()
    }

    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
            takePhotoLauncher.launch(photoUri)
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    inner class RecentActivityAdapter(private val activities: List<DocumentSnapshot>) :
        RecyclerView.Adapter<RecentActivityAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.tvOtherUserName)
            val tvStatus: TextView = view.findViewById(R.id.tvStatusBadge)
            val ivImage: ImageView = view.findViewById(R.id.ivItemImage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_active_request, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val doc = activities[position]
            val type = doc.getString("type") ?: "ACTIVITY"
            val itemTitle = doc.getString("itemTitle") ?: "Item"
            val otherUser = doc.getString("otherUserName") ?: "someone"
            val status = doc.getString("status") ?: "DONE"
            
            holder.tvTitle.text = if (type == "LENT") "Lent \"$itemTitle\"" else "Borrowed \"$itemTitle\""
            holder.tvSubtitle.text = if (type == "LENT") "To $otherUser" else "From $otherUser"
            holder.tvStatus.text = status
            
            val color = if (status == "ACTIVE") "#10B981" else "#64748B"
            holder.tvStatus.setTextColor(Color.parseColor(color))
        }

        override fun getItemCount() = activities.size
    }
}
