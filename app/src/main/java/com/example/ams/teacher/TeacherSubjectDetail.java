package com.example.ams.teacher;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * This Class is used to encapsulate Teacher Subject Detail which is used in recyclerView.
 *
 * It implements Serializable. Implementing Serializable allows us to pass Class object via Intent
 */

public class TeacherSubjectDetail implements Serializable {
    private String subjectCode;
    private String subjectName;
    private String branch;

    public TeacherSubjectDetail(String subjectCode, String branch, String subjectName){
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
