package com.biomap.application.bio_app.Mapping;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.biomap.application.bio_app.Home.MenuFragment;
import com.biomap.application.bio_app.Home.SectionsPagerAdapter;
import com.biomap.application.bio_app.Home.SettingsFragment;
import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.BottomNavigationViewHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.Random;

import jp.wasabeef.blurry.Blurry;

/**
 * Draws the pressure map on the Mapping Activity.
 * <p>
 * Created by hamis on 2017-06-13.
 */
public class MappingActivity extends AppCompatActivity implements MappingView.OnToggledListener {

    private static final String TAG = "MappingActivity";

    private static final int ACTIVITY_NUM = 0;

    private BitmapView[] myViews;

    private GridLayout mappingGridLayout;

    private LinearLayout viewGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        Log.d(TAG, "onCreate: starting.");

        // Make the bottom navigation bar.
        setupBottomNavigationView();
        setupViewPager();

        // Get the Mapping Grid layout to manipulate.
        mappingGridLayout = (GridLayout) findViewById(R.id.mappingGrid);

        // Get the number of columns and rows to be displayed in the Mapping Grid.
        int numOfCol = mappingGridLayout.getColumnCount();
        int numOfRow = mappingGridLayout.getRowCount();

        // Make an array of all the Squares in the Mapping Grid
        myViews = new BitmapView[numOfCol * numOfRow];

        int gridId = 0;

        for (int yPos = 0; yPos < numOfRow; yPos++) {
            for (int xPos = 0; xPos < numOfCol; xPos++) {

                Random rand = new Random();

                int pressure = rand.nextInt(255);

                int red = 255 - pressure;
                int green = 255 - pressure;
                int blue = 255 - pressure;

                int color;

                if (pressure >= 0 && pressure <= 100)
                    color = Color.rgb(red, green, blue);
                else if (pressure > 100 && pressure <= 200)
                    color = Color.rgb(red, green, blue);
                else if (pressure > 200 && pressure <= 255)
                    color = Color.argb(1, red, green, blue);
                else
                    color = Color.TRANSPARENT;

                BitmapView tView = new BitmapView(this, xPos, yPos, color);

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

        /* TODO: The Blurring.
        viewGroup = (LinearLayout) findViewById(R.id.mapping_viewGroup);
        viewGroup.post(new Runnable() {
            @Override
            public void run() {
                Blurry.with(MappingActivity.this)
                        .radius(25)
                        .sampling(2)
                        .color(Color.TRANSPARENT)
                        .onto((ViewGroup) viewGroup);
            }
        });
        */
    }

    /**
     * Adds menu, home, and settings fragments to the MainActivity.
     */
    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new MenuFragment());
        adapter.addFragment(new MappingFragment());
        adapter.addFragment(new SettingsFragment());

        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        TabLayout.Tab tabMenu = tabLayout.getTabAt(0);
        assert tabMenu != null;
        tabMenu.setIcon(R.drawable.ic_hamburger);

        TabLayout.Tab tabSettings = tabLayout.getTabAt(2);
        assert tabSettings != null;
        tabSettings.setIcon(R.drawable.ic_settings);

        // Starts the MainActivity HomeFragment when booted
        TabLayout.Tab tabHome = tabLayout.getTabAt(1);
        assert tabHome != null;
        viewPager.setCurrentItem(tabHome.getPosition());

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
