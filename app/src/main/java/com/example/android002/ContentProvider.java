package com.example.android002;

import static androidx.fragment.app.FragmentManager.TAG;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import android.Manifest;

public class ContentProvider {
    private Activity activity;

    public ContentProvider(Activity activity) {
        this.activity = activity;
    }

    private static final String TAG = "MyFragmentManager";
    public ArrayList<Contact> getAllContact() {
        ArrayList<Contact> listContact = new ArrayList<>();
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        };

        Cursor cursor = activity.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        Contact c = new Contact(
                                cursor.getInt(0),
                                cursor.getString(1),
                                cursor.getString(2),
                                cursor.getString(3));
                        listContact.add(c);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return listContact;
    }


    /**
     * Cập nhật thông tin contact
     * @param phoneId Phone._ID của số điện thoại cần cập nhật
     * @param newName Tên mới (null nếu không đổi tên)
     * @param newNumber Số mới (null nếu không đổi số)
     * @return true nếu thành công
     */
    public boolean updateContact(long phoneId, String newName, String newNumber) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("ContactUpdate", "Không có quyền WRITE_CONTACTS");
            return false;
        }

        // 2. Lấy CONTACT_ID và RAW_CONTACT_ID từ phoneId, vì việc thay đổi danh bạ chỉ có thể dùng Contact_ID
        long[] ids = getContactAndRawIds(phoneId);
        if (ids == null) {
            Log.e("ContactUpdate", "Không tìm thấy contact từ phoneId: " + phoneId);
            return false;
        }
        long contactId = ids[0];
        long rawContactId = ids[1];

        // 3. Tạo transaction update
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // Cập nhật tên nếu có
        if (newName != null) {
            ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                    .withSelection(
                            Data.RAW_CONTACT_ID + "=? AND " +
                                    Data.MIMETYPE + "=?",
                            new String[]{
                                    String.valueOf(rawContactId),
                                    StructuredName.CONTENT_ITEM_TYPE
                            })
                    .withValue(StructuredName.DISPLAY_NAME, newName)
                    .build());
        }

        // Cập nhật số điện thoại nếu có
        if (newNumber != null) {
            ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                    .withSelection(
                            Data._ID + "=? AND " +
                                    Data.MIMETYPE + "=?",
                            new String[]{
                                    String.valueOf(phoneId),
                                    Phone.CONTENT_ITEM_TYPE
                            })
                    .withValue(Phone.NUMBER, newNumber)
                    .build());
        }

        // 4. Thực thi
        try {
            ContentProviderResult[] results = activity.getContentResolver()
                    .applyBatch(ContactsContract.AUTHORITY, ops);

            Log.d("ContactUpdate", "Cập nhật thành công: " + results.length + " thay đổi");
            return true;
        } catch (Exception e) {
            Log.e("ContactUpdate", "Lỗi khi cập nhật: " + e.getMessage());
            return false;
        }
    }

    // Hàm hỗ trợ lấy CONTACT_ID và RAW_CONTACT_ID từ Phone._ID
    public long[] getContactAndRawIds(long phoneId) {
        Cursor c = null;
        try {
            c = activity.getContentResolver().query(
                    ContentUris.withAppendedId(Phone.CONTENT_URI, phoneId),
                    new String[]{
                            Phone.CONTACT_ID,
                            Phone.RAW_CONTACT_ID
                    }, null, null, null);

            if (c != null && c.moveToFirst()) {
                return new long[]{c.getLong(0), c.getLong(1)};
            }
        } finally {
            if (c != null) c.close();
        }
        return null;
    }

    public boolean deleteContact(long contactId) {
        try {
            long[] ids = getContactAndRawIds(contactId);

            // 1. Xóa từ bảng Contacts
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, ids[0]);
            int deletedRows = activity.getContentResolver().delete(
                    contactUri,
                    null,
                    null
            );

            // 2. Kiểm tra kết quả
            if (deletedRows > 0) {
                Log.d("ContactDelete", "Đã xóa contact ID: " + ids[0]);
                return true;
            } else {
                Log.e("ContactDelete", "Không tìm thấy contact để xóa: " + ids[0]);
                return false;
            }
        } catch (Exception e) {
            Log.e("ContactDelete", "Lỗi khi xóa contact: " + e.getMessage());
            return false;
        }
    }
    /**
     * Cập nhật thông tin liên hệ (ghi đè hàm cũ)
     * @param phoneId Phone._ID của số điện thoại cần cập nhật
     * @param newName Tên mới (truyền null nếu không đổi tên)
     * @param newNumber Số mới (truyền null nếu không đổi số)
     * @param newImageUri Uri của ảnh mới (truyền null nếu không đổi ảnh)
     * @return true nếu cập nhật thành công
     */
    public boolean updateContact(long phoneId, String newName, String newNumber, Uri newImageUri) {
        // 1. Kiểm tra quyền ghi danh bạ
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("ContactUpdate", "Permission denied: WRITE_CONTACTS");
            return false;
        }

        long[] ids = getContactAndRawIds(phoneId);
        if (ids == null) {
            Log.e("ContactUpdate", "No contact found for phoneId: " + phoneId);
            return false;
        }
        long rawContactId = ids[1];
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        if (newName != null) {
            ops.add(createNameUpdateOperation(rawContactId, newName));
        }
        if (newNumber != null) {
            ops.add(createPhoneUpdateOperation(phoneId, newNumber));
        }
        if (newImageUri != null) {
            try {
                ContentProviderOperation photoOp = createPhotoUpdateOperation(rawContactId, newImageUri);
                if (photoOp != null) {
                    ops.add(photoOp);
                }
            } catch (Exception e) {
                Log.e("ContactUpdate", "Failed to process image", e);
            }
        }
        if (!ops.isEmpty()) {
            try {
                ContentProviderResult[] results = activity.getContentResolver()
                        .applyBatch(ContactsContract.AUTHORITY, ops);
                Log.d("ContactUpdate", "Successfully executed " + results.length + " operations");
                return true;
            } catch (Exception e) {
                Log.e("ContactUpdate", "Batch operation failed", e);
            }
        }
        return false;
    }

    // Helper method: Tạo operation cập nhật tên
    private ContentProviderOperation createNameUpdateOperation(long rawContactId, String newName) {
        return ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                .withSelection(
                        Data.RAW_CONTACT_ID + "=? AND " +
                                Data.MIMETYPE + "=?",
                        new String[]{
                                String.valueOf(rawContactId),
                                StructuredName.CONTENT_ITEM_TYPE
                        })
                .withValue(StructuredName.DISPLAY_NAME, newName)
                .build();
    }

    // Helper method: Tạo operation cập nhật số điện thoại
    private ContentProviderOperation createPhoneUpdateOperation(long phoneId, String newNumber) {
        return ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                .withSelection(
                        Data._ID + "=? AND " +
                                Data.MIMETYPE + "=?",
                        new String[]{
                                String.valueOf(phoneId),
                                Phone.CONTENT_ITEM_TYPE
                        })
                .withValue(Phone.NUMBER, newNumber)
                .build();
    }

    // Helper method: Tạo operation cập nhật ảnh, android contact provider ko ho tro doi anh qua uri nen phai
    //doi thanh byte array
    private ContentProviderOperation createPhotoUpdateOperation(long rawContactId, Uri imageUri) {
        try {
            // 1. Đọc ảnh từ Uri và chuyển thành byte array
            InputStream input = activity.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            if (bitmap == null) {
                Log.e("ContactUpdate", "Failed to decode image");
                return null;
            }

            // 2. Chuyển đổi bitmap thành byte array
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            byte[] photoBytes = output.toByteArray();

            // 3. Tạo operation cập nhật ảnh
            return ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                    .withSelection(
                            Data.RAW_CONTACT_ID + "=? AND " +
                                    Data.MIMETYPE + "=?",
                            new String[]{
                                    String.valueOf(rawContactId),
                                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                            })
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes)
                    .build();
        } catch (Exception e) {
            Log.e("ContactUpdate", "Error creating photo update operation", e);
            return null;
        }
    }
    public void debugPrintAllContactIds() {
        Cursor cursor = activity.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME},
                null, null, null
        );

        Log.d("ContactDebug", "==== DANH SÁCH CONTACT HIỆN CÓ ====");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Log.d("ContactDebug", "ID: " + cursor.getLong(0) +
                        " | Name: " + cursor.getString(1));
            }
            cursor.close();
        }
        Log.d("ContactDebug", "==== KẾT THÚC DANH SÁCH ====");
    }
}