package com.example.venetatodorova.contacts;
import android.content.Intent;
import android.database.Cursor;
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

public class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String DATA = "Data";
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

}
