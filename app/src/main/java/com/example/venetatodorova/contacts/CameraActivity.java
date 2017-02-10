package com.example.venetatodorova.contacts;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity implements CameraFragment.CaptureListener{

    private CameraFragment fragment;
    private String TAG = "CameraTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        fragment = (CameraFragment) getFragmentManager().findFragmentByTag(TAG);

        if (fragment == null) {
            android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
            fragment = CameraFragment.newInstance();
            transaction.replace(R.id.container,fragment,TAG).commit();
        }
    }

    @Override
    public void onCapture(byte[] capture) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", capture);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
