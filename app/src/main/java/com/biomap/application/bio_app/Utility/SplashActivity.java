package com.biomap.application.bio_app.Utility;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.airbnb.lottie.LottieAnimationView;
import com.biomap.application.bio_app.Home.MainActivity;
import com.biomap.application.bio_app.R;

/**
 * Displays BioMap animation when application is first loaded.
 * <p>
 * Created by Hamish Brindle on 2017-07-10.
 */

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SpashActivity";
    private LottieAnimationView mLottieAnimationView;
    private Intent mainIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mLottieAnimationView = (LottieAnimationView) findViewById(R.id.animation_view);
        mainIntent = new Intent(this, MainActivity.class);
        mLottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "onAnimationStart: animation Started");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                startActivity(mainIntent);
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        Log.d(TAG, "onCreate: starting.");
    }

}
