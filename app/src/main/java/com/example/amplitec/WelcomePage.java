package com.example.amplitec;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class WelcomePage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);

        Button hearingAidButton = findViewById(R.id.Hearing_aid);
        Button multimediaButton = findViewById(R.id.multimedia);
        Button logoutButton = findViewById(R.id.logout_button); // Assuming you have a logout button in the layout

        hearingAidButton.setOnClickListener(v -> {
            Toast.makeText(WelcomePage.this, "Hearing Aid Selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(WelcomePage.this, HearingAidActivity.class);
            startActivity(intent);
        });

        multimediaButton.setOnClickListener(v -> {
            Toast.makeText(WelcomePage.this, "Multimedia Selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(WelcomePage.this, MultimediaActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(view -> {
            // Reset SharedPreferences login flag
            SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", false); // Reset the login flag
            editor.apply();

            // Clear the multimedia songs stored in SharedPreferences
            SharedPreferences multimediaPrefs = getSharedPreferences("MultimediaPrefs", MODE_PRIVATE);
            SharedPreferences.Editor multimediaEditor = multimediaPrefs.edit();
            multimediaEditor.clear(); // Clears all data from the SharedPreferences
            multimediaEditor.apply();

            // Sign out from Firebase Auth
            FirebaseAuth.getInstance().signOut();

            // Redirect to LoginPageActivity
            Intent intent = new Intent(getApplicationContext(), LoginPageActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Display a toast message or perform other actions
        Toast.makeText(this, "Use the LogOut button to LogOut", Toast.LENGTH_SHORT).show();
        // Do nothing else to prevent default back button behavior
    }
}
