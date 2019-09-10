package com.example.ams;

import androidx.annotation.Nullable;

public class TeacherSubjectDetail {
    public String subjectCode;
    public String branch;

    TeacherSubjectDetail(String subjectCode, String branch){
        this.subjectCode = subjectCode;
        this.branch = branch;
    }

    TeacherSubjectDetail(){

    }

    public void setSubjectCode(String subjectCode){
        this.subjectCode = subjectCode;
    }

    public void setBranch(String branch){
        this.branch = branch;
    }

    public String getSubjectCode(){
        return this.subjectCode;
    }
    public String getBranch(){
        return this.branch;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!(obj instanceof TeacherSubjectDetail)){
            return false;
        }
        TeacherSubjectDetail teacherSubjectDetail = (TeacherSubjectDetail) obj;
        return teacherSubjectDetail.subjectCode.equals(subjectCode) && teacherSubjectDetail.branch.equals(branch);
    }
}