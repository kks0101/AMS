package com.example.ams.teacher;

import androidx.annotation.Nullable;

public class ShortAttendanceDetail {
    String name, emailId;
    double percent;

    public ShortAttendanceDetail(String name, String emailId, double percent){
        this.name = name;
        this.emailId = emailId;
        this.percent = percent;
    }
    ShortAttendanceDetail(){

    }
    public String getName(){
        return this.name;
    }
    public String getEmailId(){
        return this.emailId;
    }
    public double getPercent(){
        return this.percent;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setEmailId(String emailId){
        this.emailId = emailId;
    }
    public void setPercent(float percent){
        this.percent = percent;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!(obj instanceof TeacherSubjectDetail)){
            return false;
        }
        ShortAttendanceDetail shortAttendanceDetail = (ShortAttendanceDetail)obj;
        return shortAttendanceDetail.name.equals(name) && shortAttendanceDetail.emailId.equals(emailId) &&
                shortAttendanceDetail.percent == percent;
    }
}
