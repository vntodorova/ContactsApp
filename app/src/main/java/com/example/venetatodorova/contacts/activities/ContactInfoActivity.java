package com.example.venetatodorova.contacts.activities;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.venetatodorova.contacts.fragments.CameraFragment;
import com.example.venetatodorova.contacts.fragments.ContactsFragment;
import com.example.venetatodorova.contacts.models.ContactsWrapper;
import com.example.venetatodorova.contacts.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ContactInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE = 1;
    private ImageView imageView;
    private ContactsWrapper contact;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_info_activity);
        Bundle bundle = getIntent().getExtras();
        String contactID = bundle.getString(ContactsFragment.DATA);
        contact = getContactFromId(contactID);
        initUI();
    }

    private void initUI() {
        TextView name = (TextView) findViewById(R.id.contactName);
        name.setText(contact.getName());
        TextView mobileNumbers = (TextView) findViewById(R.id.contactPhoneNumbers);
        mobileNumbers.setText(contact.getMobileNumbers());
        imageView = (ImageView) findViewById(R.id.contactImage);
        imageView.setImageBitmap(contact.getImage());
        TextView email = (TextView) findViewById(R.id.contactEmail);
        email.setText(contact.getEmail());
        imageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                byte[] bytes = fileToByteArray(new File(CameraFragment.PATH));
                setContactImage(bytes, contact.getID());
            }
        }
    }

    private ContactsWrapper getContactFromId(String id) {
        ContactsWrapper contact = new ContactsWrapper();
        contact.setName(getContactName(id));
        contact.setMobileNumber(getContactMobileNumber(id));
        contact.setImage(getContactImage(Long.valueOf(id)));
        contact.setEmail(getContactEmail(id));
        contact.setID(id);
        return contact;
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
        Cursor photoCursor = getContentResolver().query(getContactImageUri(id),
                new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (photoCursor == null) {
            return null;
        } else {
            if (photoCursor.moveToFirst()) {
                byte[] data = photoCursor.getBlob(0);
                imageBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
            } else {
                imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.contact);
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

    private byte[] fileToByteArray(File file) {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try (BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file))) {
            buf.read(bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private void setContactImage(byte[] image, String contactID) {
        Uri rawContactUri = getContactUri(contactID);
        ContentResolver cr = getContentResolver();
        int photoRow = -1;
        long rawContactId = -1;
        Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.RAW_CONTACT_ID + " == " +
                        ContentUris.parseId(rawContactUri) + " AND " + ContactsContract.Contacts.Data.MIMETYPE + "=='" +
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'",
                null,
                null);
        if (cursor != null && cursor.moveToFirst()) {
            photoRow = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Data._ID));
            rawContactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
            cursor.close();
        }
        image = compressImage(image);
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data._ID + " = ?", new String[]{Integer.toString(photoRow)})
                .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.Data.DATA15, image)
                .build());

        try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(getContactImage(Long.valueOf(contactID)));
    }

    private byte[] compressImage(byte[] image) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap preview_bitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        preview_bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private Uri getContactUri(String contactID) {
        ContentResolver cr = getContentResolver();
        Uri rawContactUri = null;
        Cursor rawContactCursor = cr.query(ContactsContract.RawContacts.CONTENT_URI, new String[]{ContactsContract.RawContacts._ID}, ContactsContract.RawContacts.CONTACT_ID + " = " + contactID, null, null);
        if (!rawContactCursor.isAfterLast()) {
            rawContactCursor.moveToFirst();
            rawContactUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendPath("" + rawContactCursor.getLong(0)).build();
        }
        rawContactCursor.close();
        return rawContactUri;
    }

    public void openInGallery(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri imageUri = getContactImageUri(Long.valueOf(contact.getID()));
        intent.setDataAndType(imageUri,"image/*");
        startActivity(intent);
    }

    private Uri getContactImageUri(Long id) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        return Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }
}
