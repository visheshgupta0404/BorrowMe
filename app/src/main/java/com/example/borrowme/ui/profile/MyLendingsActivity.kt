package com.example.borrowme.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.borrowme.R
import com.example.borrowme.databinding.ActivityMyLendingsBinding
import com.example.borrowme.ui.auth.LoginActivity
import com.example.borrowme.ui.borrowing.RequestsManagementActivity
import com.example.borrowme.ui.dashboard.HomeActivity
import com.example.borrowme.ui.items.AddItemActivity
import com.example.borrowme.ui.items.ItemDetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class MyLendingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyLendingsBinding
    private val db = FirebaseFirestore.getInstance()
    private val lendingsList = mutableListOf<DocumentSnapshot>()
    private lateinit var adapter: MyLendingsGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMyLendingsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupUI()
            setupClickListeners()
        } catch (e: Exception) {
            Log.e("MyLendings", "Error in onCreate", e)
        }
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            loadMyLendings()
        }
    }

    private fun setupUI() {
        // Changed to GridLayoutManager to match Borrow Dashboard as requested
        binding.rvMyLendings.layoutManager = GridLayoutManager(this, 2)
        adapter = MyLendingsGridAdapter(lendingsList)
        binding.rvMyLendings.adapter = adapter
    }

    private fun loadMyLendings() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        // Removed orderBy to prevent crash due to missing composite index in Firestore
        db.collection("items")
            .whereEqualTo("ownerId", user.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                lendingsList.clear()
                lendingsList.addAll(querySnapshot.documents)
                adapter.notifyDataSetChanged()
                
                if (lendingsList.isEmpty()) {
                    Toast.makeText(this, "You haven't listed any items yet", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("MyLendings", "Error loading items", e)
                Toast.makeText(this, "Failed to load lendings. Check internet.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // Uniform Bottom Nav implementation
        binding.navHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        
        binding.navLendings.setOnClickListener {
            // Already here, maybe scroll to top
        }
        
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }
        
        binding.navRequests.setOnClickListener {
            startActivity(Intent(this, RequestsManagementActivity::class.java))
            finish()
        }
        
        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

    inner class MyLendingsGridAdapter(private val items: List<DocumentSnapshot>) :
        RecyclerView.Adapter<MyLendingsGridAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivItemImage: ImageView = view.findViewById(R.id.ivItemImage)
            val tvCategory: TextView = view.findViewById(R.id.tvCategory)
            val tvItemName: TextView = view.findViewById(R.id.tvItemName)
            val tvStatusBadge: TextView = view.findViewById(R.id.tvStatusBadge)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // Using item_feed_card to keep it consistent with the borrow page
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_feed_card, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val doc = items[position]
            val title = doc.getString("title") ?: "Unknown Item"
            val category = doc.getString("category") ?: "Category"
            val status = doc.getString("status") ?: "AVAILABLE"
            val imageUrl = doc.getString("imageUrl")

            holder.tvItemName.text = title
            holder.tvCategory.text = category.uppercase()
            holder.tvStatusBadge.text = status
            
            // Adjust badge color based on status
            if (status == "AVAILABLE") {
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green)
            } else {
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange)
            }

            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.ivItemImage)
            } else {
                holder.ivItemImage.setImageResource(R.drawable.ic_launcher_foreground)
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(this@MyLendingsActivity, ItemDetailActivity::class.java)
                intent.putExtra("itemId", doc.id)
                startActivity(intent)
            }
        }

        override fun getItemCount() = items.size
    }
}
