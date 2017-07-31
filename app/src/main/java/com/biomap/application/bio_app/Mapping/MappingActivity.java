package com.biomap.application.bio_app.Mapping;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.biomap.application.bio_app.Alerts.AlertsActivity;
import com.biomap.application.bio_app.Connect.ConnectActivity;
import com.biomap.application.bio_app.Login.AccountActivity;
import com.biomap.application.bio_app.Login.LoginRegisterActivity;
import com.biomap.application.bio_app.Mapping.Heatmap.MyGLSurfaceView;
import com.biomap.application.bio_app.OpenGL.GLHeatmap;
import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.BottomNavigationViewHelper;
import com.biomap.application.bio_app.Utility.CustomFontTextView;
import com.biomap.application.bio_app.Utility.SerialHelper;
import com.biomap.application.bio_app.Vitals.VitalsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

/**
 * Draws the pressure map on the Mapping Activity.
 * <p>
 * Created by hamis on 2017-06-13.
 */
public class MappingActivity extends AppCompatActivity {

    private static final String TAG = "MappingActivity";
    private static final int ACTIVITY_NUM = 0;
    DatabaseReference myRef;
    FirebaseDatabase database;
    private DrawerLayout mDrawer;
    private CustomFontTextView leftWeightText;
    private CustomFontTextView rightWeightText;
    private ProgressBar leftProgress;
    private ProgressBar rightProgress;

    private GLHeatmap heatmap;
    private int[] serialInArray;

    public MappingActivity() {
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        // Dashed warning line doesn't appear 'dashed' unless the following:
        ImageView mDashedLine = (ImageView) findViewById(R.id.dashed_line);
        mDashedLine.setLayerType(View.LAYER_TYPE_SOFTWARE, null);


        leftWeightText = (CustomFontTextView) findViewById(R.id.left_percentage);
        rightWeightText = (CustomFontTextView) findViewById(R.id.right_percentage);
        leftProgress = (ProgressBar) findViewById(R.id.left_progress);
        rightProgress = (ProgressBar) findViewById(R.id.right_progress);


        ImageButton accountSettings = (ImageButton) findViewById(R.id.toolbar_settings);
        accountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accountIntent = new Intent(getApplicationContext(), AccountActivity.class);
                startActivity(accountIntent);

            }
        });


        setupFirebase();
        setupDateBanner();
        setupToolbar();
        setupHeatMap();
        setupBottomNavigationView();
        // calculateDistribution(getPressure());

    }

    private void setupDateBanner() {
        //Setting the date banner
        TextView mDayofWeek = (TextView) findViewById(R.id.date_weekday);
        TextView mfullDate = (TextView) findViewById(R.id.date_month_day);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        Date date = new Date();
        Calendar cal = Calendar.getInstance();

        mfullDate.setText(simpleDateFormat.format(date));
        mDayofWeek.setText(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()));
    }

    private void setupFirebase() {
        final Intent register_login_intent = new Intent(this, LoginRegisterActivity.class);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

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
                    startActivity(register_login_intent);
                    finish();
                }
            }
        };

        // Get the user's authentication credentials and check if signed in or not.
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuthListener.onAuthStateChanged(mAuth);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupHeatMap() {
        LinearLayout heatMapView = (LinearLayout) findViewById(R.id.heatmap_parent);
        MyGLSurfaceView mGLView = new MyGLSurfaceView(this);
        heatMapView.addView(mGLView);

        heatmap = mGLView.getRenderer().getHeatmap();
        SerialHelper serialHelper = new SerialHelper(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if (serialHelper.getRender()) {
                        Log.d(TAG, "run: setupHeatmap: Attempting to render map.");
                        ArrayList<Integer> temp = serialHelper.getIntArray();
                        int size = temp.size();
                        for (int i = 0; i < size; i++) {
                            serialInArray[i] = temp.get(i);
                        }
                        heatmap.plotHeatMap(serialInArray);
                        serialHelper.setRender(false);
                    }
                }
            }
        }).start();

    }

    /**
     * Convert a 1D matrix into a
     *
     * @param input Array to be converted.
     * @return Converted array.
     */
    int[][] convert2DArray(int[] input) {
        int resolution = (int) Math.sqrt(input.length);
        int[][] output = new int[resolution][resolution];
        int count = 0;

        for (int yPos = 0; yPos < resolution; yPos++) {
            for (int xPos = 0; xPos < resolution; xPos++) {
                output[xPos][yPos] = input[count++];
            }
        }
        return output;
    }


    /**
     * Setup the top-action-bar for navigation, page title, and settings.
     */
    private void setupToolbar() {
        // Set a Toolbar to replace the ActionBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Find drawer view
        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nvView);

        //finding header of nav bar
        View header = nvDrawer.getHeaderView(0);

        // Setup drawer view
        setupDrawerContent(nvDrawer);

        ImageButton hamburger = (ImageButton) findViewById(R.id.toolbar_hamburger);
        hamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(GravityCompat.START);
            }
        });
        TextView mTimeOfDay = (TextView) header.findViewById(R.id.nav_header_time_of_day);
        final TextView mNameOfUser = (TextView) header.findViewById(R.id.nav_header_user_name);

        Calendar calender = Calendar.getInstance();

        if (6 < calender.get(Calendar.HOUR_OF_DAY) && calender.get(Calendar.HOUR_OF_DAY) < 12) {
            mTimeOfDay.setText(getString(R.string.good_morning_text));
        } else if (12 <= calender.get(Calendar.HOUR_OF_DAY) && calender.get(Calendar.HOUR_OF_DAY) < 17) {
            mTimeOfDay.setText(getString(R.string.good_afternoon_text));
        } else {
            mTimeOfDay.setText(getString(R.string.good_evening_text));
        }

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] fullname = dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Name").getValue().toString().split(" ");
                mNameOfUser.setText(fullname[0].substring(0, 1).toUpperCase() + fullname[0].substring(1));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    /**
     * Open's the drawer when the hamburger (menu button) is selected.
     *
     * @param item Selected item.
     * @return Selected item as well.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Setup the buttons for inside the navigation drawer in the top-action-bar.
     *
     * @param navigationView The drawer menu.
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    /**
     * Gets user's choice of drawer buttons.
     *
     * @param menuItem The drawer button chosen.
     */
    public void selectDrawerItem(MenuItem menuItem) {

        Intent intent = null;

        switch (menuItem.getItemId()) {
            case R.id.nav_mapping:
                intent = new Intent(getBaseContext(), MappingActivity.class);
                break;
            case R.id.nav_alerts:
                intent = new Intent(getBaseContext(), AlertsActivity.class);
                break;
            case R.id.nav_analytics:
                intent = new Intent(getBaseContext(), VitalsActivity.class);
                break;
            case R.id.nav_connect:
                intent = new Intent(getBaseContext(), ConnectActivity.class);
                break;
            case R.id.nav_sign_out:
                FirebaseAuth.getInstance().signOut();
                setupFirebase();
                Intent logoutintent = new Intent(getApplicationContext(), LoginRegisterActivity.class);
                ComponentName cn = logoutintent.getComponent();
                intent = IntentCompat.makeRestartActivityTask(cn);
                break;
            case R.id.nav_account:
                intent = new Intent(this, AccountActivity.class);
                break;
            default:

        }

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();

        startActivity(intent);

    }

    /**
     * Sets up and enables the bottom navigation bar for each activity.
     * <p>
     * Also customizes the bottom navigation so that the buttons don't physically react to being
     * selected. Without this method, the buttons grow and shrink and shift around. It's gross.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting-up bottom navigation view.");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);

        // Set color of selected item in the navbar (unique to each activity)
        bottomNavigationViewEx.setIconTintList(ACTIVITY_NUM, getColorStateList(R.color.bottom_nav_mapping));

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(MappingActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem item = menu.getItem(ACTIVITY_NUM);
        item.setChecked(true);
    }

    /**
     * Gets a pressure-reading array of values for each node.
     *
     * @return The pressure map array of values.
     */
    private int[] getPressure() {

        // TODO: This will eventually get information from the nodes and create an array.

        return new int[]{
                0, 0, 0, 20, 30, 20, 0, 0,
                0, 20, 70, 88, 90, 75, 20, 0,
                15, 65, 70, 61, 64, 65, 50, 0,
                0, 75, 60, 40, 38, 60, 50, 0,
                0, 50, 50, 20, 0, 55, 55, 0,
                0, 60, 45, 0, 0, 48, 50, 0,
                0, 65, 40, 0, 0, 45, 55, 0,
                0, 40, 30, 0, 0, 55, 47, 0
        };
    }

    public void calculateDistribution(int[] dataArray) {
        int[][] twoDdataArray = convert2DArray(dataArray);
        Log.d(TAG, "calculateDistribution: " + twoDdataArray.toString());
        double leftWeight = 0;
        double rightWeight = 0;
        double total = 0;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 8; j++) {
                leftWeight += twoDdataArray[i][j];
                Log.d(TAG, "calculateDistribution: left " + leftWeight);
            }

        }
        for (int i = 4; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                rightWeight += twoDdataArray[i][j];
                Log.d(TAG, "calculateDistribution:right  " + rightWeight);
            }

        }
        total = leftWeight + rightWeight;
        leftWeight = (leftWeight / total) * 100;
        rightWeight = (rightWeight / total) * 100;
        leftWeightText.setText(String.valueOf((int) leftWeight) + "%");
        rightWeightText.setText(String.valueOf((int) rightWeight) + "%");
        leftProgress.setProgress((int) leftWeight);
        rightProgress.setProgress((int) rightWeight);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setupFirebase();
    }


}
