package com.example.venetatodorova.contacts;

import android.graphics.Bitmap;

public class ContactsWrapper {
    Bitmap image;
    String name;
    String mobileNumber;

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

}
