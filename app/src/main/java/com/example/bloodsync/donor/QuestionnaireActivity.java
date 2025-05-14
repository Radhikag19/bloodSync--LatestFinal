package com.example.bloodsync.donor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bloodsync.MainActivity;
import com.example.bloodsync.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class QuestionnaireActivity extends AppCompatActivity {

    private RadioButton diabetesYes, diabetesNo;
    private RadioButton heartLungsYes, heartLungsNo;
    private RadioButton hivAidsYes, hivAidsNo;
    private RadioButton cancerYes, cancerNo;
    private Button checkEligibilityButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questionnare);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI elements
        diabetesYes = findViewById(R.id.diabetesYes);
        diabetesNo = findViewById(R.id.diabetesNo);
        heartLungsYes = findViewById(R.id.heartLungsYes);
        heartLungsNo = findViewById(R.id.heartLungsNo);
        hivAidsYes = findViewById(R.id.hivAidsYes);
        hivAidsNo = findViewById(R.id.hivAidsNo);
        cancerYes = findViewById(R.id.cancerYes);
        cancerNo = findViewById(R.id.cancerNo);
        checkEligibilityButton = findViewById(R.id.checkEligibilityButton);

        // Set default selections
        diabetesNo.setChecked(true);
        heartLungsNo.setChecked(true);
        hivAidsNo.setChecked(true);
        cancerNo.setChecked(true);

        checkEligibilityButton.setOnClickListener(v -> saveQuestionnaire());
    }

    private void saveQuestionnaire() {
        String userId = mAuth.getCurrentUser().getUid();

        // Get questionnaire answers
        boolean hasDiabetes = diabetesYes.isChecked();
        boolean hasHeartLungProblems = heartLungsYes.isChecked();
        boolean hasHivAids = hivAidsYes.isChecked();
        boolean hasCancer = cancerYes.isChecked();

        // Store questionnaire results in Firebase Database
        Map<String, Object> questionnaireData = new HashMap<>();
        questionnaireData.put("hasDiabetes", hasDiabetes);
        questionnaireData.put("hasHeartLungProblems", hasHeartLungProblems);
        questionnaireData.put("hasHivAids", hasHivAids);
        questionnaireData.put("hasCancer", hasCancer);

        mDatabase.child("users").child(userId).child("questionnaire")
                .setValue(questionnaireData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Calculate eligibility
                        boolean isEligible = !hasDiabetes && !hasHeartLungProblems &&
                                !hasHivAids && !hasCancer;

                        // Update eligibility status
                        mDatabase.child("users").child(userId).child("isEligibleDonor")
                                .setValue(isEligible)
                                .addOnCompleteListener(eligibilityTask -> {
                                    if (eligibilityTask.isSuccessful()) {
                                        String message = isEligible ?
                                                "You are eligible to donate blood!" :
                                                "Sorry, you are not eligible to donate blood based on your health information.";

                                        Toast.makeText(QuestionnaireActivity.this,
                                                message, Toast.LENGTH_LONG).show();

                                        // Navigate to MainActivity regardless of eligibility
                                        Intent intent = new Intent(QuestionnaireActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    } else {
                        Toast.makeText(QuestionnaireActivity.this,
                                "Failed to save questionnaire: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}