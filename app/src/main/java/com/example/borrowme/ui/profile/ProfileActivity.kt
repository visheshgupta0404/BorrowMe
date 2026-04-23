package com.example.borrowme.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.borrowme.databinding.ActivityProfileBinding
// TODO: Import other activities when they are available
// import com.example.borrowme.ui.auth.LoginActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSettings.setOnClickListener {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnEditImage.setOnClickListener {
            Toast.makeText(this, "Edit profile image", Toast.LENGTH_SHORT).show()
        }

        binding.btnEditProfile.setOnClickListener {
            // TODO: Navigate to Edit Profile Screen
            Toast.makeText(this, "Navigate to Edit Profile", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            // TODO: Perform logout logic and navigate to Login Screen
            // val intent = Intent(this, LoginActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // startActivity(intent)
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnSeeAll.setOnClickListener {
            // TODO: Navigate to All Activity Screen
            Toast.makeText(this, "See All Activity", Toast.LENGTH_SHORT).show()
        }
    }
}
