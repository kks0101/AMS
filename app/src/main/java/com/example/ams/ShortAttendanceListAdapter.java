package com.example.ams;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ShortAttendanceListAdapter extends ArrayAdapter<ShortAttendanceDetail> {

    private Context mContext;
    private ArrayList<ShortAttendanceDetail> shortList = new ArrayList<>();

    public ShortAttendanceListAdapter(@NonNull Context context, ArrayList<ShortAttendanceDetail> list){
        super(context, 0, list);
        this.mContext = context;
        this.shortList = list;
    }
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null){
            listItem = LayoutInflater.from(mContext).inflate(R.layout.short_listview, parent, false);

        }
        ShortAttendanceDetail shortAttendanceDetail = shortList.get(position);
        TextView nameTextView = (TextView)listItem.findViewById(R.id.nameTextView);
        TextView emailIdTextView = (TextView)listItem.findViewById(R.id.emailIdTextView);
        TextView percentTextView = (TextView)listItem.findViewById(R.id.percentTextView);

        nameTextView.setText(shortAttendanceDetail.getName());
        emailIdTextView.setText(shortAttendanceDetail.getEmailId());
        percentTextView.setText(Double.toString(shortAttendanceDetail.getPercent()*100) + "%");

        return listItem;
    }
}
