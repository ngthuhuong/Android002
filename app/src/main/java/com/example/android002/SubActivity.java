package com.example.android002;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SubActivity extends AppCompatActivity {
    private EditText etName, etPhone;
    private Integer contactId;
    private ImageView imageView;

    Button btnSave,btnCancel;
    ContentProvider contentProvider ;

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
                    Boolean isUpdated = contentProvider.updateContact(contactId, newName,newPhone);
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
        }
    }
}