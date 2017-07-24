package com.biomap.application.bio_app.Login;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.biomap.application.bio_app.Home.MainActivity;
import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.CustomFontsLoader;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.Manifest.permission.READ_CONTACTS;
import static com.biomap.application.bio_app.Home.MainActivity.SHARED_PREFERENCES;
import static com.biomap.application.bio_app.R.layout.activity_register;

/**
 * A register screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int RC_SIGN_IN = 9001;

    private UserLoginTask mAuthTask = null;
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mConfirmPasswordView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Intent mainIntent;
    private Intent setUpIntent;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_register);

        setupRegisterForm();

    }

    /**
     * Setup the form and enable reading data into Firebase for user.
     */
    private void setupRegisterForm() {

        // Get email from auto-complete.
        mEmailView = (EditText) findViewById(R.id.form_email);

        // Get form elements.
        mPasswordView = (EditText) findViewById(R.id.form_password);
        mFirstNameView = (EditText) findViewById(R.id.form_first_name);
        mLastNameView = (EditText) findViewById(R.id.form_last_name);
        mConfirmPasswordView = (EditText) findViewById(R.id.form_confirm_password);

        Button emailSignUpButton = (Button) findViewById(R.id.form_button_next);
        LinearLayout googleSignInButton = (LinearLayout) findViewById(R.id.google_signin_button);

        //Setting the font for the buttons
        emailSignUpButton.setTypeface(CustomFontsLoader.getTypeface(this, CustomFontsLoader.GOTHAM_BOLD));

        // Setup the intents to direct user from the activity.
        setUpIntent = new Intent(this, ProfileActivity.class);
        mainIntent = new Intent(this, MainActivity.class);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();

        // Listener for any password field editing.
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // Configure Google Sign-In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("87626777677-vt3mopj16fhd99jjjoifa80buvp4d369.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        // Create the state-change listener (user logged in or out)
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged.Login:signed_in:" + user.getUid());
                    startActivity(setUpIntent);

                    // Make switching between activities blend via fade-in / fade-out
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                    Toast.makeText(RegisterActivity.this, "Redirecting",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged.Login:signed_out");
                }
            }
        };

        // Button for redirecting to Registration.
        emailSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });


        // Button for signing-in with Google.
        googleSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

    }

    /**
     * Signs in user with their Google account.
     */
    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

        // Make switching between activities blend via fade-in / fade-out
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * Handler for successful and non-successful login attempts.
     *
     * @param result User's sign in attempt; either successful or not.
     */
    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            //Signed in Succesfully

            Log.d(TAG, "onActivityResult: Success");
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        } else {
            //Still signed out
            Toast.makeText(this, "Unable to Sign in with Google", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Authentication for Google sign-in with Firebase.
     *
     * @param account User's Google account
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @SuppressWarnings("ConstantConditions")
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child("Users").hasChild(mAuth.getCurrentUser().getUid())) {
                                        startActivity(mainIntent);

                                        // Make switching between activities blend via fade-in / fade-out
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                                        finish();
                                    } else {
                                        myRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Email").setValue(mAuth.getCurrentUser().getEmail());
                                        myRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Name").setValue(mAuth.getCurrentUser().getDisplayName());
                                        myRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("SetUp").setValue(false);
                                        startActivity(setUpIntent);

                                        // Make switching between activities blend via fade-in / fade-out
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            mAuthListener.onAuthStateChanged(mAuth);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    /**
     * Using the form's data, create an account for the user in Firebase.
     */
    private void createAccount() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
        mConfirmPasswordView.setError(null);


        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        String confirmPassword = mConfirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("This Field is Required");
            focusView = mPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("This Field is Required");
            focusView = mPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(firstName)) {
            mFirstNameView.setError("This Field is Required");
            focusView = mFirstNameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(lastName)) {
            mLastNameView.setError("This Field is Required");
            focusView = mLastNameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            mConfirmPasswordView.setError("This Field is Required");
            focusView = mConfirmPasswordView;
            cancel = true;
        }
        if (!confirmPassword.equals(password)) {
            mConfirmPasswordView.setError("Passwords must match");
            mPasswordView.setError("Passwords must match");
            focusView = mConfirmPasswordView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            Log.d(TAG, "createAccount: going to login");
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
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


    /**
     * Ask user for access to contacts.
     *
     * @return Permission granted or not.
     */
    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;

        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }


    private boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }


        @Override
        protected Boolean doInBackground(Void... params) {


            mAuth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                @SuppressWarnings("ConstantConditions")
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        //noinspection ThrowableResultOfMethodCallIgnored
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(RegisterActivity.this, "User with this email already exist.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "onComplete: adding to db");
                        String name = mFirstNameView.getText().toString() + " " + mLastNameView.getText().toString();
                        myRef.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Email").setValue(mEmailView.getText().toString());
                        myRef.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Name").setValue(name);
                        myRef.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("SetUp").setValue(false);

                    }
                }
            });


            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (success) {
                // We set the MOBILE_REGISTER_FLAG to true so we don't attempt to login again and again.
                SHARED_PREFERENCES.edit().putBoolean("mobile_register_flag", true).apply();
                Log.d(TAG, "MOBILE REGISTER FLAG SETTING TO TRUE");
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}

