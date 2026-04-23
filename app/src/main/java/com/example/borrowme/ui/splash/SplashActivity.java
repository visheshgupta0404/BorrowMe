package com.example.borrowme.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.borrowme.R;
import com.example.borrowme.ui.auth.SignupActivity;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class SplashActivity extends AppCompatActivity {

    private LinearProgressIndicator progressBar;
    private TextView tvProgress;
    private int progressStatus = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);

        startLoading();
    }

    private void startLoading() {
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 5;
                handler.post(() -> {
                    progressBar.setProgress(progressStatus);
                    tvProgress.setText(progressStatus + "%");
                });
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            handler.post(() -> {
                startActivity(new Intent(SplashActivity.this, SignupActivity.class));
                finish();
            });
        }).start();
    }
}
