package com.example.android002;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class SubActivity extends AppCompatActivity {
    private EditText etName, etPhone;
    private Integer contactId;
    private ImageView imageView;

    Button btnSave,btnCancel;
    ContentProvider contentProvider ;

    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int REQUEST_READ_STORAGE_PERMISSION = 101;
    Uri newImageContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sub);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etName = findViewById(R.id.textInputEditText2);
        etPhone = findViewById(R.id.textInputEditText3);
        btnSave = findViewById(R.id.btnAdd);
        btnCancel = findViewById(R.id.btnBack);
        imageView = findViewById(R.id.imageView);

        // Load contact data from intent
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            contactId = bundle.getInt("Id");
            etName.setText(bundle.getString("Name"));
            etPhone.setText(bundle.getString("Phone"));
            String imagePath = bundle.getString("Image");
            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("content://")) { //URI từ thư viện
                    Uri imageUri = Uri.parse(imagePath);
                    imageView.setImageURI(imageUri);
                }
            } else {
                imageView.setImageResource(R.drawable.img1);
            }
            btnSave.setText("LƯU");

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newName = etName.getText().toString();
                    String newPhone = etPhone.getText().toString();

                    contentProvider = new ContentProvider(SubActivity.this);
                    contentProvider.debugPrintAllContactIds();
                    Boolean isUpdated =false;
                    if(newImageContact==null){
                         isUpdated = contentProvider.updateContact(contactId, newName,newPhone);
                    } else  {
                         isUpdated = contentProvider.updateContact(contactId, newName,newPhone,newImageContact);
                    }

                    if(isUpdated){
                        Toast.makeText(SubActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        Intent returnItent = new Intent();
                        setResult(RESULT_OK,returnItent);
                        finish();
                    }else{
                        Toast.makeText(SubActivity.this,"Cap nhat ko thanh cong",Toast.LENGTH_LONG).show();

                    }

                }
            });
            btnCancel.setOnClickListener(v-> finish());
        } else if (intent == null && intent.getExtras() ==null) {
            etName.setText("");
            etPhone.setText("");
            imageView.setImageResource(R.drawable.img1);
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");

                if (ContextCompat.checkSelfPermission(SubActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                } else {
                    ActivityCompat.requestPermissions(SubActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_READ_STORAGE_PERMISSION);
                }
            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
            newImageContact = selectedImageUri;
        }
    }

}