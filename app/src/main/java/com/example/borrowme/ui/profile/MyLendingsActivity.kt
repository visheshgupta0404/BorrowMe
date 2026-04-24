package com.example.borrowme.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.borrowme.databinding.ActivityMyLendingsBinding
import com.example.borrowme.ui.dashboard.FeedActivity
import com.example.borrowme.ui.borrowing.RequestsManagementActivity

class MyLendingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyLendingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyLendingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        binding.bottomNavigation.selectedItemId = com.example.borrowme.R.id.nav_lendings
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnEditListing1.setOnClickListener {
            Toast.makeText(this, "Edit Calculus Textbook", Toast.LENGTH_SHORT).show()
        }

        binding.btnEditListing2.setOnClickListener {
            Toast.makeText(this, "Edit Sony WH-1000XM6", Toast.LENGTH_SHORT).show()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.borrowme.R.id.nav_feed -> {
                    startActivity(Intent(this, FeedActivity::class.java))
                    finish()
                    true
                }
                com.example.borrowme.R.id.nav_lendings -> true
                com.example.borrowme.R.id.nav_requests -> {
                    startActivity(Intent(this, RequestsManagementActivity::class.java))
                    finish()
                    true
                }
                com.example.borrowme.R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
