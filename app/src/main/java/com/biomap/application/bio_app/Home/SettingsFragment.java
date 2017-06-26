package com.biomap.application.bio_app.Home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.biomap.application.bio_app.Login.LoginActivity;
import com.biomap.application.bio_app.Login.ProfileActivity;
import com.biomap.application.bio_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by hamis on 2017-06-13.
 */

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private Button mSignOut;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private Button mUpdate;
    private Intent updateProfileIntent;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
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
                    Intent logOutIntent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(logOutIntent);
                    getActivity().finish();
                }
            }
        };
        updateProfileIntent = new Intent(getActivity(), ProfileActivity.class);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener.onAuthStateChanged(mAuth);
        mUpdate = (Button) view.findViewById(R.id.settings_profile_update);
        mSignOut = (Button) view.findViewById(R.id.settings_button_logout);
        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: signed out button");
                // Sign out the user.
                mAuth.signOut();

                // Check if user is signed out.
                // TODO: Preferably, we'd like to not call this manually.
                mAuthListener.onAuthStateChanged(mAuth);
            }
        });
        mUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(updateProfileIntent);
                getActivity().finish();

            }
        });
        return view;

    }

}
