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
                ArrayList<String> callHistory = getCallHistoryByPhoneNumber(c.getNumber());
                if (callHistory.isEmpty()) {
                    Toast.makeText(this, "Không có lịch sử cuộc gọi", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(this, CallHistory.class);
                    intent.putStringArrayListExtra("CALL_HISTORY_LIST", callHistory);
                    intent.putExtra("PHONE_NUMBER", c.getNumber());
                    startActivity(intent);
                }
            }
        }
        return super.onContextItemSelected(item);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.mnHis){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                    == PackageManager.PERMISSION_GRANTED) {
                ArrayList<String> callHistory = getCallHistory();
                if (callHistory.isEmpty()) {
                    Toast.makeText(this, "Không có lịch sử cuộc gọi", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(this, CallHistory.class);
                    intent.putStringArrayListExtra("CALL_HISTORY_LIST", callHistory);
                    startActivityForResult(intent,220);
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CALL_LOG},
                        REQUEST_READ_CALL_LOG);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);

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
        // Thử chuẩn hóa số điện thoại
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
                            .setTitle("Lịch sử cuộc gọi của" +phone)
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

    private ArrayList<String> getCallHistory() {
        ArrayList<String> callHistoryList = new ArrayList<>();

        String[] projection = {
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME
        };

        Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null, //ko lấy điều kiện where
                null,
                CallLog.Calls.DATE + " DESC"); //ngay gan nhat hien truoc

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                    int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
                    int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
                    int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                    int nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);

                    do {
                        if (typeIndex < 0 || dateIndex < 0 || durationIndex < 0 || numberIndex < 0) {
                            Log.e("CallLog", "Missing required columns in cursor");
                            break;
                        }

                        int callType = cursor.getInt(typeIndex);
                        long date = cursor.getLong(dateIndex);
                        long duration = cursor.getLong(durationIndex);
                        String number = cursor.getString(numberIndex);
                        String name = cursor.getString(nameIndex);

                        String contact = (name != null && !name.isEmpty()) ? name : number;

                        String type;
                        switch (callType) {
                            case CallLog.Calls.INCOMING_TYPE:
                                type = "Gọi đến";
                                break;
                            case CallLog.Calls.OUTGOING_TYPE:
                                type = "Gọi di";
                                break;
                            case CallLog.Calls.MISSED_TYPE:
                                type = "Nhỡ";
                                break;
                            default:
                                type = "Khác";
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String callDate = sdf.format(new Date(date));

                        String callInfo = contact + "\n" + type + " - " + callDate + " - " + duration + " giây";
                        callHistoryList.add(callInfo);

                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return callHistoryList;
    }
    private ArrayList<String> getCallHistoryByPhoneNumber(String phoneNumber) {
        ArrayList<String> callHistoryList = new ArrayList<>();

        // Chuẩn hóa số điện thoại (bỏ các ký tự không phải số)
        String normalizedNumber = phoneNumber.replaceAll("[^0-9+]", "");

        // Các cột dữ liệu cần lấy từ CallLog
        String[] projection = {
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME
        };

        // Truy vấn với điều kiện số điện thoại
        Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                CallLog.Calls.NUMBER + " = ?",  // Điều kiện WHERE
                new String[]{normalizedNumber}, // Giá trị cho điều kiện
                CallLog.Calls.DATE + " DESC"    // Sắp xếp theo ngày giảm dần
        );

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    // Lấy chỉ số cột
                    int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                    int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
                    int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
                    int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                    int nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);

                    do {
                        // Đọc giá trị từ cursor
                        int callType = cursor.getInt(typeIndex);
                        long date = cursor.getLong(dateIndex);
                        long duration = cursor.getLong(durationIndex);
                        String number = cursor.getString(numberIndex);
                        String name = cursor.getString(nameIndex);

                        // Xác định loại cuộc gọi
                        String callTypeStr;
                        switch (callType) {
                            case CallLog.Calls.INCOMING_TYPE:
                                callTypeStr = "Cuộc gọi đến";
                                break;
                            case CallLog.Calls.OUTGOING_TYPE:
                                callTypeStr = "Cuộc gọi đi";
                                break;
                            case CallLog.Calls.MISSED_TYPE:
                                callTypeStr = "Cuộc gọi nhỡ";
                                break;
                            default:
                                callTypeStr = "Không xác định";
                        }

                        // Định dạng ngày tháng
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String callDate = sdf.format(new Date(date));

                        // Tạo chuỗi thông tin cuộc gọi
                        String contactInfo = (name != null && !name.isEmpty()) ? name : number;
                        String callInfo = contactInfo + "\n"
                                + callTypeStr + " - "
                                + callDate + " - "
                                + duration + " giây";

                        callHistoryList.add(callInfo);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close(); // Đóng cursor sau khi sử dụng
            }
        }

        return callHistoryList;
    }
}