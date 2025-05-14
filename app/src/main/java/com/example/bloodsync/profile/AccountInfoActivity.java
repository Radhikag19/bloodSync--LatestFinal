package com.example.bloodsync.profile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodsync.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountInfoActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, phoneTextView, bloodGroupTextView;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        // Initialize views
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        bloodGroupTextView = findViewById(R.id.bloodGroupTextView);
        backButton = findViewById(R.id.backButton);

        // Set back button listener
        backButton.setOnClickListener(v -> onBackPressed());

        // Load user account info
        loadAccountInfo();
    }

    private void loadAccountInfo() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Set email from Auth
            String email = user.getEmail();
            emailTextView.setText(email != null ? email : "Not available");

            // Get other details from Database
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String phone = dataSnapshot.child("phone").getValue(String.class);
                        String bloodGroup = dataSnapshot.child("bloodGroup").getValue(String.class);

                        nameTextView.setText(name != null ? name : "Not available");
                        phoneTextView.setText(phone != null ? phone : "Not available");
                        bloodGroupTextView.setText(bloodGroup != null ? bloodGroup : "Not available");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(AccountInfoActivity.this,
                            "Failed to load account info: " + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // User not logged in
            finish();
        }
    }
}