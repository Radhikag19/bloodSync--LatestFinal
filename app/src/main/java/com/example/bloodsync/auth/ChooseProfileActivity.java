package com.example.bloodsync.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bloodsync.MainActivity;
import com.example.bloodsync.MainActivityC;
import com.example.bloodsync.R;
import com.example.bloodsync.donor.QuestionnaireActivity;
import com.example.bloodsync.seeker.SeekerHomePage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class ChooseProfileActivity extends AppCompatActivity {

    public String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_profile_activity);

        // Check if user is a new user from registration flow
        boolean isNewUser = getIntent().getBooleanExtra("newUser", false);

        // Only redirect existing users to MainActivity
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && !isNewUser) {
            startActivity(new Intent(ChooseProfileActivity.this, MainActivity.class));
            finish();
            return;
        }

        Button donorButton = findViewById(R.id.donorButton);
        Button seekerButton = findViewById(R.id.seekerButton);

        donorButton.setOnClickListener(v -> {
            // Update user type in database
            if (currentUser != null) {
                FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(currentUser.getUid())
                        .child("userType")
                        .setValue("donor");
            }

            Intent intent = new Intent(ChooseProfileActivity.this, QuestionnaireActivity.class);
            intent.putExtra("userType", "donor");
            startActivity(intent);
        });

        seekerButton.setOnClickListener(v -> {
            // Update user type in database
            if (currentUser != null) {
                FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(currentUser.getUid())
                        .child("userType")
                        .setValue("seeker");
            }

            Intent intent = new Intent(ChooseProfileActivity.this, MainActivityC.class);
            intent.putExtra("userType", "seeker");
            startActivity(intent);
        });
    }
}