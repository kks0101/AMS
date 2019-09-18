package com.example.ams.student;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ams.R;
import com.example.ams.teacher.TeacherSubjectDetail;

import java.util.ArrayList;

public class StudentSubjectAdapter extends RecyclerView.Adapter<StudentSubjectAdapter.StudentSubjectViewHolder> {
    private Context mContext;
    private ArrayList<TeacherSubjectDetail> subjectList = new ArrayList<>();

    public StudentSubjectAdapter(@NonNull Context context, ArrayList<TeacherSubjectDetail> list){
        this.mContext = context;
        this.subjectList = list;
    }

    @NonNull
    @Override
    public StudentSubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.layout_teacher_subject, null);

        return new StudentSubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentSubjectViewHolder holder, int position) {
        //getting the subject details of the specified position
        TeacherSubjectDetail teacherSubjectDetail = subjectList.get(position);

        //binding the data with the viewholder views
        holder.subjectCodeTextView.setText(teacherSubjectDetail.getSubjectCode());
        holder.subjectNameTextView.setText(teacherSubjectDetail.getSubjectName());
        holder.groupNameTextView.setText(String.valueOf(teacherSubjectDetail.getBranch()));
        //will be filled after wards
        holder.avgAttendanceTextView.setVisibility(View.INVISIBLE);

        holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.book));
    }


    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    class StudentSubjectViewHolder extends RecyclerView.ViewHolder {

        TextView subjectCodeTextView, subjectNameTextView, groupNameTextView, avgAttendanceTextView;
        ImageView imageView;

        public StudentSubjectViewHolder(View itemView) {
            super(itemView);

            subjectCodeTextView = itemView.findViewById(R.id.subjectCodeTextView);
            subjectNameTextView = itemView.findViewById(R.id.subjectNameTextView);
            groupNameTextView = itemView.findViewById(R.id.groupNameTextView);
            avgAttendanceTextView = itemView.findViewById(R.id.avgAttendanceTextView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

}

