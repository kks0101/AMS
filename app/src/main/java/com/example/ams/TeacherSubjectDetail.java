package com.example.ams;

import androidx.annotation.Nullable;

import java.io.Serializable;

//implements Serializable, inorder to pass the object of this class directly through intent
public class TeacherSubjectDetail implements Serializable {
    public String subjectCode;
    public String subjectName;
    public String branch;

    TeacherSubjectDetail(String subjectCode, String branch, String subjectName){
        this.subjectCode = subjectCode;
        this.branch = branch;
        this.subjectName = subjectName;
    }

    TeacherSubjectDetail(){

    }
    public void setSubjectName(String subjectName){
        this.subjectName = subjectName;
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
    public String getSubjectName(){
        return this.subjectName;
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
