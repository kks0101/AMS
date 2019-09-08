package com.example.ams;

public class TeacherSubjectDetail {
    private String subjectCode;
    private String branch;

    TeacherSubjectDetail(String subjectCode, String branch){
        this.subjectCode = subjectCode;
        this.branch = branch;
    }

    TeacherSubjectDetail(){

    }

    void setSubjectCode(String subjectCode){
        this.subjectCode = subjectCode;
    }

    void setBranch(String branch){
        this.branch = branch;
    }

    String getSubjectCode(){
        return this.subjectCode;
    }
    String getBranch(){
        return this.branch;
    }
}
