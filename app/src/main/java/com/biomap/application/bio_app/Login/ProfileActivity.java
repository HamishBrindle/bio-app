package com.biomap.application.bio_app.Login;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.biomap.application.bio_app.Home.MainActivity;
import com.biomap.application.bio_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A register screen that offers login via email/password.
 */
public class ProfileActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final String TAG = "RegisterActivity";

    // UI references.
    private TextView mFirstNameView;
    private TextView mLastNameView;
    private String firstName;
    private String lastName;
    private Boolean ulcersDBCheck;

    private Button mContinueButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Intent mainIntent;
    private Intent registerIntent;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private CheckBox mUlcersCheck;
    private boolean ulcers;
    private Boolean alreadySetUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Firebase stuff
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        //Views
        mFirstNameView = (TextView) findViewById(R.id.profile_first_name);
        mLastNameView = (TextView) findViewById(R.id.profike_last_name);
        mContinueButton = (Button) findViewById(R.id.profile_continue_button);
        mUlcersCheck = (CheckBox) findViewById(R.id.checkBoxUlcers);


        //Intents
        registerIntent = new Intent(this, RegisterActivity.class);
        mainIntent = new Intent(this, MainActivity.class);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User is signed in
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("Users").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                alreadySetUp = new Boolean((Boolean) dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("SetUp").getValue());
                                if (alreadySetUp.booleanValue()) {
                                    firstName = dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("FirstName").getValue().toString();
                                    lastName = dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("LastName").getValue().toString();
                                    ulcersDBCheck = (Boolean) dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Ulcers").getValue();
                                    mFirstNameView.setText(firstName);
                                    mLastNameView.setText(lastName);
                                    if (ulcersDBCheck) {
                                        mUlcersCheck.setChecked(true);
                                    } else {
                                        mUlcersCheck.setChecked(false);
                                    }

                                }
                            } else {
                                myRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Email").setValue(mAuth.getCurrentUser().getEmail());

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    //User is NOT signed in;
                    startActivity(registerIntent);
                    finish();
                }
            }
        };


        mUlcersCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "onCheckedChanged: Checked");
                    ulcers = true;
                } else {
                    Log.d(TAG, "onCheckedChanged: Unchecked");
                    ulcers = false;
                }
            }
        });

        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Verify Ran");
                verify();
            }
        });
    }

    private void verify() {

        Log.d(TAG, "verify: in verify");
        // Reset errors.
        mFirstNameView.setError(null);
        mLastNameView.setError(null);

        // Store values at the time of the login attempt.
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        if (TextUtils.isEmpty(firstName)) {
            Log.d(TAG, "verify: firename empty");
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(lastName)) {
            Log.d(TAG, "verify: lastname empty");
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        }
        if (cancel) {
            Log.d(TAG, "verify: canceled");
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // perform the user update attempt.
            Log.d(TAG, "verify: UPDATE RAN");
            update();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private void update() {
        Log.d(TAG, "update: RUNNING UPDATE");
        FirebaseUser user = mAuth.getCurrentUser();
        myRef.child("Users").child(user.getUid()).child("SetUp").setValue(true);
        myRef.child("Users").child(user.getUid()).child("FirstName").setValue(mFirstNameView.getText().toString());
        myRef.child("Users").child(user.getUid()).child("LastName").setValue(mLastNameView.getText().toString());
        if (ulcers) {
            myRef.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Ulcers").setValue(true);
        } else {
            myRef.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Ulcers").setValue(false);
        }
        Log.d(TAG, "update: ABOUT TO REDIRECT");
        startActivity(mainIntent);
        finish();
    }
}