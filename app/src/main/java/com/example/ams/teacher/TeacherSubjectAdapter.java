package com.example.ams.teacher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ams.R;

import java.util.ArrayList;

/**
 * This provides adapter to bind the view to the list Layout. Every Recycler View implementation requires
 * a adapter that defines the View Holder to display the data in list form.
 */

public class TeacherSubjectAdapter extends RecyclerView.Adapter<TeacherSubjectAdapter.TeacherSubjectViewHolder> {
    private Context mContext;
    private ArrayList<TeacherSubjectDetail> subjectList = new ArrayList<>();

    public TeacherSubjectAdapter(@NonNull Context context, ArrayList<TeacherSubjectDetail> list){
        this.mContext = context;
        this.subjectList = list;
    }

    @NonNull
    @Override
    public TeacherSubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        //setting the appropriate layout
        View view = inflater.inflate(R.layout.layout_teacher_subject, null);

        return new TeacherSubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherSubjectViewHolder holder, int position) {
        //getting the subject details of the specified position
        TeacherSubjectDetail teacherSubjectDetail = subjectList.get(position);

        //binding the data with the viewholder views
        holder.subjectCodeTextView.setText(teacherSubjectDetail.getSubjectCode());
        holder.subjectNameTextView.setText(teacherSubjectDetail.getSubjectName());
        holder.groupNameTextView.setText(String.valueOf(teacherSubjectDetail.getBranch()));
        //will be filled after wards
        //holder.avgAttendanceTextView.setText("");

        holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.semester));
    }


    @Override
    public int getItemCount() {
        if(subjectList!=null)
        return subjectList.size();
        return 0;
    }

    class TeacherSubjectViewHolder extends RecyclerView.ViewHolder {

        TextView subjectCodeTextView, subjectNameTextView, groupNameTextView, avgAttendanceTextView;
        ImageView imageView;

        public TeacherSubjectViewHolder(View itemView) {
            super(itemView);

            subjectCodeTextView = itemView.findViewById(R.id.subjectCodeTextView);
            subjectNameTextView = itemView.findViewById(R.id.subjectNameTextView);
            groupNameTextView = itemView.findViewById(R.id.groupNameTextView);
            avgAttendanceTextView = itemView.findViewById(R.id.avgAttendanceTextView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

}
