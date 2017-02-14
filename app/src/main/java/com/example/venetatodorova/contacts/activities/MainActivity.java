package com.example.venetatodorova.contacts.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.venetatodorova.contacts.adapters.ContactsAdapter;
import com.example.venetatodorova.contacts.fragments.ContactsFragment;
import com.example.venetatodorova.contacts.R;

import static com.example.venetatodorova.contacts.fragments.ContactsFragment.DATA;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final String FRAGMENT_TAG = "FragmentTag";
    ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ContactsFragment contactsFragment = ContactsFragment.newInstance();
        fragmentTransaction.add(android.R.id.content, contactsFragment, FRAGMENT_TAG).commit();

        listview = (ListView) findViewById(R.id.listView);
        listview.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Cursor cursor = ((ContactsAdapter) adapterView.getAdapter()).getCursor();
        cursor.moveToPosition(i);
        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        Intent intent = new Intent(this, ContactInfoActivity.class);
        intent.putExtra(DATA, id);
        startActivity(intent);
    }
}
