package com.example.venetatodorova.contacts;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

class ContactsWrapper implements Parcelable {
    private Bitmap image;
    private String name;
    private String mobileNumbers;
    private String email;

    ContactsWrapper() {
    }

    private ContactsWrapper(Parcel in) {
        image = in.readParcelable(Bitmap.class.getClassLoader());
        name = in.readString();
        mobileNumbers = in.readString();
        email = in.readString();
    }

    public static final Creator<ContactsWrapper> CREATOR = new Creator<ContactsWrapper>() {
        @Override
        public ContactsWrapper createFromParcel(Parcel in) {
            return new ContactsWrapper(in);
        }

        @Override
        public ContactsWrapper[] newArray(int size) {
            return new ContactsWrapper[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(image, i);
        parcel.writeString(name);
        parcel.writeString(mobileNumbers);
        parcel.writeString(email);
    }
}
