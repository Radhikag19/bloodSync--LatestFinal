package com.example.bloodsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bloodsync.auth.ChooseProfileActivity;
import com.example.bloodsync.auth.LoginActivity;
import com.example.bloodsync.onboarding.OnboardingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private static final String PREF_ONBOARDING_COMPLETE = "onboarding_complete";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
            boolean onboardingComplete = preferences.getBoolean(PREF_ONBOARDING_COMPLETE, false);

            Intent intent;
            if (onboardingComplete) {
                // Check if user is logged in
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    // User is already logged in, go to MainActivity
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    // User is not logged in, go to login
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
            } else {
                // First time user, show onboarding
                intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}