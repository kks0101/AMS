package com.example.ams;

import androidx.annotation.Nullable;

public class TeacherDetails {
    private String name, emailId, teacherId, phoneNo, verified;
    TeacherDetails(String name, String emailId, String teacherId, String phoneNo, String verified){
        this.name = name;
        this.emailId = emailId;
        this.teacherId = teacherId;
        this.phoneNo = phoneNo;
        this.verified = verified;
    }

    public String getName(){
        return this.name;
    }

    public String getEmailId(){
        return this.emailId;
    }
    public String getTeacherId(){
        return this.teacherId;
    }
    public  String getPhoneNo(){
        return this.phoneNo;
    }
    public String getVerified(){
        return this.verified;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!(obj instanceof TeacherSubjectDetail)){
            return false;
        }
        TeacherDetails teacherDetails = (TeacherDetails)obj;
        return teacherDetails.teacherId.equals(teacherId) ;
    }
}
