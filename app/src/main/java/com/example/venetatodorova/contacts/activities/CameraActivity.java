package com.example.venetatodorova.contacts.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.util.Log;
import android.view.Window;

import com.example.venetatodorova.contacts.fragments.CameraFragment;
import com.example.venetatodorova.contacts.R;

public class CameraActivity extends AppCompatActivity implements CameraFragment.CaptureListener {

    private CameraFragment fragment;
    private static String TAG = "CameraTag";
    public static String EXTRA = "Result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().setEnterTransition(new Slide());
        setContentView(R.layout.activity_camera);
        fragment = (CameraFragment) getFragmentManager().findFragmentByTag(TAG);

        if (fragment == null) {
            android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
            fragment = CameraFragment.newInstance();
            transaction.replace(R.id.container,fragment,TAG).commit();
        }
    }

    @Override
    public void onCapture(String path) {
        Intent returnIntent = new Intent();
        //returnIntent.putExtra(EXTRA, path);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
