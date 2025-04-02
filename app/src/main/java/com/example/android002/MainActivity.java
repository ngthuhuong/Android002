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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 200;
    private Button btn;
    private EditText input;
    private ListView danhsach;
    private ArrayList<Contact> ContactList;
    private Adapter adapter;

    private int selectedId;

    private ContentProvider contentProvider;
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
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        Contact c = ContactList.get(selectedId);
        if(item.getItemId() == R.id.mnEdit){//sua danh ba
            //mo sub activity de edit
            openSubActivity(c);
        }

        return super.onContextItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 200 && resultCode == RESULT_OK){
            ArrayList<Contact> updatedContacts = contentProvider.getAllContact();

            // Cập nhật Adapter hoặc ListView/RecyclerView
//            adapter.updateContacts(updatedContacts);
//            adapter.notifyDataSetChanged();
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
}