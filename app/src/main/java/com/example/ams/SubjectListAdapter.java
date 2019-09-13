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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SubjectListAdapter extends ArrayAdapter<TeacherSubjectDetail> {
    private Context mContext;
    private ArrayList<TeacherSubjectDetail> subjectList = new ArrayList<>();

    public SubjectListAdapter(@NonNull Context context, ArrayList<TeacherSubjectDetail> list){
        super(context, 0, list);
        this.mContext = context;
        this.subjectList = list;
    }



    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null){
            listItem = LayoutInflater.from(mContext).inflate(R.layout.activity_listview, parent, false);

        }
        TeacherSubjectDetail subjectDetail = subjectList.get(position);
        TextView subjectCodeTextView = (TextView)listItem.findViewById(R.id.subjectCodeTextView);
        TextView branchTextView = (TextView)listItem.findViewById(R.id.branchTextView);
        TextView subjectNameTextView = (TextView)listItem.findViewById(R.id.subjectNameTextView);
        ImageButton deleteButton = (ImageButton)listItem.findViewById(R.id.deleteButton);

        subjectNameTextView.setText(subjectDetail.getSubjectName());
        subjectCodeTextView.setText(subjectDetail.getSubjectCode());
        branchTextView.setText(subjectDetail.getBranch());

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subjectList.remove(position);
                notifyDataSetChanged();
            }
        });
        return listItem;
    }
}

