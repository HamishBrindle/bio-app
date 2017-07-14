package com.biomap.application.bio_app.Login;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.biomap.application.bio_app.Home.MainActivity;
import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.CustomFontsLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginRegisterActivity extends AppCompatActivity {
    private static final String TAG = "LoginRegisterActivity";
    TextView mDescriptionText;
    Typeface font;
    Button mRegisterButton;
    Button mSignInButton;
    Intent registerIntent;
    Intent signInIntent;
    Intent homeIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        //Initializing all items
        mDescriptionText = (TextView) findViewById(R.id.login_register_description_text);
        mRegisterButton = (Button) findViewById(R.id.register_button);
        mSignInButton = (Button) findViewById(R.id.sign_in_button);
        signInIntent = new Intent(this, LoginActivity.class);
        registerIntent = new Intent(this, RegisterActivity.class);
        homeIntent = new Intent(this, MainActivity.class);

        FirebaseAuth.AuthStateListener mAuthListener;
        FirebaseAuth mAuth;

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: User is signed in");
                    startActivity(homeIntent);
                    // Make switching between activities blend via fade-in / fade-out
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else {
                    // User is signed out


                }
            }
        };

        //Setting the font of the description text;
        mDescriptionText.setTypeface(CustomFontsLoader.getTypeface(this, CustomFontsLoader.GOTHAM_BOOK));
        mRegisterButton.setTypeface(CustomFontsLoader.getTypeface(this, CustomFontsLoader.GOTHAM_BOLD));
        mSignInButton.setTypeface(CustomFontsLoader.getTypeface(this, CustomFontsLoader.GOTHAM_BOLD));

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(registerIntent);

                // Make switching between activities blend via fade-in / fade-out
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

//                finish();
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(signInIntent);

                // Make switching between activities blend via fade-in / fade-out
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

//                finish();
            }
        });
    }
}
