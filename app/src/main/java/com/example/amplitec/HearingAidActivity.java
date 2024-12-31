package com.example.amplitec;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HearingAidActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hearingaid_page);

        Button volumeButton = findViewById(R.id.volume_control);
        Button batteryStatusButton = findViewById(R.id.battery_status);
        ImageView backImage = findViewById(R.id.back_image);

        backImage.setOnClickListener(v -> {
            // Go back to the Welcome Page
            Intent intent = new Intent(HearingAidActivity.this, WelcomePage.class);  // Assuming your Welcome Page is WelcomePage.class
            startActivity(intent);
            finish();  // Optionally call finish to close the current activity
        });

        volumeButton.setOnClickListener(v -> {
            Toast.makeText(HearingAidActivity.this, "Volume Control Selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HearingAidActivity.this, HearingAidVolume.class);
            startActivity(intent);

        });

        batteryStatusButton.setOnClickListener(v -> {
            Toast.makeText(HearingAidActivity.this, "Battery Status Selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HearingAidActivity.this, BatteryStatusActivity.class);
            startActivity(intent);

        });
    }

    @Override
    public void onBackPressed() {
        // Display a toast message or perform other actions
        Toast.makeText(this, "Use the back button provided in the app.", Toast.LENGTH_SHORT).show();
        // Do nothing else to prevent default back button behavior
    }
}


