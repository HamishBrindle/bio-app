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


public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private TextView mUlcersText;
    private TextView mGreeting;
    private String name;
    private boolean ulcers;
    private Intent setUpIntent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        setUpIntent = new Intent(getActivity(), ProfileActivity.class);
        //Notificatonis
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Data changed");
                Boolean setUp = (Boolean) dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("SetUp").getValue();
                if (!setUp) {
                    startActivity(setUpIntent);
                    getActivity().finish();
                } else {
                    name = dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("FirstName").getValue().toString();
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    ulcers = (boolean) (dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Ulcers").getValue());

                    Calendar calender = Calendar.getInstance();
                    Log.d(TAG, "onDataChange: " + calender.get(Calendar.HOUR_OF_DAY));
                    if (6 < calender.get(Calendar.HOUR_OF_DAY) && calender.get(Calendar.HOUR_OF_DAY) < 12) {
                        mGreeting.setText(getString(R.string.good_morning_text, name));
                    } else if (12 <= calender.get(Calendar.HOUR_OF_DAY) && calender.get(Calendar.HOUR_OF_DAY) < 17) {
                        mGreeting.setText(getString(R.string.good_afternoon_text, name));
                    } else if (17 <= calender.get(Calendar.HOUR_OF_DAY) && calender.get(Calendar.HOUR_OF_DAY) < 20) {
                        mGreeting.setText(getString(R.string.good_evening_text, name));
                    } else {
                        mGreeting.setText(getString(R.string.good_night_text, name));
                    }

                    if (ulcers) {
                        mUlcersText.setText("You have ulcers");
                    } else {
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
