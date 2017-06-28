package com.biomap.application.bio_app.Home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.biomap.application.bio_app.Alerts.AlertsActivity;
import com.biomap.application.bio_app.Analytics.AnalyticsActivity;
import com.biomap.application.bio_app.Connect.ConnectActivity;
import com.biomap.application.bio_app.Mapping.MappingActivity;
import com.biomap.application.bio_app.R;

/**
 * Created by hamis on 2017-06-13.
 */

public class MenuFragment extends Fragment {

    private static final String TAG = "MenuFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        setupMenuFragmentButtons(view);
        return view;
    }

    /**
     * Creates the menu buttons for the menu fragment.
     * Method is shit; is result of no internet on ferry.
     * <p>
     * Note: I have to pass in the View from onCreateView() because, otherwise,
     * the Buttons return null.
     */
    public void setupMenuFragmentButtons(View view) {

        ImageButton[] menuButtons = {
                (ImageButton) view.findViewById(R.id.menu_button_mapping),
                (ImageButton) view.findViewById(R.id.menu_button_alerts),
                (ImageButton) view.findViewById(R.id.menu_button_analytics),
                (ImageButton) view.findViewById(R.id.menu_button_connect)
        };

        final Class[] menuActivities = {
                MappingActivity.class,
                AlertsActivity.class,
                AnalyticsActivity.class,
                ConnectActivity.class
        };

        int numMenuButtons = menuButtons.length;

        for (int i = 0; i < numMenuButtons; i++) {
            final int finalI = i;
            menuButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), menuActivities[finalI]);
                    startActivity(intent);
                }
            });
        }


    }

}
