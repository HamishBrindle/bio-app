package com.biomap.application.bio_app.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.CustomFontTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private String[] name;
    private Button mSignOutBtn;
    private Button mUpdateBtn;
    CustomFontTextView mProfileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mProfileName = (CustomFontTextView) findViewById(R.id.profile_name);
        setupFirebase();
        setUpButtons();

    }

    private void setUpButtons() {
        mSignOutBtn = (Button) findViewById(R.id.account_signOut_button);
        mUpdateBtn = (Button) findViewById(R.id.account_update_profile);

        mSignOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                setupFirebase();
                Intent signOutIntent = new Intent(getApplicationContext(), LoginRegisterActivity.class);
                signOutIntent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(signOutIntent);
                finish();
            }
        });

        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileUpdateIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(profileUpdateIntent);
                finish();
            }
        });

    }


    private void setupFirebase() {

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();

        FirebaseAuth.AuthStateListener mAuthListener = firebaseAuth -> {

            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged.Main:signed_in:" + user.getUid());
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String fullName = dataSnapshot.child("Users").child(mAuth.getCurrentUser().getUid()).child("Name").getValue().toString();
                        name = fullName.split(" ");
                        mProfileName.setText(name[0]);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged.Main:signed_out");
                // Create the logout activity intent.
                Intent register_login_intent = new Intent(this, LoginRegisterActivity.class);
                register_login_intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(register_login_intent);
                finish();

            }
        };

        // Get the user's authentication credentials and check if signed in or not.
        mAuth = FirebaseAuth.getInstance();
        mAuthListener.onAuthStateChanged(mAuth);

    }
}
