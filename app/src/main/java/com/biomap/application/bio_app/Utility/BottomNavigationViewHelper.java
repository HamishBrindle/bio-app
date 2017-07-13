package com.biomap.application.bio_app.Utility;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.biomap.application.bio_app.Alerts.AlertsActivity;
import com.biomap.application.bio_app.Analytics.AnalyticsActivity;
import com.biomap.application.bio_app.Connect.ConnectActivity;
import com.biomap.application.bio_app.Home.MainActivity;
import com.biomap.application.bio_app.Mapping.MappingActivity;
import com.biomap.application.bio_app.R;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

/**
 * Creates and enables the bottom navigation nav_top. This nav_top is always visible and switches
 * between the main activities.
 * <p>
 * Created by hamis on 2017-06-13.
 */

public class BottomNavigationViewHelper extends AppCompatActivity {

    private static final String TAG = "BottomNavigationViewHelper";

    /**
     * BottomNavigationView Setup
     */
    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    /**
     * Enables the navigation for the bottom navigation bar; adds listeners to each button IN ORDER.
     * Each button is labeled with the corresponding number to it's order.
     *
     * @param context The activity we're presently in.
     * @param view    The nav_top we're using.
     */
    public static void enableNavigation(final Context context, BottomNavigationViewEx view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.ic_mapping:
                        Intent intentMapping = new Intent(context, MappingActivity.class); // 0
                        context.startActivity(intentMapping);
                        break;

                    case R.id.ic_alerts:
                        Intent intentAlerts = new Intent(context, AlertsActivity.class); // 1
                        context.startActivity(intentAlerts);
                        break;

                    case R.id.ic_home:
                        Intent intentHome = new Intent(context, MainActivity.class); // 2
                        context.startActivity(intentHome);
                        break;

                    case R.id.ic_analytics:
                        Intent intentAnalytics = new Intent(context, AnalyticsActivity.class); // 3
                        context.startActivity(intentAnalytics);
                        break;

                    case R.id.ic_connect:
                        Intent intentConnect = new Intent(context, ConnectActivity.class); // 4
                        context.startActivity(intentConnect);
                        break;
                }

                return false;
            }
        });
    }

}
