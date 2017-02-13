package com.example.venetatodorova.contacts.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.venetatodorova.contacts.fragments.CameraFragment;
import com.example.venetatodorova.contacts.fragments.ContactsFragment;
import com.example.venetatodorova.contacts.models.ContactsWrapper;
import com.example.venetatodorova.contacts.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ContactInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 1;
    private ImageView imageView;
    private String contactID;
    private ContactsWrapper contact;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Explode());
        setContentView(R.layout.contact_info_activity);
        Bundle bundle = getIntent().getExtras();
        contactID = bundle.getString(ContactsFragment.DATA);
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

    private ContactsWrapper getContactFromId(String id) {
        ContactsWrapper contact = new ContactsWrapper();
        contact.setName(getContactName(id));
        contact.setMobileNumber(getContactMobileNumber(id));
        contact.setImage(getCircleBitmap(getContactImage(Long.valueOf(id))));
        contact.setEmail(getContactEmail(id));
        return contact;
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);

        Canvas c = new Canvas(circleBitmap);
        c.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        return circleBitmap;
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

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, REQUEST_CODE, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                //Bundle bundle = data.getExtras();
                //String imagePath = bundle.getString(CameraActivity.EXTRA);
                if (CameraFragment.PATH != null) {
                    byte[] bytes = fileToByteArray(new File(CameraFragment.PATH));
                    setContactImage(bytes);
                }
            }
        }
    }

    private byte[] fileToByteArray(File file) {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try(BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file))) {
            buf.read(bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private void setContactImage(byte[] image) {
        Uri rawContactUri = getPictureUri();
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

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data._ID + " = ?", new String[] {Integer.toString(photoRow)})
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
        imageView.setImageBitmap(getCircleBitmap(getContactImage(Long.valueOf(contactID))));
    }

    private Uri getPictureUri() {
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
}