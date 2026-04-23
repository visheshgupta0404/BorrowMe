package com.example.borrowme.ui.borrowing

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.borrowme.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout

class RequestsManagementActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var rvRequests: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests_management)

        initViews()
        setupListeners()
        setupRecyclerView()
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.tabLayout)
        rvRequests = findViewById(R.id.rvRequests)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        
        // Select 'Requests' tab in bottom navigation
        bottomNavigation.selectedItemId = R.id.nav_requests
    }

    private fun setupListeners() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val category = tab?.text ?: "Pending"
                Toast.makeText(this@RequestsManagementActivity, "Showing $category requests", Toast.LENGTH_SHORT).show()
                // TODO: Filter list based on category
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_feed -> {
                    // TODO: Navigate to Feed Screen
                    true
                }
                R.id.nav_lendings -> {
                    // TODO: Navigate to Lendings Screen
                    true
                }
                R.id.nav_requests -> true
                R.id.nav_profile -> {
                    // TODO: Navigate to Profile Screen
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        rvRequests.layoutManager = LinearLayoutManager(this)
        // TODO: Set up Adapter with mock data
    }
}
