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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        this.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_right);
        String TAG = "CameraTag";
        CameraFragment fragment = (CameraFragment) getFragmentManager().findFragmentByTag(TAG);
        if (fragment == null) {
            android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
            fragment = CameraFragment.newInstance();
            transaction.replace(R.id.container, fragment, TAG).commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_left);
    }

    @Override
    public void onCapture(String path) {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
        this.overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_left);
    }
}
