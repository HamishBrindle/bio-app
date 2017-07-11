package com.biomap.application.bio_app.Mapping;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.ImageButton;

import com.biomap.application.bio_app.Alerts.AlertsActivity;
import com.biomap.application.bio_app.Analytics.AnalyticsActivity;
import com.biomap.application.bio_app.Connect.ConnectActivity;
import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.BottomNavigationViewHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.Random;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Draws the pressure map on the Mapping Activity.
 * <p>
 * Created by hamis on 2017-06-13.
 */
public class MappingActivity extends AppCompatActivity {

    private static final String TAG = "MappingActivity";
    private static final int ACTIVITY_NUM = 0;

    private final double heightReduction = 0.60;
    private final Random rand = new Random();
    private BitmapSquare[][] gridSquares;
    private GridLayout grid;
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFonts();
        setContentView(R.layout.activity_mapping);

        // Get the Mapping Grid layout to manipulate.
        grid = (GridLayout) findViewById(R.id.mappingGrid);

        // Make the bottom navigation bar.
        setupGrid();
        setupToolbar();
        setupBottomNavigationView();

    }

    /**
     * Draw the pressure map and add to activity.
     */
    private void setupGrid() {

        // Make a mock pressure-chart; this will be 8x8.
        int[] pressure = getPressure();

        // Expand the 8x8 pressure inputs to MAP_RESOLUTION.
        MappingMatrix matrix = new MappingMatrix();

        int[][] expandedMatrix = matrix.convert2D(pressure);

        expandedMatrix = matrix.expand(expandedMatrix, 3);

        // Set the number of columns and rows in the grid.
        grid.setRowCount(expandedMatrix.length);
        grid.setColumnCount(expandedMatrix[0].length);

        // Get the number of columns and rows to be displayed in the Mapping Grid.
        int numOfCol = grid.getColumnCount();
        int numOfRow = grid.getRowCount();

        // Make an array of all the Squares in the Mapping Grid
        gridSquares = new BitmapSquare[numOfCol][numOfRow];

        // Initialize gridId to start.
        int gridId = 0;


        // Create squares for the pressure map and add them to the grid. Also, make an array for
        // the squares so we can make further changes to the grid.
        for (int yPos = 0; yPos < numOfRow; yPos++) {
            for (int xPos = 0; xPos < numOfCol; xPos++) {
                BitmapSquare tView = new BitmapSquare(
                        this, // View
                        xPos, // X position
                        yPos, // Y Position
                        gridId,
                        expandedMatrix[xPos][yPos] // The pressure value from array
                );
                tView.setId(gridId++);
                gridSquares[xPos][yPos] = tView;
                grid.addView(tView);
            }
        }

        /* Create listeners for each Mapping Grid Square.
         * TODO: Produces strange error message in logcat:
         * "horizontal constraints [...] are inconsistent; permanently removing: [...]"
         */
        grid.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        // Set the space between each button. Keep at zero.
                        final int MARGIN = 0;

                        int pWidth = grid.getWidth();
                        int pHeight = (int) (pWidth * heightReduction);
                        int numOfCol = grid.getColumnCount();
                        int numOfRow = grid.getRowCount();
                        int w = pWidth / numOfCol;
                        int h = pHeight / numOfRow;

                        for (int yPos = 0; yPos < numOfRow; yPos++) {
                            for (int xPos = 0; xPos < numOfCol; xPos++) {
                                GridLayout.LayoutParams params =
                                        (GridLayout.LayoutParams) gridSquares[xPos][yPos].getLayoutParams();
                                params.width = w;
                                params.height = h;
                                params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
                                gridSquares[xPos][yPos].setLayoutParams(params);
                            }
                        }
                    }

                });
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

        // Setup drawer view
        setupDrawerContent(nvDrawer);

        ImageButton hamburger = (ImageButton) findViewById(R.id.toolbar_hamburger);
        hamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(GravityCompat.START);
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
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
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
                intent = new Intent(getBaseContext(), AnalyticsActivity.class);
                break;
            case R.id.nav_connect:
                intent = new Intent(getBaseContext(), ConnectActivity.class);
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

    /**
     * Initialize the custom fonts.
     */
    private void initFonts() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Gotham-Bold.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

}
