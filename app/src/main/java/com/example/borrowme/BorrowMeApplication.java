package com.example.borrowme;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class BorrowMeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
