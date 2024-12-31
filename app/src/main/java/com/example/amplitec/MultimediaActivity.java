package com.example.amplitec;

import android.annotation.SuppressLint;
import android.app.AutomaticZenRule;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

public class MultimediaActivity extends AppCompatActivity {



    private static final String PREF_NAME = "MultimediaPrefs";
    private static final String KEY_SONG_URIS = "SongUris";
    private static final String QCC5125_MAC_ADDRESS = "40:ED:98:19:87:89";
    private static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
    private static final UUID BATTERY_LEVEL_UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private TextView batteryLevelTextView;
    private MediaPlayer mediaPlayer;
    private List<Uri> songUris = new ArrayList<>();
    private int currentSongIndex = 0;
    private AudioManager audioManager;
    private Uri selectedSongUri;
    private PlaylistAdapter playlistAdapter;
    private RecyclerView playlistRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multimedia_page);

        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //startActivityForResult(enableBtIntent, 1);
        }

        playlistRecyclerView = findViewById(R.id.playlist_recycler_view);
        playlistAdapter = new PlaylistAdapter(this, songUris, mediaPlayer);
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        playlistRecyclerView.setAdapter(playlistAdapter);

        Button selectMp3 = findViewById(R.id.select_mp3);
        Button playMusic = findViewById(R.id.BtnPlay);
        Button pauseMusic = findViewById(R.id.BtnPause);
        Button nextTrack = findViewById(R.id.BtnNext);
        Button previousTrack = findViewById(R.id.BtnPrevious);
        Button volumeUp = findViewById(R.id.volumeUp); // Volume Up Button
        Button volumeDown = findViewById(R.id.volumeDown); // Volume Down Button
        ImageView backImage = findViewById(R.id.back_image);

        backImage.setOnClickListener(v -> {
            // Go back to the Welcome Page
            Intent intent = new Intent(MultimediaActivity.this, WelcomePage.class);  // Assuming your Welcome Page is WelcomePage.class
            startActivity(intent);
            finish();  // Optionally call finish to close the current activity
        });

        selectMp3.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("audio/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 1);
        });

        playMusic.setOnClickListener(v -> playMusic());
        pauseMusic.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause(); // Pause the music if it is playing
                    Toast.makeText(this, "Music Paused", Toast.LENGTH_SHORT).show();
                } else {
                    mediaPlayer.start(); // Resume the music if it is paused
                    Toast.makeText(this, "Music Resumed", Toast.LENGTH_SHORT).show();
                }
            }
        });


        nextTrack.setOnClickListener(v -> playNextTrack());
        previousTrack.setOnClickListener(v -> playPreviousTrack());

        // Volume Up Button Click Listener
        volumeUp.setOnClickListener(v -> {
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
            Toast.makeText(this, "Volume Up", Toast.LENGTH_SHORT).show();
        });

        // Volume Down Button Click Listener
        volumeDown.setOnClickListener(v -> {
            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
            Toast.makeText(this, "Volume Down", Toast.LENGTH_SHORT).show();
        });

        // Load saved songs from SharedPreferences
        loadSavedSongs();
    }

    @Override
    public void onBackPressed() {
        // Display a toast message or perform other actions
        Toast.makeText(this, "Use the back button provided in the app.", Toast.LENGTH_SHORT).show();
        // Do nothing else to prevent default back button behavior
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause(); // Pause playback when leaving activity
        }
    }


    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            // Auto-play the next track after the current one finishes
            playNextTrack();
        });
    }



    public void onSongDeleted(int position) {
        songUris.remove(position); // Remove song from list
        playlistAdapter.notifyItemRemoved(position); // Notify adapter about item removal
        saveSongsToPreferences(); // Save updated list to SharedPreferences

        if (songUris.isEmpty()) {
            showAddSongsPopup();
        }
    }

    private void showAddSongsPopup() {
        Toast.makeText(this, "Playlist is empty. Please add songs.", Toast.LENGTH_LONG).show();
    }

    private void playMusic() {
        if (!songUris.isEmpty()) {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(this, songUris.get(currentSongIndex));
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                Toast.makeText(this, "Unable to play selected song", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }catch (SecurityException e) {
                Toast.makeText(this, "Permission denied for selected song", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "No song selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void playNextTrack() {
        if (!songUris.isEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songUris.size();
            playMusic();
        }
    }

    private void playPreviousTrack() {
        if (!songUris.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + songUris.size()) % songUris.size();
            playMusic();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri songUri = data.getData();
                if (songUri != null) {
                    // Check if the song is already in the playlist
                    if (songUris.contains(songUri)) {
                        Toast.makeText(this, "Song already exists in the playlist!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Add the selected song to the playlist
                    songUris.add(songUri);
                    playlistAdapter.notifyItemInserted(songUris.size() - 1);
                    saveSongsToPreferences();

                    Toast.makeText(this, "Song added to playlist", Toast.LENGTH_SHORT).show();

                    // If it's the first song added, start playing it immediately
                    if (songUris.size() == 1) {
                        currentSongIndex = 0;
                        playMusic();
                    }
                }
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveSongsToPreferences() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        ArrayList<String> songUriStrings = new ArrayList<>();

        for (Uri uri : songUris) {
            songUriStrings.add(uri.toString());
        }

        editor.putStringSet(KEY_SONG_URIS, new HashSet<>(songUriStrings));
        editor.apply();
    }

    private void loadSavedSongs() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        Set<String> songUriStrings = preferences.getStringSet(KEY_SONG_URIS, new HashSet<>());

        songUris.clear();  // Clear the list to avoid duplicating items when loading songs multiple times
        for (String uriString : songUriStrings) {
            Uri uri = Uri.parse(uriString);
            songUris.add(uri);
        }

        if (songUris.isEmpty()) {
            showAddSongsPopup();
        }

        // Refresh the RecyclerView adapter to show the loaded songs
        playlistAdapter.notifyDataSetChanged();



    }

}