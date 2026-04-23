package com.example.borrowme.ui.borrowing

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.borrowme.R

class BorrowRequestActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnSubmitRequest: Button
    private lateinit var etMessage: EditText
    private lateinit var tvSelectedDuration: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_borrow_request)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest)
        etMessage = findViewById(R.id.etMessage)
        tvSelectedDuration = findViewById(R.id.tvSelectedDuration)
        
        // Initial placeholder text for duration
        tvSelectedDuration.text = "Select a start and end date"
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnSubmitRequest.setOnClickListener {
            if (validateInput()) {
                submitRequest()
            }
        }
    }

    private fun validateInput(): Boolean {
        // Basic check if a duration has been selected (placeholder logic)
        if (tvSelectedDuration.text == "Select a start and end date") {
            Toast.makeText(this, "Please select a lending duration", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun submitRequest() {
        val message = etMessage.text.toString().trim()
        
        // TODO: Implement actual submission logic (API call, etc.)
        Toast.makeText(this, "Request submitted successfully!", Toast.LENGTH_SHORT).show()
        
        // TODO: Navigate to Requests Management or Home Screen
        // val intent = Intent(this, RequestsManagementActivity::class.java)
        // startActivity(intent)
        finish()
    }
}
