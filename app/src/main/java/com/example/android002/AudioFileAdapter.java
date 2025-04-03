package com.example.android002;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AudioFileAdapter extends ArrayAdapter<File> {
    private Context context;
    private List<File> audioFiles;
    private MediaPlayer mediaPlayer;
    private int currentPosition = -1;

    public AudioFileAdapter(Context context, List<File> audioFiles) {
        super(context, 0, audioFiles);
        this.context = context;
        this.audioFiles = audioFiles;
        this.mediaPlayer = new MediaPlayer();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_file_sound, parent, false);
        }

        File audioFile = audioFiles.get(position);

        TextView tvTenNhac = convertView.findViewById(R.id.tvTenNhac);
        TextView tvThongtin = convertView.findViewById(R.id.tvThongtin);
        Button btnPlay = convertView.findViewById(R.id.button);
        tvTenNhac.setText(audioFile.getName());

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(audioFile.getAbsolutePath());

            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long dur = Long.parseLong(duration);
            String durationFormatted = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(dur),
                    TimeUnit.MILLISECONDS.toSeconds(dur) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(dur)));

            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (artist == null) artist = "Unknown";

            String info = artist + " | " + durationFormatted + " | " + formatFileSize(audioFile.length());
            tvThongtin.setText(info);
        } catch (Exception e) {
            tvThongtin.setText("Không thể đọc thông tin");
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Sự kiện nút phát
        btnPlay.setOnClickListener(v -> {
            if (currentPosition == position && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlay.setText("Phát");
            }  else {
                playAudio(audioFile, position);
                btnPlay.setText("Tạm dừng");
            }
        });

        return convertView;
    }

    private void playAudio(File audioFile, int position) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentPosition = position;

            mediaPlayer.setOnCompletionListener(mp -> {
                currentPosition = -1;
                notifyDataSetChanged();
            });

            notifyDataSetChanged();
        } catch (IOException e) {
            Toast.makeText(context, "Không thể phát file audio", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}