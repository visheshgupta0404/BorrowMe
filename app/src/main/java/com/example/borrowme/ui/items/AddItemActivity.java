package com.example.borrowme.ui.items;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.borrowme.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.materialswitch.MaterialSwitch;

public class AddItemActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnChooseFiles;
    private EditText etItemName;
    private TextView tvCategory;
    private TextView tvDaysValue;
    private Slider sliderDays;
    private MaterialSwitch switchHostelOnly;
    private MaterialButton btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnChooseFiles = findViewById(R.id.btnChooseFiles);
        etItemName = findViewById(R.id.etItemName);
        tvCategory = findViewById(R.id.tvCategory);
        tvDaysValue = findViewById(R.id.tvDaysValue);
        sliderDays = findViewById(R.id.sliderDays);
        switchHostelOnly = findViewById(R.id.switchHostelOnly);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnChooseFiles.setOnClickListener(v -> {
            // TODO: Implement image picker
            Toast.makeText(this, "Opening Gallery...", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnSelectCategory).setOnClickListener(v -> {
            // TODO: Show category selection dialog
            Toast.makeText(this, "Select Category", Toast.LENGTH_SHORT).show();
        });

        sliderDays.addOnChangeListener((slider, value, fromUser) -> {
            tvDaysValue.setText((int) value + " Days");
        });

        btnSubmit.setOnClickListener(v -> {
            if (validateInput()) {
                submitItem();
            }
        });
    }

    private boolean validateInput() {
        String itemName = etItemName.getText().toString().trim();
        if (itemName.isEmpty()) {
            etItemName.setError("Item name is required");
            return false;
        }
        
        if (tvCategory.getText().toString().equals("Select a category")) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void submitItem() {
        // TODO: Implement actual submission logic
        Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();
        
        // TODO: Navigate to Home Screen or My Lendings
        // Intent intent = new Intent(this, HomeActivity.class);
        // startActivity(intent);
        finish();
    }
}
