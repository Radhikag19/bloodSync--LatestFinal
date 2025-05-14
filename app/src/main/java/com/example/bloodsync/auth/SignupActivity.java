package com.example.bloodsync.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bloodsync.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText emailEdit, passwordEdit;
    private Button signupButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI elements
        emailEdit = findViewById(R.id.emailEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        signupButton = findViewById(R.id.signupButton);

        // Set click listener for signup button
        signupButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        // Validate email and password
        if (email.isEmpty()) {
            emailEdit.setError("Email is required");
            emailEdit.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEdit.setError("Password is required");
            passwordEdit.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEdit.setError("Password must be at least 6 characters");
            passwordEdit.requestFocus();
            return;
        }

        // Add validation for other fields
        String firstName = ((EditText)findViewById(R.id.firstNameEdit)).getText().toString().trim();
        String lastName = ((EditText)findViewById(R.id.lastNameEdit)).getText().toString().trim();
        String mobile = ((EditText)findViewById(R.id.mobileEdit)).getText().toString().trim();

        if (firstName.isEmpty()) {
            ((EditText)findViewById(R.id.firstNameEdit)).setError("First name is required");
            ((EditText)findViewById(R.id.firstNameEdit)).requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            ((EditText)findViewById(R.id.lastNameEdit)).setError("Last name is required");
            ((EditText)findViewById(R.id.lastNameEdit)).requestFocus();
            return;
        }

        if (mobile.isEmpty()) {
            ((EditText)findViewById(R.id.mobileEdit)).setError("Mobile number is required");
            ((EditText)findViewById(R.id.mobileEdit)).requestFocus();
            return;
        }

        // Show progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing up...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Save user data to database
                        saveUserToDatabase(mAuth.getCurrentUser().getUid(), email);
                    } else {
                        // If sign up fails, display a message to the user
                        progressDialog.dismiss();
                        Toast.makeText(SignupActivity.this, "Registration failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, String email) {
        // Get all user input fields
        String firstName = ((EditText)findViewById(R.id.firstNameEdit)).getText().toString().trim();
        String lastName = ((EditText)findViewById(R.id.lastNameEdit)).getText().toString().trim();
        String mobile = ((EditText)findViewById(R.id.mobileEdit)).getText().toString().trim();
        String location = ((EditText)findViewById(R.id.locationEdit)).getText().toString().trim();
        String age = ((EditText)findViewById(R.id.editTextAge)).getText().toString().trim();
        String gender = ((Spinner)findViewById(R.id.genderSpinner)).getSelectedItem().toString();
        String bloodGroup = ((Spinner)findViewById(R.id.bloodGroupSpinner)).getSelectedItem().toString();

        // Create complete user profile
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("name", firstName + " " + lastName);
        userData.put("mobile", mobile);
        userData.put("location", location);
        userData.put("age", age);
        userData.put("gender", gender);
        userData.put("bloodGroup", bloodGroup);
        userData.put("createdAt", ServerValue.TIMESTAMP);
        // We'll set userType later in ChooseProfileActivity

        mDatabase.child("users").child(userId).setValue(userData)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(SignupActivity.this, "Registration successful!",
                                Toast.LENGTH_SHORT).show();

                        // Pass the newUser flag to ChooseProfileActivity
                        Intent intent = new Intent(SignupActivity.this, ChooseProfileActivity.class);
                        intent.putExtra("newUser", true);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error handling code remains the same
                        if (mAuth.getCurrentUser() != null) {
                            mAuth.getCurrentUser().delete()
                                    .addOnCompleteListener(deleteTask -> {
                                        Toast.makeText(SignupActivity.this, "Failed to save user data: " +
                                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(SignupActivity.this, "Failed to save user data: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}