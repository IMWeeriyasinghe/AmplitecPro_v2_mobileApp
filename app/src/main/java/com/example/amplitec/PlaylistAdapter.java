package com.example.amplitec;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final List<Uri> songUris;
    private final MediaPlayer mediaPlayer;
    private final Context context;

    public PlaylistAdapter(Context context, List<Uri> songUris, MediaPlayer mediaPlayer) {
        this.context = context;
        this.songUris = songUris;
        this.mediaPlayer = mediaPlayer;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Uri songUri = songUris.get(position);
        holder.bind(songUri);

        // Play song on item click
        holder.itemView.setOnClickListener(v -> {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(context, songUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Unable to play song", Toast.LENGTH_SHORT).show();
            }
        });

        // Remove song on delete button click
        holder.removeButton.setOnClickListener(v -> {
            Toast.makeText(context, "Song removed from playlist", Toast.LENGTH_SHORT).show();
            if (context instanceof MultimediaActivity) {
                ((MultimediaActivity) context).onSongDeleted(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songUris.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView songTitle;
        ImageView removeButton;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.song_title);
            removeButton = itemView.findViewById(R.id.delete_song);
        }

        public void bind(Uri songUri) {
            songTitle.setText(songUri.getLastPathSegment()); // Display the song name
        }
    }
}
