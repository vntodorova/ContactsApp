package com.example.venetatodorova.contacts;

import android.graphics.Bitmap;

class ContactsWrapper{
    private Bitmap image;
    private String name;
    private String mobileNumbers;
    private String email;

    ContactsWrapper() {
    }

    Bitmap getImage() {
        return image;
    }

    void setEmail(String email) {
        this.email = email;
    }

    String getEmail() {
        return email;
    }

    void setImage(Bitmap image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    void setMobileNumber(String mobileNumbers) {
        this.mobileNumbers = mobileNumbers;
    }

    String getMobileNumbers() {
        return mobileNumbers;
    }

}
