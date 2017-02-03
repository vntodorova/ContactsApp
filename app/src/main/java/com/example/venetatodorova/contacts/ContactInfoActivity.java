package com.example.venetatodorova.contacts;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.ByteArrayInputStream;

public class ContactInfoActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_info_activity);
        Bundle bundle = getIntent().getExtras();
        String id = bundle.getString(ContactsFragment.DATA);

        TextView name = (TextView) findViewById(R.id.contactName);
        name.setText(getContactName(id));

        TextView mobileNumbers = (TextView) findViewById(R.id.contactPhoneNumbers);
        mobileNumbers.setText(getContactMobileNumber(id));

        ImageView image = (ImageView) findViewById(R.id.contactImage);
        image.setImageBitmap(getContactImage(Long.valueOf(id)));

        TextView email = (TextView) findViewById(R.id.contactEmail);
        email.setText(getContactEmail(id));

    }

    private String getContactName(String id) {
        String contactName = null;
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(
                ContactsContract.Contacts.CONTENT_URI, null,
                ContactsContract.Contacts._ID + "=" + id, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        if (cursor != null) {
            cursor.close();
        }
        return contactName;
    }

    private String getContactMobileNumber(String id) {
        StringBuilder numbers = new StringBuilder();
        ContentResolver cr = getContentResolver();
        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
        if (phones != null && phones.moveToFirst()) {
            do {
                String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                numbers.append(number).append("\n");
            } while (phones.moveToNext());
        }
        if (phones != null) {
            phones.close();
        }
        return numbers.toString();
    }

    private Bitmap getContactImage(Long id) {
        Bitmap imageBitmap;
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor photoCursor = getContentResolver().query(photoUri,
                new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (photoCursor == null) {
            return null;
        } else {
            if (photoCursor.moveToFirst()) {
                byte[] data = photoCursor.getBlob(0);
                imageBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
            } else {
                imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thumbnail);
            }
        }
        photoCursor.close();
        return imageBitmap;
    }

    private String getContactEmail(String id) {
        StringBuilder emails = new StringBuilder();
        ContentResolver cr = getContentResolver();
        Cursor emailsCursor = cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + id, null, null);
        if (emailsCursor != null && emailsCursor.moveToFirst()) {
            do {
                String email = emailsCursor.getString(emailsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                emails.append(email).append("\n");
            } while (emailsCursor.moveToNext());
        }
        if (emailsCursor != null) {
            emailsCursor.close();
        }
        return emails.toString();
    }

}
