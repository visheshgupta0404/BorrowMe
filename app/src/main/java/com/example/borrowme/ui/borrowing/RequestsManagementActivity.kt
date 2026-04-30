package com.example.borrowme.ui.borrowing

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.borrowme.R
import com.example.borrowme.ui.auth.LoginActivity
import com.example.borrowme.ui.dashboard.FeedActivity
import com.example.borrowme.ui.dashboard.HomeActivity
import com.example.borrowme.ui.profile.MyLendingsActivity
import com.example.borrowme.ui.profile.ProfileActivity
import com.example.borrowme.ui.items.AddItemActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestsManagementActivity : AppCompatActivity() {

    private var tabLayout: TabLayout? = null
    private var rvRequests: RecyclerView? = null

    private val db = FirebaseFirestore.getInstance()
    private var allRequests = mutableListOf<DocumentSnapshot>()
    private var filteredRequests = mutableListOf<DocumentSnapshot>()
    private lateinit var adapter: RequestsAdapter
    private var currentTab = "PENDING"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_requests_management)

            initViews()
            setupListeners()
            setupRecyclerView()
            seedMockRequests()
        } catch (e: Exception) {
            Log.e("RequestsActivity", "Error in onCreate", e)
        }
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            loadRequests()
        }
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.tabLayout)
        rvRequests = findViewById(R.id.rvRequests)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val text = tab?.text?.toString()?.uppercase() ?: "PENDING"
                currentTab = if (text == "RETURN PENDING") "RETURN_PENDING" else text
                filterRequests()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        findViewById<View>(R.id.navHome)?.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.navLendings)?.setOnClickListener {
            startActivity(Intent(this, MyLendingsActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.navRequests)?.setOnClickListener {
            loadRequests()
        }
        findViewById<View>(R.id.navProfile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.fabAdd)?.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        rvRequests?.layoutManager = LinearLayoutManager(this)
        adapter = RequestsAdapter(filteredRequests)
        rvRequests?.adapter = adapter
    }

    private fun loadRequests() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        
        db.collection("requests").whereEqualTo("borrowerId", user.uid)
            .get()
            .addOnSuccessListener { borrowerSnap ->
                val tempRequests = borrowerSnap.documents.toMutableList()
                db.collection("requests").whereEqualTo("lenderId", user.uid)
                    .get()
                    .addOnSuccessListener { lenderSnap ->
                        tempRequests.addAll(lenderSnap.documents)
                        allRequests = tempRequests.distinctBy { it.id }.toMutableList()
                        filterRequests()
                    }
            }
            .addOnFailureListener { Log.e("RequestsActivity", "Load error", it) }
    }

    private fun filterRequests() {
        filteredRequests.clear()
        filteredRequests.addAll(allRequests.filter { it.getString("status") == currentTab })
        filteredRequests.sortByDescending { it.getDate("timestamp") ?: it.getDate("requestedAt") ?: Date(0) }
        adapter.notifyDataSetChanged()
    }

    private fun updateRequestStatus(requestId: String, newStatus: String, borrowerId: String?, lenderId: String?) {
        db.collection("requests").document(requestId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Request $newStatus", Toast.LENGTH_SHORT).show()
                if (newStatus == "APPROVED") {
                    if (borrowerId != null) db.collection("users").document(borrowerId).update("activeBorrowCount", FieldValue.increment(1))
                    if (lenderId != null) db.collection("users").document(lenderId).update("activeLendCount", FieldValue.increment(1))
                }
                loadRequests()
            }
    }

    private fun seedMockRequests() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        db.collection("requests").limit(1).get().addOnSuccessListener { 
            if (it.isEmpty) {
                val mock1 = hashMapOf(
                    "itemId" to "showcase_0",
                    "borrowerId" to "dummy_id",
                    "lenderId" to user.uid,
                    "status" to "PENDING",
                    "timestamp" to FieldValue.serverTimestamp()
                )
                db.collection("requests").add(mock1)
            }
        }
    }

    inner class RequestsAdapter(private val items: List<DocumentSnapshot>) :
        RecyclerView.Adapter<RequestsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvItemName: TextView? = view.findViewById(R.id.tvItemName)
            val tvUserInfo: TextView? = view.findViewById(R.id.tvUserInfo)
            val ivItemImage: ImageView? = view.findViewById(R.id.ivItemImage)
            val btnApprove: Button? = view.findViewById(R.id.btnApprove)
            val btnReject: Button? = view.findViewById(R.id.btnReject)
            val tvStatus: TextView? = view.findViewById(R.id.tvStatus)
            val statusIndicator: View? = view.findViewById(R.id.statusIndicator)
            val tvRequestDate: TextView? = view.findViewById(R.id.tvRequestDate)
            val ivUserSmall: ImageView? = view.findViewById(R.id.ivUserSmall)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val req = items[position]
            val itemId = req.getString("itemId")
            val borrowerId = req.getString("borrowerId")
            val lenderId = req.getString("lenderId")
            val status = req.getString("status") ?: "PENDING"
            val timestamp = req.getDate("timestamp") ?: req.getDate("requestedAt")

            if (timestamp != null && holder.tvRequestDate != null) {
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                holder.tvRequestDate.text = "Requested on: ${sdf.format(timestamp)}"
            }

            if (itemId != null) {
                db.collection("items").document(itemId).get().addOnSuccessListener { 
                    if (it.exists()) {
                        holder.tvItemName?.text = it.getString("title")
                        val url = it.getString("imageUrl")
                        if (!url.isNullOrEmpty() && holder.ivItemImage != null) {
                            Glide.with(holder.itemView.context).load(url).placeholder(R.drawable.ic_launcher_foreground).into(holder.ivItemImage)
                        }
                    }
                }
            }

            val otherId = if (FirebaseAuth.getInstance().currentUser?.uid == lenderId) borrowerId else lenderId
            if (otherId != null) {
                db.collection("users").document(otherId).get().addOnSuccessListener { 
                    if (it.exists()) {
                        val name = it.getString("fullName") ?: "User"
                        val hostel = it.getString("hostel") ?: ""
                        val profileImage = it.getString("profileImage")
                        holder.tvUserInfo?.text = if (hostel.isNotEmpty()) "$name • $hostel" else name
                        
                        if (!profileImage.isNullOrEmpty() && holder.ivUserSmall != null) {
                            Glide.with(holder.itemView.context)
                                .load(profileImage)
                                .circleCrop()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(holder.ivUserSmall)
                        }
                    }
                }
            }

            holder.tvStatus?.text = status.replace("_", " ")
            holder.btnApprove?.visibility = View.GONE
            holder.btnReject?.visibility = View.GONE

            when (status) {
                "PENDING" -> {
                    holder.tvStatus?.setTextColor(Color.parseColor("#F59E0B"))
                    holder.tvStatus?.setBackgroundResource(R.drawable.bg_badge_subtle)
                    holder.statusIndicator?.setBackgroundColor(Color.parseColor("#F59E0B"))
                    if (FirebaseAuth.getInstance().currentUser?.uid == lenderId) {
                        holder.btnApprove?.visibility = View.VISIBLE
                        holder.btnReject?.visibility = View.VISIBLE
                        holder.btnApprove?.setOnClickListener { updateRequestStatus(req.id, "APPROVED", borrowerId, lenderId) }
                        holder.btnReject?.setOnClickListener { updateRequestStatus(req.id, "REJECTED", null, null) }
                    }
                }
                "APPROVED" -> {
                    holder.tvStatus?.setTextColor(Color.parseColor("#10B981"))
                    holder.tvStatus?.setBackgroundResource(R.drawable.bg_badge_subtle)
                    holder.statusIndicator?.setBackgroundColor(Color.parseColor("#10B981"))
                }
                "REJECTED" -> {
                    holder.tvStatus?.setTextColor(Color.parseColor("#EF4444"))
                    holder.tvStatus?.setBackgroundResource(R.drawable.bg_badge_subtle)
                    holder.statusIndicator?.setBackgroundColor(Color.parseColor("#EF4444"))
                }
                "RETURN_PENDING" -> {
                    holder.tvStatus?.setTextColor(Color.parseColor("#2563EB"))
                    holder.tvStatus?.setBackgroundResource(R.drawable.bg_badge_subtle)
                    holder.statusIndicator?.setBackgroundColor(Color.parseColor("#2563EB"))
                }
            }
        }

        override fun getItemCount() = items.size
    }
}
