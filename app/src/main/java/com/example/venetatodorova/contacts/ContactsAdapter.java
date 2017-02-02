package com.example.venetatodorova.contacts;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ContactsAdapter extends CursorAdapter {

    private LayoutInflater layoutInflater;

    public ContactsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.contacts_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        TextView name_text = (TextView) view.findViewById(R.id.contact_name);
        name_text.setText(name);


        Uri imageUri = Uri.parse(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)));
        ImageView imageView = (ImageView) view.findViewById(R.id.contact_image);
        Bitmap image;
        try {
            InputStream is = context.getContentResolver().openInputStream(imageUri);
            image = BitmapFactory.decodeStream(is);
            imageView.setImageBitmap(image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
