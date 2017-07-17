package com.biomap.application.bio_app.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.biomap.application.bio_app.Login.LoginRegisterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

/**
 * Handles the Firebase login's / logout's, as well as adding and removing data.
 * <p>
 * Created by Hamish Brindle on 2017-07-10.
 */

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public FirebaseHelper() {
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public void SignedOutListener(final Context context, final Activity activity) {
        final Intent register_login_intent = new Intent(context, LoginRegisterActivity.class);

        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged.Main:signed_in:" + user.getUid());

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged.Main:signed_out");

                    // Create the logout activity intent.
                    register_login_intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(register_login_intent);
                    activity.finish();
                }
            }
        };
    }

    public void callListener() {
        mAuthListener.onAuthStateChanged(mAuth);
    }



}