package com.example.ams.others;

/**
 * This class is used to encapsulate the profile image data, that is to be stored on Firebase.
 * It is used, because it is very convenient to write data to Firebase Database using class objects.
 * Firebase itself converts it into JSON form to store in JSON tree.
 */


public class Upload {
    private String mName;
    private String mImageUrl;

    public Upload() {
        //empty constructor needed
    }

    public Upload(String name, String imageUrl) {
        if (name.trim().equals("")) {
            name = "No Name";
        }

        mName = name;
        mImageUrl = imageUrl;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }
}