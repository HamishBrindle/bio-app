package com.biomap.application.bio_app.Mapping;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.Toast;

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
public class MappingActivity extends AppCompatActivity implements BitmapSquare.OnToggledListener {

    private static final String TAG = "MappingActivity";
    private static final int ACTIVITY_NUM = 0;
    private static final int NODES_RESOLUTION = 8;
    private static final int NUM_NODES = (int) Math.pow(NODES_RESOLUTION, 2);
    public static final int MAP_RESOLUTION = (NODES_RESOLUTION * 2) + 1;
    public static final int MAP_SIZE = (int) Math.pow(MAP_RESOLUTION, 2);
    private BitmapSquare[][] gridSquares;
    private GridLayout grid;
    private final Random rand = new Random();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFonts();
        setContentView(R.layout.activity_mapping);

        Log.d(TAG, "onCreate: starting.");

        // Make the bottom navigation bar.
        setupBottomNavigationView();
        setupGrid();
    }

    /**
     * Draw the pressure map and add to activity.
     */
    private void setupGrid() {

        // Get the Mapping Grid layout to manipulate.
        grid = (GridLayout) findViewById(R.id.mappingGrid);

        // Set the number of columns and rows in the grid.
        grid.setRowCount(MAP_RESOLUTION);
        grid.setColumnCount(MAP_RESOLUTION);

        // Get the number of columns and rows to be displayed in the Mapping Grid.
        int numOfCol = grid.getColumnCount();
        int numOfRow = grid.getRowCount();

        // Make an array of all the Squares in the Mapping Grid
        gridSquares = new BitmapSquare[numOfCol][numOfRow];

        // Initialize gridId to start.
        int gridId = 0;

        // Make a mock pressure-chart; this will be 8x8.
        int[] pressure = getPressure();

        // Expand the 8x8 pressure inputs to MAP_RESOLUTION.
        MappingMatrix matrix = new MappingMatrix(pressure, NODES_RESOLUTION, MAP_RESOLUTION);
        int[][] expandedMatrix = matrix.convertMatrix2D(pressure);
        expandedMatrix = matrix.expandMatrix(expandedMatrix);

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
                tView.setOnToggledListener(this);
                tView.setId(gridId++);
                gridSquares[xPos][yPos] = tView;
                grid.addView(tView);
            }
        }

        // Create listeners for each Mapping Grid Square.
        grid.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Set the space between each button. Keep at zero.
                        final int MARGIN = 0;

                        int pWidth = grid.getWidth();
                        int pHeight = (int) (pWidth * 0.75);
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

        // TODO: This will eventually get information from the nodes and create an array.

        return new int[]{
            3, 6, 4, 20, 30, 20, 15, 11,
            7, 20, 70, 88, 90, 75, 20, 7,
            15, 45, 50, 11, 44, 65, 30, 10,
            2, 4, 10, 4, 8, 23, 10, 5,
            10, 20, 10, 5, 5, 7, 15, 2,
            20, 40, 30, 5, 7, 28, 33, 15,
            30, 65, 60, 15, 11, 45, 55, 10,
            40, 80, 70, 20, 20, 65, 77, 13
        };
    }

    @Override
    public void OnToggled(BitmapSquare v, boolean touchOn) {
        //get the id string
        String idString = v.getXCoordinate() + ":" + v.getYCoordinate();

        v.setColor(Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));

        Toast.makeText(MappingActivity.this,
                "Toogled:\n" +
                        idString + "\n",
                Toast.LENGTH_SHORT).show();
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
