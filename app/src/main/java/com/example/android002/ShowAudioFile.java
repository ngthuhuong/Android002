package com.example.android002;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.net.Uri;

public class ShowAudioFile extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_CODE = 101;
    private ListView listAudioFile;
    private TextView tvTitle;
    private AudioFileAdapter adapter;
    private List<File> audioFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_audio_file);

        listAudioFile = findViewById(R.id.listAudioFile);
        tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Danh sách nhạc trong thiết bị");

        // Kiểm tra và yêu cầu quyền
        if (checkPermission()) {
            loadAudioFiles();
        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAudioFiles();
            } else {
                Toast.makeText(this, "Cần cấp quyền để đọc file nhạc", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadAudioFiles() {
        audioFiles = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME
        };

        try (Cursor cursor = getContentResolver().query(
                uri,
                projection,
                null,
                null,
                null
        )) {
            if (cursor != null) {
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);

                while (cursor.moveToNext()) {
                    String path = cursor.getString(dataColumn);
                    String name = cursor.getString(nameColumn);
                    audioFiles.add(new File(path));
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi đọc file nhạc", Toast.LENGTH_SHORT).show();
        }

        if (audioFiles.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy file nhạc", Toast.LENGTH_SHORT).show();
        } else {
            adapter = new AudioFileAdapter(this, audioFiles);
            listAudioFile.setAdapter(adapter);
        }
    }

    private List<File> findAudioFiles(File directory) {
        List<File> audioFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    audioFiles.addAll(findAudioFiles(file));
                } else {
                    if (file.getName().endsWith(".mp3") || file.getName().endsWith(".wav") ||
                            file.getName().endsWith(".ogg") || file.getName().endsWith(".m4a")) {
                        audioFiles.add(file);
                    }
                }
            }
        }
        return audioFiles;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.releaseMediaPlayer();
        }
    }
}