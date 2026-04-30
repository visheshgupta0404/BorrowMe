package com.example.borrowme.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.borrowme.databinding.ActivityProfileBinding
import com.example.borrowme.ui.auth.SignupActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.borrowme.ui.borrowing.RequestsManagementActivity
import com.google.firebase.storage.FirebaseStorage
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var photoUri: Uri? = null
    private var currentPhotoPath: String? = null

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
                photoUri?.let { uploadImageToFirebase(it) }
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                photoUri = it
                uploadImageToFirebase(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("fullName")
                        val hostel = document.getString("hostel")
                        
                        binding.tvUserName.text = fullName ?: "No Name"
                        binding.tvLocation.text = hostel ?: "No Hostel"
                        
                        val profileImageUrl = document.getString("profileImageUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .placeholder(com.example.borrowme.R.drawable.ic_person)
                                .into(binding.ivProfile)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")
        
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()
        
        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveProfileImageUrlToFirestore(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfileImageUrlToFirestore(url: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        
        db.collection("users").document(user.uid)
            .update("profileImageUrl", url)
            .addOnSuccessListener {
                Glide.with(this).load(url).into(binding.ivProfile)
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to sync profile image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSettings.setOnClickListener {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnEditImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                showImageSourceDialog()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Navigate to Edit Profile", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SignupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnSeeAll.setOnClickListener {
            val intent = Intent(this, RequestsManagementActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Select Profile Image")
            .setItems(options) { _, which ->
                if (which == 0) {
                    launchCamera()
                } else {
                    pickImageLauncher.launch("image/*")
                }
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
}
