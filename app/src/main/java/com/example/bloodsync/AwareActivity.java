package com.example.bloodsync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class AwareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aware);

        // Set up back button functionality
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> finish());

        // Set up home icon functionality
        LinearLayout homeTab = findViewById(R.id.homeTab);
        if (homeTab != null) {
            homeTab.setOnClickListener(v -> {
                Intent intent = new Intent(AwareActivity.this, MainActivityC.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }
}