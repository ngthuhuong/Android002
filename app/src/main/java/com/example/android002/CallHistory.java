package com.example.android002;


import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.ArrayList;

public class CallHistory extends AppCompatActivity {
    private ListView listHistory;
    private CallHistoryAdapter adapter;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_history);

        // Ánh xạ view
        listHistory = findViewById(R.id.listHistory);
        btnCancel = findViewById(R.id.btnCancel);
        TextView textView = findViewById(R.id.textView);

        // Nhận dữ liệu từ Intent
        ArrayList<String> callHistoryList = getIntent().getStringArrayListExtra("CALL_HISTORY_LIST");

        // Thiết lập tiêu đề
        textView.setText("Lịch sử cuộc gọi");

        // Tạo adapter và gán cho ListView
        adapter = new CallHistoryAdapter(this, callHistoryList);
        listHistory.setAdapter(adapter);

        // Xử lý sự kiện nút Cancel
        btnCancel.setOnClickListener(v -> finish());
    }
}