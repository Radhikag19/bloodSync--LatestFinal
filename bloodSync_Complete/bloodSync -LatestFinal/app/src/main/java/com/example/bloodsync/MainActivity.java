package com.example.bloodsync;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bloodsync.auth.LoginActivity;
import com.example.bloodsync.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainr);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not logged in, redirect to login
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Remove problematic reference to main_text which doesn't exist
        // Instead, we'll add the ID to the first TextView in the layout

        // Load user data from Firebase
        loadUserProfile(currentUser.getUid());

        // Set up bottom navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // Find the navigation items
        LinearLayout awareTab = findViewById(R.id.awareTab);
        LinearLayout homeTab = findViewById(R.id.homeTab);
        LinearLayout profileTab = findViewById(R.id.profileTab);

        // Set click listeners for navigation
        if (awareTab != null) {
            awareTab.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "Opening Aware Tab", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, AwareActivity.class);
                startActivity(intent);
            });
        }

        // Home tab is current screen - no need for action
        if (homeTab != null) {
            homeTab.setOnClickListener(v -> {
                // Already on home screen
                Toast.makeText(MainActivity.this, "Already on Home", Toast.LENGTH_SHORT).show();
            });
        }

        // Profile tab click listener
        if (profileTab != null) {
            profileTab.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "Opening Profile", Toast.LENGTH_SHORT).show();
                try {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        }
    }

    private void loadUserProfile(String userId) {
        mDatabase.child("users").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String name = dataSnapshot.child("name").getValue(String.class);
                            // Get the first TextView in the layout to show the welcome message
                            TextView welcomeText = findViewById(R.id.welcomeTextView);

                            if (welcomeText != null) {
                                welcomeText.setText("Hello! " + name);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this,
                                "Error loading profile: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Sign out user
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Redirect to login screen
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}