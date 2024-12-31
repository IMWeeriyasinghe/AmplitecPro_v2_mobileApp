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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class HearingAidVolume extends AppCompatActivity {
    private static final String TAG = "BluetoothApp";
    private static final String ESP32_MAC = "CC:7B:5C:F0:5B:52"; // Replace with your ESP32 MAC address
    private static final UUID UUID_BLUETOOTH = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String PREFS_NAME = "HearingAidPrefs";
    private static final String VOLUME_KEY = "volume";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private Button connectButton;
    private SeekBar volumeSeekBar;
    private TextView volumeLabel;
    private int currentVolume = 50; // Default volume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hearingaid_volume);

        connectButton = findViewById(R.id.Connected_Button);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        volumeLabel = findViewById(R.id.volumeLabel);
        ImageView backImage = findViewById(R.id.back_image);

        backImage.setOnClickListener(v -> {
            // Go back to the Welcome Page
            Intent intent = new Intent(HearingAidVolume.this, HearingAidActivity.class);  // Assuming your Welcome Page is WelcomePage.class
            startActivity(intent);
            finish();  // Optionally call finish to close the current activity
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Restore saved volume
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentVolume = preferences.getInt(VOLUME_KEY, 50); // Default to 50 if no saved value

        volumeSeekBar.setProgress(currentVolume);
        volumeLabel.setText("Volume: " + currentVolume);

        connectButton.setOnClickListener(v -> connectToESP32());
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentVolume = progress;
                volumeLabel.setText("Volume: " + currentVolume);

                // Send data immediately if connected
                sendData(currentVolume + "\n");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Save the current volume in preferences
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                editor.putInt(VOLUME_KEY, currentVolume);
                editor.apply();
            }
        });
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
            outputStream = bluetoothSocket.getOutputStream();
            Toast.makeText(this, "Connected to ESP32", Toast.LENGTH_SHORT).show();

            // Send the initial volume to ESP32 after connecting
            sendData(currentVolume + "\n");
        } catch (IOException e) {
            Log.e(TAG, "Connection failed", e);
            Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendData(String data) {
        try {
            if (outputStream != null) {
                outputStream.write(data.getBytes());
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to send data", e);
        }
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
