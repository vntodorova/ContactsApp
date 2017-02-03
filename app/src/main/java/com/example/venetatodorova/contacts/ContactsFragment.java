package com.example.venetatodorova.contacts;

import android.content.ContentResolver;
import android.content.Intent;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    public static final String DATA = "Data";
    public static final int CONTACTS_REQUEST_CODE = 1;
    private ContactsAdapter adapter;
    private ListView listView;

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = new ContactsAdapter(getContext(), null, 0);
        getLoaderManager().initLoader(0, null, this);
        listView = (ListView) getActivity().findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projectionFields = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI,
        };
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                ContactsContract.Contacts.CONTENT_URI,
                projectionFields,
                null,
                null,
                "display_name ASC");

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Cursor cursor = ((ContactsAdapter) adapterView.getAdapter()).getCursor();
        cursor.moveToPosition(i);

        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        Intent intent = new Intent(getActivity(), ContactInfoActivity.class);
        intent.putExtra(DATA, id);
        startActivity(intent);

    }

}
