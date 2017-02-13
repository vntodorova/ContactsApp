package com.example.venetatodorova.contacts.models;

public class DrawerElement {
    public static final int FLASH_CHECKBOX = 1;
    public static final int ZOOM_SEEKBAR = 2;
    public static final int EXPOSURE_SEEKBAR = 3;

    private int type;

    public DrawerElement(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
