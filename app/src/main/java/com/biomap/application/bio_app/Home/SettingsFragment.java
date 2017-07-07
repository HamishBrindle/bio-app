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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by hamis on 2017-06-13.
 */

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private Button mSignOut;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private Button mUpdate;
    private Button mAddtoDB;

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
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
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
                mAuthListener.onAuthStateChanged(mAuth);
            }
        });
        Integer[] numbers = new Integer[63];
        for (int i = 0; i < 63; i++) {
            numbers[i] = i;
        }
        final List<Integer> numbersList = Arrays.asList(numbers);
        mUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(updateProfileIntent);
                getActivity().finish();

            }
        });
        mAddtoDB = (Button) view.findViewById(R.id.array_to_db_btn);
        mAddtoDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: addtoDB");
                Calendar cal = Calendar.getInstance();
                String year = String.valueOf(cal.get(Calendar.YEAR));
                String month = String.valueOf(cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
                Log.d(TAG, "onClick: " + cal.get(Calendar.MONTH));
                String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
                String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));

                Log.d(TAG, "onClick: " + cal.get(Calendar.YEAR));
                myRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("History").child(year).child("" + month + "//" + day).child(hour).setValue(numbersList);
            }
        });
        return view;

    }

}
