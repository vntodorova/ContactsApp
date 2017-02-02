package com.example.venetatodorova.contacts;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ListView;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private ContactsAdapter adapter;
    private ListView listView;

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ContactsAdapter(getContext(),null,0);
        getLoaderManager().initLoader(0,null,this);
        listView = (ListView) getActivity().findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projectionFields = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI,};
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                ContactsContract.Contacts.CONTENT_URI,
                projectionFields,
                null,
                null,
                null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //List<ContactsWrapper> contacts = contactsFromCursor(cursor);
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }


    private List<ContactsWrapper> contactsFromCursor(Cursor cursor) {
        List<ContactsWrapper> contacts = new ArrayList<>();
        ContactsWrapper contact = new ContactsWrapper();

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                contact.setName(name);
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contact.setMobileNumber(number);
                Uri imageUri = Uri.parse(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)));
                InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getActivity().getBaseContext().getContentResolver(), imageUri);
                Bitmap bm = BitmapFactory.decodeStream(input);
                contact.setImage(bm);
            } while (cursor.moveToNext());
        }

        return contacts;
    }
}
