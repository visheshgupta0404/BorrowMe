package com.example.borrowme.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.borrowme.databinding.ActivityMyLendingsBinding
// TODO: Import ItemDetailActivity when available
// import com.example.borrowme.ui.items.ItemDetailActivity

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
        // Set selected item in bottom navigation
        binding.bottomNavigation.selectedItemId = com.example.borrowme.R.id.nav_lendings
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnEditListing1.setOnClickListener {
            // TODO: Navigate to Edit Item Screen
            Toast.makeText(this, "Edit Calculus Textbook", Toast.LENGTH_SHORT).show()
        }

        binding.btnEditListing2.setOnClickListener {
            // TODO: Navigate to Edit Item Screen
            Toast.makeText(this, "Edit Sony WH-1000XM6", Toast.LENGTH_SHORT).show()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.borrowme.R.id.nav_feed -> {
                    // TODO: Navigate to Feed/Home Screen
                    true
                }
                com.example.borrowme.R.id.nav_lendings -> true
                com.example.borrowme.R.id.nav_requests -> {
                    // TODO: Navigate to Requests Screen
                    true
                }
                com.example.borrowme.R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
