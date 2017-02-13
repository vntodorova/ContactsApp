package com.example.venetatodorova.contacts.models;

import android.graphics.Bitmap;

public class ContactsWrapper{
    private Bitmap image;
    private String name;
    private String mobileNumbers;
    private String email;

    public ContactsWrapper() {
    }

    public Bitmap getImage() {
        return image;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMobileNumber(String mobileNumbers) {
        this.mobileNumbers = mobileNumbers;
    }

    public String getMobileNumbers() {
        return mobileNumbers;
    }

}
