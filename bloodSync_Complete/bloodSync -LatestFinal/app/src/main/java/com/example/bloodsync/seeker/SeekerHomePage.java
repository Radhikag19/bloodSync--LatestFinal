package com.example.bloodsync.seeker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bloodsync.R;

public class SeekerHomePage extends AppCompatActivity {

    public TextView welcomeTextView1;
    private TextView profileTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seeker_activity);

        // Initialize TextViews
        welcomeTextView1 = findViewById(R.id.welcomeTextView);
        profileTextView = findViewById(R.id.profileTextView);

         profileTextView.setText("Welcome to your seeker profile!");
    }
}