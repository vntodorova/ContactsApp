package com.example.venetatodorova.contacts.models;

import android.graphics.Bitmap;

public class ContactsWrapper{
    private Bitmap image;
    private String name;
    private String mobileNumbers;
    private String email;
    private String ID;

    public ContactsWrapper() {
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMobileNumber(String mobileNumbers) {
        this.mobileNumbers = mobileNumbers;
    }

    public String getMobileNumbers() {
        return mobileNumbers;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }
}
