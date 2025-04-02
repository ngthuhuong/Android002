package com.example.android002;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class Adapter extends BaseAdapter implements Filterable {

    public void setData(ArrayList<Contact> data) {
        this.data = data;
    }

    private ArrayList<Contact> data;

    private ArrayList<Contact> databackup;

    private Activity context;

    private LayoutInflater inflater; //doi contact xml thanh 1 view

    public Adapter(ArrayList<Contact> data, Activity context) {
        this.data = data;
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    public Adapter(ArrayList<Contact> data, ArrayList<Contact> databackup, Activity context, LayoutInflater inflater) {
        this.data = data;
        this.databackup = databackup;
        this.context = context;
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View convertedView = convertView;

        if (convertedView == null) {
            convertedView = inflater.inflate(R.layout.contact, null);
        }

        ImageView imageView = convertedView.findViewById(R.id.avatarView);
        TextView tvName = convertedView.findViewById(R.id.nameView);
        TextView tvPhone = convertedView.findViewById(R.id.phoneView);
        Contact contact = data.get(position);
        tvName.setText(contact.getName());
        tvPhone.setText(contact.getNumber());

        String imagePath = contact.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imagePath.startsWith("content://")) { //URI từ thư viện
                Uri imageUri = Uri.parse(imagePath);
                imageView.setImageURI(imageUri);
            }
        } else {
            imageView.setImageResource(R.drawable.img1);
        }
        return convertedView;
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}
