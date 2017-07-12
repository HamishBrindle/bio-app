package com.biomap.application.bio_app.Utility;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.biomap.application.bio_app.Home.MainActivity;
import com.biomap.application.bio_app.R;

/**
 * Displays BioMap animation when application is first loaded.
 * <p>
 * Created by Hamish Brindle on 2017-07-10.
 */

public class AnimationActivity extends AppCompatActivity {

    private static final String TAG = "SpashActivity";
    private Intent mainIntent;
    private TextView mGreeting;
    private AlphaAnimation anim;
    private Typeface font;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);

        // Define fade-in animation for greeting
        mGreeting = (TextView) findViewById(R.id.greeting_view);
        mGreeting.setVisibility(View.INVISIBLE);

        //Setting the font of the description text
        font = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Book.otf");
        mGreeting.setTypeface(font);

        // Parameterize animation for the TextView greeting
        anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1000);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mGreeting.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(mainIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}

        });

        LottieAnimationView lottieAnimationView = (LottieAnimationView) findViewById(R.id.animation_view);
        mainIntent = new Intent(this, MainActivity.class);
        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "onAnimationStart: animation Started");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mGreeting.startAnimation(anim);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}

        });

    }

}
