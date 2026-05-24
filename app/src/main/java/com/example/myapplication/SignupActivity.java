package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;


public class SignupActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput, confirmInput;
    private Button signupBtn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); // mirror of login layout with 3 fields

        mAuth = FirebaseAuth.getInstance();

        emailInput    = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmInput  = findViewById(R.id.confirmInput);
        signupBtn     = findViewById(R.id.signupBtn);
        progressBar   = findViewById(R.id.progressBar);

        signupBtn.setOnClickListener(v -> attemptSignup());
    }

    private void attemptSignup() {
        String email    = emailInput.getText().toString().trim();
        String pass     = passwordInput.getText().toString().trim();
        String confirm  = confirmInput.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        signupBtn.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(r -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    signupBtn.setEnabled(true);
                    Toast.makeText(this, "Signup failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}