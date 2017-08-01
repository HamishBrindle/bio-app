package com.biomap.application.bio_app.Login;

import android.content.Intent;
import android.net.Uri;
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
    CustomFontTextView mProfileName;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private String[] name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mProfileName = (CustomFontTextView) findViewById(R.id.profile_name);
        setupFirebase();
        setUpButtons();

    }

    @SuppressWarnings("Convert2Lambda")
    private void setUpButtons() {
        Button signOutBtn = (Button) findViewById(R.id.account_signOut_button);
        Button updateBtn = (Button) findViewById(R.id.account_update_profile);
        Button contactBtn = (Button) findViewById(R.id.account_contact_btn);

        signOutBtn.setOnClickListener(new View.OnClickListener() {
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
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileUpdateIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(profileUpdateIntent);
                finish();
            }
        });
        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.biomap.ca"));
                startActivity(browserIntent);

            }
        });

    }


    private void setupFirebase() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();

        FirebaseAuth.AuthStateListener mAuthListener = firebaseAuth -> {

            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged.Main:signed_in:" + user.getUid());
                myRef.addValueEventListener(new ValueEventListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String fullName = dataSnapshot.child("Users").child(mAuth.getCurrentUser().getUid()).child("Name").getValue().toString();
                        name = fullName.split(" ");
                        mProfileName.setText(fullName);

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
