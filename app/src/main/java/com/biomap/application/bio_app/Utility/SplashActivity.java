package com.biomap.application.bio_app.Utility;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.biomap.application.bio_app.R;

/**
 * Displays BioMap animation when application is first loaded.
 *
 * Created by Hamish Brindle on 2017-07-10.
 */

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SpashActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        Log.d(TAG, "onCreate: starting.");
    }

}
