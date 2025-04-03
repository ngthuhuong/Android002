package com.example.android002;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class CallHistoryAdapter extends ArrayAdapter<String> {
    private Context context;
    private ArrayList<String> callHistoryList;

    public CallHistoryAdapter(Context context, ArrayList<String> callHistoryList) {
        super(context, R.layout.item_call_history, callHistoryList);
        this.context = context;
        this.callHistoryList = callHistoryList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_call_history, parent, false);

            holder = new ViewHolder();
            holder.tvContactInfo = convertView.findViewById(R.id.tvContactInfo);
            holder.tvCallDetails = convertView.findViewById(R.id.tvCallDetails);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String callInfo = callHistoryList.get(position);
        String[] parts = callInfo.split("\n");

        if (parts.length >= 2) {
            holder.tvContactInfo.setText(parts[0]);
            holder.tvCallDetails.setText(parts[1]);
        } else {
            holder.tvContactInfo.setText(callInfo);
            holder.tvCallDetails.setText("");
        }

        return convertView;
    }

    static class ViewHolder {
        TextView tvContactInfo;
        TextView tvCallDetails;
    }
}
