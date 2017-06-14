package com.example.hamishbrindle.bio_app.Utility;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import com.example.hamishbrindle.bio_app.Alerts.AlertsActivity;
import com.example.hamishbrindle.bio_app.Analytics.AnalyticsActivity;
import com.example.hamishbrindle.bio_app.Connect.ConnectActivity;
import com.example.hamishbrindle.bio_app.Home.MainActivity;
import com.example.hamishbrindle.bio_app.Mapping.MappingActivity;
import com.example.hamishbrindle.bio_app.R;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

/**
 * Created by hamis on 2017-06-13.
 */

public class BottomNavigationViewHelper {
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

    public static void enableNavigation(final Context context, BottomNavigationViewEx view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {

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
                        Intent intentAnalytics= new Intent(context, AnalyticsActivity.class); // 3
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
