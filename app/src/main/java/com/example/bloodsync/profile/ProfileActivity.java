package com.example.bloodsync.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodsync.AwareActivity;
import com.example.bloodsync.MainActivity;
import com.example.bloodsync.R;
import com.example.bloodsync.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName, phoneNumber;
    private LinearLayout accountInfoItem, historyItem, logoutItem;
    private LinearLayout awareTab, homeTab, profileTab;
    private ImageView backButton;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // If no user is logged in, redirect to LoginActivity
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize Database reference
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Initialize views
        initializeViews();

        // Load user data
        loadUserData();

        // Set click listeners
        setClickListeners();
    }

    private void initializeViews() {
        profileName = findViewById(R.id.profileName);
        phoneNumber = findViewById(R.id.phoneNumber);
        accountInfoItem = findViewById(R.id.accountInfoItem);
        historyItem = findViewById(R.id.historyItem);
        logoutItem = findViewById(R.id.logoutItem);
        awareTab = findViewById(R.id.awareTab);
        homeTab = findViewById(R.id.homeTab);
        profileTab = findViewById(R.id.profileTab);
        backButton = findViewById(R.id.backButton);
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);

                    if (name != null) {
                        profileName.setText(name);
                    }

                    if (phone != null) {
                        phoneNumber.setText("+91 " + phone);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this,
                        "Failed to load user data: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setClickListeners() {
        // Bottom navigation tabs
        awareTab.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AwareActivity.class);
            startActivity(intent);
            finish();
        });

        homeTab.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Profile tab is current activity, no action needed
        profileTab.setOnClickListener(v -> {
            // Already in profile, do nothing or refresh
            Toast.makeText(ProfileActivity.this, "Already in Profile", Toast.LENGTH_SHORT).show();
        });

        // Profile menu items
        accountInfoItem.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AccountInfoActivity.class);
            startActivity(intent);
        });

        historyItem.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "History feature coming soon", Toast.LENGTH_SHORT).show();
        });

        logoutItem.setOnClickListener(v -> showLogoutConfirmationDialog());

        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}