package com.example.hamishbrindle.bio_app.Mapping;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.Toast;

import com.example.hamishbrindle.bio_app.R;
import com.example.hamishbrindle.bio_app.Utility.BottomNavigationViewHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

/**
 * Draws the pressure map on the Mapping Activity.
 *
 * Created by hamis on 2017-06-13.
 */
public class MappingActivity extends AppCompatActivity implements MappingView.OnToggledListener {

    private static final String TAG = "MappingActivity";

    private static final int ACTIVITY_NUM = 0;

    private MappingView[] myViews;

    private GridLayout mappingGridLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        Log.d(TAG, "onCreate: starting.");

        // Make the bottom navigation bar.
        setupBottomNavigationView();

        // Get the Mapping Grid layout to manipulate.
        mappingGridLayout = (GridLayout)findViewById(R.id.mappingGrid);

        // Get the number of columns and rows to be displayed in the Mapping Grid.
        int numOfCol = mappingGridLayout.getColumnCount();
        int numOfRow = mappingGridLayout.getRowCount();

        // Make an array of all the Squares in the Mapping Grid
        myViews = new MappingView[numOfCol*numOfRow];

        int gridId = 0;

        for(int yPos=0; yPos<numOfRow; yPos++){
            for(int xPos=0; xPos<numOfCol; xPos++){
                MappingView tView = new MappingView(this, xPos, yPos);
                tView.setOnToggledListener(this);
                tView.setId(gridId++);
                myViews[yPos*numOfCol + xPos] = tView;
                mappingGridLayout.addView(tView);
            }
        }

        // Create listeners for each Mapping Grid Square.
        mappingGridLayout.getViewTreeObserver().addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener(){
                @Override
                public void onGlobalLayout() {

                    // Set the space between each button. I think this should be zero for
                    // seamless appearance.
                    final int MARGIN = 0;

                    int pWidth = mappingGridLayout.getWidth();
                    int pHeight = pWidth;
                    int numOfCol = mappingGridLayout.getColumnCount();
                    int numOfRow = mappingGridLayout.getRowCount();
                    int w = pWidth/numOfCol;
                    int h = pHeight/numOfRow;

                    for(int yPos=0; yPos<numOfRow; yPos++){
                        for(int xPos=0; xPos<numOfCol; xPos++){
                            GridLayout.LayoutParams params =
                                    (GridLayout.LayoutParams)myViews[yPos*numOfCol + xPos].getLayoutParams();
                            params.width = w - 2*MARGIN;
                            params.height = h - 2*MARGIN;
                            params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
                            myViews[yPos*numOfCol + xPos].setLayoutParams(params);
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

    @Override
    public void OnToggled(MappingView v, boolean touchOn) {

        //get the id string
        String idString = v.getIdX() + ":" + v.getIdY();

        Toast.makeText(MappingActivity.this,
                "Toogled:\n" +
                        idString + "\n" +
                        touchOn + "",
                Toast.LENGTH_SHORT).show();
    }
}
