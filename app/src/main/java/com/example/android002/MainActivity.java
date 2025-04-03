package com.example.android002;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import android.database.Cursor;
import android.provider.CallLog;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 200;
    private Button btn;
    private EditText input;
    private ListView danhsach;
    private ArrayList<Contact> ContactList;
    private Adapter adapter;

    private int selectedId;

    private ContentProvider contentProvider;
    private static final int REQUEST_READ_CALL_LOG = 101;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.actionmenu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.contextmenu, menu);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        input = findViewById(R.id.input);
        btn = findViewById(R.id.btnNhap);
        danhsach = findViewById(R.id.lstView);

        ContactList = new ArrayList<Contact>();
        adapter = new Adapter(ContactList, this);
        danhsach.setAdapter(adapter);
        showContact();

        registerForContextMenu(danhsach);
        danhsach.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long l) {
                selectedId = i;
                return false;
            }
        });



        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle);
                //mở subactivity
                startActivityForResult(intent,210);
            }
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        Contact c = ContactList.get(selectedId);
        if(item.getItemId() == R.id.mnEdit){//sua danh ba
            //mo sub activity de edit
            openSubActivity(c);
        } else if (item.getItemId()== R.id.mnDelete) {
            long selectedContactId =  c.getId();
            Boolean isDeleted = contentProvider.deleteContact(selectedContactId);
            // Hiển thị dialog xác nhận
            new AlertDialog.Builder(this)
                    .setTitle("Xóa liên hệ")
                    .setMessage("Bạn chắc chắn muốn xóa?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        if (isDeleted) {
                            Toast.makeText(this, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                            showContact();
                        } else {
                            Toast.makeText(this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        } else if (item.getItemId()==R.id.mnHistory) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CALL_LOG},
                        REQUEST_READ_CALL_LOG);
            } else {
                showCallHistory(c.getNumber());
            }

        }

        return super.onContextItemSelected(item);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.mnHis){
            //ham doc lich su cuoc goi tu tat ca so dien thoai
            showAllCallHistory();
        }
        return super.onOptionsItemSelected(item);

    }

    private void showAllCallHistory() {
        // Define which columns we want to query
        String[] projection = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_NAME
        };

        // Query the call log (all calls, not filtered by number)
        Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                CallLog.Calls.DATE + " DESC LIMIT 100");  // Show most recent 100 calls

        if (cursor != null) {
            try {
                StringBuilder callHistory = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                while (cursor.moveToNext()) {
                    // Get column indices safely
                    int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                    int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                    int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
                    int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
                    int nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);

                    // Skip if any required column is missing
                    if (numberIndex == -1 || typeIndex == -1 || dateIndex == -1) continue;

                    // Extract values
                    String number = cursor.getString(numberIndex);
                    int type = cursor.getInt(typeIndex);
                    long date = cursor.getLong(dateIndex);
                    long duration = (durationIndex != -1) ? cursor.getLong(durationIndex) : 0;
                    String name = (nameIndex != -1) ? cursor.getString(nameIndex) : "Unknown";

                    // Format call type
                    String callType;
                    switch (type) {
                        case CallLog.Calls.INCOMING_TYPE:
                            callType = "Gọi tới";
                            break;
                        case CallLog.Calls.OUTGOING_TYPE:
                            callType = "Gọi đi";
                            break;
                        case CallLog.Calls.MISSED_TYPE:
                            callType = "Gọi nhỡ";
                            break;
                        default:
                            callType = "Số lạ";
                    }

                    // Format the entry
                    callHistory.append(sdf.format(new Date(date)))
                            .append(" - ")
                            .append(callType)
                            .append(" - ")
                            .append(name.isEmpty() ? number : name)
                            .append(" (")
                            .append(duration)
                            .append("s)\n\n");
                }

                // Show results in a dialog
                new AlertDialog.Builder(this)
                        .setTitle("Call History")
                        .setMessage(callHistory.toString())
                        .setPositiveButton("OK", null)
                        .show();

            } finally {
                cursor.close();
            }
        } else {
            Toast.makeText(this, "Failed to load call history", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 200 && resultCode == RESULT_OK){
            ArrayList<Contact> updatedContacts = contentProvider.getAllContact();
            showContact();
            Toast.makeText(this, "Danh bạ đã được cập nhật", Toast.LENGTH_SHORT).show();
        } else if (requestCode==210 && resultCode ==211) {
            ArrayList<Contact> updatedContacts = contentProvider.getAllContact();
            showContact();
            Toast.makeText(this, "Danh bạ đã được cập nhật", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void openSubActivity(Contact c) {
        Intent intent = new Intent(MainActivity.this, SubActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("Id",c.getId());
        bundle.putString("Image",c.getImage());
        bundle.putString("Name",c.getName());
        bundle.putString("Phone",c.getNumber());
        intent.putExtras(bundle);
        //mở subactivity
        startActivityForResult(intent,200);
    }

    private void showContact(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(android.Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        }else{
            contentProvider = new ContentProvider(this);
            ContactList = contentProvider.getAllContact();
            adapter = new Adapter(ContactList,this);
            danhsach.setAdapter(adapter);
        }
    }
    private void showCallHistory(String phone) {
        // Thử chuẩn hóa số điện thoại trước khi truy vấn
        String phoneNumber = phone.replaceAll("[^0-9+]", "");
        Log.d("phone : ",phoneNumber);
        String[] projection = {
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.NUMBER
        };

        Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                CallLog.Calls.NUMBER + " = ?",
                new String[]{phoneNumber},
                CallLog.Calls.DATE + " DESC");

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    StringBuilder callHistory = new StringBuilder();
                    int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                    int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
                    int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);

                    do {
                        // Kiểm tra từng cột có tồn tại không
                        if (typeIndex < 0 || dateIndex < 0 || durationIndex < 0) {
                            Log.e("CallLog", "Missing required columns in cursor");
                            break;
                        }

                        int callType = cursor.getInt(typeIndex);
                        long date = cursor.getLong(dateIndex);
                        long duration = cursor.getLong(durationIndex);

                        String type;
                        switch (callType) {
                            case CallLog.Calls.INCOMING_TYPE:
                                type = "Cuộc gọi đến";
                                break;
                            case CallLog.Calls.OUTGOING_TYPE:
                                type = "Cuộc gọi đi";
                                break;
                            case CallLog.Calls.MISSED_TYPE:
                                type = "Cuộc gọi nhỡ";
                                break;
                            default:
                                type = "Không xác định";
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String callDate = sdf.format(new Date(date));

                        callHistory.append(type)
                                .append(" - ")
                                .append(callDate)
                                .append(" - ")
                                .append(duration)
                                .append(" giây\n");

                    } while (cursor.moveToNext());

                    // Hiển thị kết quả
                    new AlertDialog.Builder(this)
                            .setTitle("Lịch sử cuộc gọi")
                            .setMessage(callHistory.toString())
                            .setPositiveButton("Đóng", null)
                            .show();
                } else {
                    Toast.makeText(this, "Không có lịch sử cuộc gọi", Toast.LENGTH_SHORT).show();
                }
            } finally {
                cursor.close(); // Luôn đóng cursor sau khi dùng xong
            }
        } else {
            Toast.makeText(this, "Lỗi khi truy vấn lịch sử cuộc gọi", Toast.LENGTH_SHORT).show();
        }
    }
}