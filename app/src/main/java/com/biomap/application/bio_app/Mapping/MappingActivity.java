package com.biomap.application.bio_app.Mapping;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;

import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.BottomNavigationViewHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.Random;

/**
 * Draws the pressure map on the Mapping Activity.
 * <p>
 * Created by hamis on 2017-06-13.
 */
public class MappingActivity extends AppCompatActivity {

    private static final String TAG = "MappingActivity";

    private static final int ACTIVITY_NUM = 0;

    private BitmapView[] myViews;

    private GridLayout mappingGridLayout;

    public static final int MAP_SIZE = 64;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        Log.d(TAG, "onCreate: starting.");

        // Make the bottom navigation bar.
        setupBottomNavigationView();

        // Get the Mapping Grid layout to manipulate.
        mappingGridLayout = (GridLayout) findViewById(R.id.mappingGrid);

        // Get the number of columns and rows to be displayed in the Mapping Grid.
        int numOfCol = mappingGridLayout.getColumnCount();
        int numOfRow = mappingGridLayout.getRowCount();

        // Make an array of all the Squares in the Mapping Grid
        myViews = new BitmapView[numOfCol * numOfRow];

        int gridId = 0;

        int[] pressure = getPressure();
        int pressureIndex = 0;

        for (int yPos = 0; yPos < numOfRow; yPos++) {
            for (int xPos = 0; xPos < numOfCol; xPos++) {
                BitmapView tView = new BitmapView(
                        this, // View
                        xPos, // X position
                        yPos, // Y Position
                        pressure[pressureIndex++ % pressure.length] // The pressure value from array
                );
                tView.setId(gridId++);
                myViews[yPos * numOfCol + xPos] = tView;
                mappingGridLayout.addView(tView);
            }
        }

        // Create listeners for each Mapping Grid Square.
        mappingGridLayout.getViewTreeObserver().addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    // Set the space between each button. I think this should be zero for
                    // seamless appearance.
                    final int MARGIN = 0;

                    int pWidth = mappingGridLayout.getWidth();
                    int pHeight = pWidth;
                    int numOfCol = mappingGridLayout.getColumnCount();
                    int numOfRow = mappingGridLayout.getRowCount();
                    int w = pWidth / numOfCol;
                    int h = pHeight / numOfRow;

                    for (int yPos = 0; yPos < numOfRow; yPos++) {
                        for (int xPos = 0; xPos < numOfCol; xPos++) {
                            GridLayout.LayoutParams params =
                                    (GridLayout.LayoutParams) myViews[yPos * numOfCol + xPos].getLayoutParams();
                            params.width = w;
                            params.height = h;
                            params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
                            myViews[yPos * numOfCol + xPos].setLayoutParams(params);
                        }
                    }
                }
            });
    }

    /**
     * Sets up and enables the bottom navigation bar for each activity.
     *
     * Also customizes the bottom navigation so that the buttons don't physically react to being
     * selected. Without this method, the buttons grow and shrink and shift around. It's gross.
     */
    public void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting-up bottom navigation view.");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
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
        int[] pressure = new int[MAP_SIZE];
        Random rand = new Random();
        for (int i = 0; i < MAP_SIZE; i++) {
            pressure[i] = rand.nextInt(255);
        }
        return pressure;
    }
}
