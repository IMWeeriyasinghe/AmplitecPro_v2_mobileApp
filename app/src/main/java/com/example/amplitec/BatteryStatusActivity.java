package com.example.amplitec;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

public class BatteryStatusActivity extends AppCompatActivity {
    private static final String TAG = "BatteryStatusActivity";
    private static final String ESP32_MAC = "CC:7B:5C:F0:5B:52"; // Replace with your ESP32 MAC
    private static final UUID UUID_BLUETOOTH = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String PREFS_NAME = "BatteryStatusPrefs";
    private static final String BATTERY_KEY = "battery";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BufferedReader inputReader;
    private Button connectButton;
    private TextView batteryLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battery_status);

        connectButton = findViewById(R.id.connectButton);
        batteryLabel = findViewById(R.id.batteryLabel);  // TextView to display battery percentage
        ImageView backImage = findViewById(R.id.back_image);

        backImage.setOnClickListener(v -> {
            // Go back to the Welcome Page
            Intent intent = new Intent(BatteryStatusActivity.this, HearingAidActivity.class);  // Assuming your Welcome Page is WelcomePage.class
            startActivity(intent);
            finish();  // Optionally call finish to close the current activity
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Load the last known battery percentage from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastBatteryValue = preferences.getString(BATTERY_KEY, "Unknown");
        batteryLabel.setText("Battery: " + lastBatteryValue + "%");

        connectButton.setOnClickListener(v -> connectToESP32());
    }

    private void connectToESP32() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return;
        }

        BluetoothDevice esp32 = bluetoothAdapter.getRemoteDevice(ESP32_MAC);
        try {
            bluetoothSocket = esp32.createRfcommSocketToServiceRecord(UUID_BLUETOOTH);
            bluetoothSocket.connect();
            inputReader = new BufferedReader(new InputStreamReader(bluetoothSocket.getInputStream()));

            Toast.makeText(this, "Connected to ESP32", Toast.LENGTH_SHORT).show();
            startPercentageReading();  // Start reading the percentage data
        } catch (IOException e) {
            Log.e(TAG, "Connection failed", e);
            Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
        }
    }

    private void startPercentageReading() {
        new Thread(() -> {
            while (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                try {
                    // Read the data sent from ESP32
                    String data = inputReader.readLine();
                    if (data != null) {
                        Log.d(TAG, "Received data: " + data);

                        // Extract battery percentage (e.g., "Battery:85%, Volume:50%")
                        String batteryPercentage = parseBatteryPercentage(data);

                        // Update the TextView with battery percentage
                        if (batteryPercentage != null) {
                            runOnUiThread(() -> batteryLabel.setText("Battery: " + batteryPercentage + "%"));

                            // Save the battery value to SharedPreferences
                            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                            editor.putString(BATTERY_KEY, batteryPercentage);
                            editor.apply();
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading percentage data", e);
                    break;
                }
            }
        }).start();
    }

    private String parseBatteryPercentage(String data) {
        try {
            // Check if the data contains "Battery:"
            if (data.contains("Battery:")) {
                // Extract the percentage value using substring and split
                String[] parts = data.split("Battery:")[1].split("%")[0].trim().split(",");
                return parts[0];  // Return the first number after "Battery:"
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing battery percentage", e);
        }
        return null; // Return null if parsing fails
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close socket", e);
        }
    }
    @Override
    public void onBackPressed() {
        // Display a toast message or perform other actions
        Toast.makeText(this, "Use the back button provided in the app.", Toast.LENGTH_SHORT).show();
        // Do nothing else to prevent default back button behavior
    }
}
