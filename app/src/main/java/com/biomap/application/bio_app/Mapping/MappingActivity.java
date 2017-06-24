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

    public static final int MAP_RESOLUTION = 17;

    public static final int MAP_SIZE = (int) Math.pow(MAP_RESOLUTION, 2);

    private BitmapSquare[][] gridSquares;

    private GridLayout grid;

    private Random rand = new Random();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        Log.d(TAG, "onCreate: starting.");

        // Make the bottom navigation bar.
        setupBottomNavigationView();
        setupGrid();
    }

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

        // TODO: Change expandedMatrix to the new resolution
        expandedMatrix = matrix.expandMatrix(expandedMatrix);

        Log.e(TAG, "expandedMatrix: " + expandedMatrix);

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
                        int pHeight = pWidth;
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

        //        int[] pressure = new int[NUM_NODES];
//        for (int i = 0; i < NUM_NODES; i++) {
//            pressure[i] = rand.nextInt(255);
//        }

        return new int[]{
            10, 20, 30, 40, 50, 60, 70, 80,
            20, 30, 40, 50, 60, 70, 80, 10,
            30, 40, 50, 60, 70, 80, 10, 20,
            40, 50, 60, 70, 80, 10, 20, 30,
            50, 60, 70, 80, 10, 20, 30, 40,
            60, 70, 80, 10, 20, 30, 40, 50,
            70, 80, 10, 20, 30, 40, 50, 60,
            80, 10, 20, 30, 40, 50, 60, 70
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

}
