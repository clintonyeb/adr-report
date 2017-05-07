package com.clinton.adrreport.activities;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.clinton.adrreport.R;
import com.clinton.adrreport.fragments.DetailFragment;
import com.clinton.adrreport.fragments.MainActivityFragment;
import com.clinton.adrreport.utils.AndroidPermission;

import static com.clinton.adrreport.utils.Helpers.SETTINGS_FILE;

public class MainActivity extends AppCompatActivity
        implements MainActivityFragment.onStartButtonClick, DetailFragment.OnDetailsFragmentInter {

    private AndroidPermission mPermissions;
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    String patientName = null;
    String patientId = null;
    Fragment mCurrentFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPermissions = new AndroidPermission(this,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        checkAuthenticated();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if(findViewById(R.id.fragment) != null){
            if (savedInstanceState != null) return;

            getSupportFragmentManager().beginTransaction().add(R.id.fragment, new MainActivityFragment()).commit();
        }

    }

    void checkPermission() {
        if (mPermissions.checkPermissions()) {
            startCamera();
        } else {
            Log.i("Activity", "Some needed permissions are missing. Requesting them.");
            mPermissions.requestPermissions(PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.i("Activity", "onRequestPermissionsResult");

        if (mPermissions.areAllRequiredPermissionsGranted(permissions, grantResults)) {
            startCamera();
        } else {
            onInsufficientPermissions();
        }
    }

    private void onInsufficientPermissions() {
        //your implementation to show the user that the required permissions have not been granted
    }

    private void checkAuthenticated(){
        SharedPreferences preferences = this.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if(token == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.home:
                onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment instanceof DetailFragment && DetailFragment.DETAIL_ACTIVITY_ACTIVE){
            getDialogBox().show();
        }

        else{
            super.onBackPressed();
        }
    }

    private AlertDialog getDialogBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Images taken will not be sent?")
                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mCurrentFragment instanceof DetailFragment) {
                            ((DetailFragment) mCurrentFragment).deleteUnusedPictures();
                        }
                       MainActivity.super.onBackPressed();
                    }
                });
        return builder.create();
    }

    @Override
    public void onButtonClick(String patientName, String patientId) {
        this.patientName = patientName;
        this.patientId = patientId;
        checkPermission();
    }

    void startCamera() {
        if (patientName != null && patientId != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mCurrentFragment = DetailFragment.newInstance(patientName, patientId);
            transaction.replace(R.id.fragment, mCurrentFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

    }

    @Override
    public void onFormSubmitted(boolean val) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mCurrentFragment = new MainActivityFragment();
        transaction.replace(R.id.fragment, mCurrentFragment);
        transaction.commit();
        Toast.makeText(this, "Uploading images", Toast.LENGTH_SHORT).show();
    }
}
