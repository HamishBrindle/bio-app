package com.biomap.application.bio_app.Home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.biomap.application.bio_app.Login.ProfileActivity;
import com.biomap.application.bio_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

/**
 * Created by hamish on 2017-06-13.
 */

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private TextView mUlcersText;
    private TextView mGreeting;
    private DatabaseReference myRef;
    private String name;
    private boolean ulcers;
    private Intent setUpIntent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();


        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mGreeting = (TextView) getView().findViewById(R.id.home_greeting);
        mUlcersText = (TextView) getView().findViewById(R.id.home_greeting_ulcers);
        setUpIntent = new Intent(getActivity(), ProfileActivity.class);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Data changed");
                Boolean setUp = new Boolean((Boolean) dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("SetUp").getValue());
                Log.d(TAG, "onDataChange: " + setUp);
                if (!setUp.booleanValue()) {
                    startActivity(setUpIntent);
                    getActivity().finish();
                } else {
                    name = dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("FirstName").getValue().toString();
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    ulcers = (boolean) (dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Ulcers").getValue());

                    Log.d(TAG, "onDataChange:" + name);
                    Log.d(TAG, "onDataChange: " + ulcers);

                    Calendar calender = Calendar.getInstance();
                    if (6 < calender.get(Calendar.HOUR_OF_DAY) && calender.get(Calendar.HOUR_OF_DAY) < 12) {
                        mGreeting.setText(getString(R.string.good_morning_text) + name);
                    } else if (12 < calender.get(Calendar.HOUR_OF_DAY) && calender.get(Calendar.HOUR_OF_DAY) < 17) {
                        mGreeting.setText(getString(R.string.good_afternoon_text) + name);
                    } else if (17 < calender.get(Calendar.HOUR_OF_DAY) && calender.get(Calendar.HOUR_OF_DAY) < 20) {
                        mGreeting.setText(getString(R.string.good_evening_text) + name);
                    } else {
                        mGreeting.setText(getString(R.string.good_night_text) + name);
                    }

                    if (ulcers) {
                        Log.d(TAG, "onDataChange: trueB");
                        mUlcersText.setText("You have ulcers");
                    } else {
                        Log.d(TAG, "onDataChange: falseB");

                        mUlcersText.setText("You Don't have ulcers");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
