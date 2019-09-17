package com.example.ams;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Adapter class to bind the view, in order to display data in recycler View.
 */


public class TeacherDetailAdapter extends RecyclerView.Adapter<TeacherDetailAdapter.TeacherDetailViewHolder> {
    private Context mContext;
    private ArrayList<TeacherDetails> teacherList = new ArrayList<>();

    public TeacherDetailAdapter(@NonNull Context context, ArrayList<TeacherDetails> list){
        this.mContext = context;
        this.teacherList = list;
    }

    @NonNull
    @Override
    public TeacherDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.layout_teacher_verify, null);

        return new TeacherDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherDetailViewHolder holder, int position) {
        //getting the subject details of the specified position
        TeacherDetails teacherDetail = teacherList.get(position);

        //binding the data with the viewholder views
        holder.teacherNameTextView.setText(teacherDetail.getName());
        holder.teacherIdTextView.setText(teacherDetail.getTeacherId());
        //will be filled after wards
        //holder.avgAttendanceTextView.setText("");
    }


    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    class TeacherDetailViewHolder extends RecyclerView.ViewHolder {

        TextView teacherNameTextView, teacherIdTextView;
        ImageView imageView;

        public TeacherDetailViewHolder(View itemView) {
            super(itemView);

            teacherNameTextView = itemView.findViewById(R.id.teacherNameTextView);
            teacherIdTextView = itemView.findViewById(R.id.teacherIdTextView);

        }
    }

}
