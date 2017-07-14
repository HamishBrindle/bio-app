package com.biomap.application.bio_app.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.AnimationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class BeginActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private TextView name;
    private TextView mGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);

        Button mBegin = (Button) findViewById(R.id.button_begin);

        final Intent splashIntent = new Intent(this, AnimationActivity.class);
        name = (TextView) findViewById(R.id.user_name);
        mGreeting = (TextView) findViewById(R.id.greeting);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] fullname = dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Name").getValue().toString().split(" ");
                name.setText(fullname[0].substring(0, 1).toUpperCase() + fullname[0].substring(1));

                Calendar calender = Calendar.getInstance();

                if (6 < calender.get(Calendar.HOUR_OF_DAY) && calender.get(Calendar.HOUR_OF_DAY) < 12) {
                    mGreeting.setText(getString(R.string.good_morning_text));
                } else if (12 <= calender.get(Calendar.HOUR_OF_DAY) && calender.get(Calendar.HOUR_OF_DAY) < 17) {
                    mGreeting.setText(getString(R.string.good_afternoon_text));
                } else {
                    mGreeting.setText(getString(R.string.good_evening_text));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(splashIntent);

                // Make switching between activities blend via fade-in / fade-out
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                finish();
            }
        });

    }
}
