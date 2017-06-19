package com.biomap.application.bio_app.Mapping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class BitmapView extends View {

    int idX = 0; //default
    int idY = 0; //default
    private int color;
    private int pressure;

    public BitmapView(Context context, int x, int y, int pressure) {
        super(context);
        idX = x;
        idY = y;
        this.pressure = pressure;
        getColorFromPressure();
    }

    public BitmapView(Context context) {
        super(context);
        init();
    }

    public BitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BitmapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(this.color);
    }

    /**
     * Determine the color of the square based on the pressure reading during initialization.
     */
    private void getColorFromPressure() {

        if (pressure >= 200)
            color = Color.rgb(255, 0, 0);
        else if (pressure >= 125)
            color = Color.rgb(127, 127, 0);
        else if (pressure >= 75)
            color = Color.rgb(0, 255, 0);
        else if (pressure >= 50)
            color = Color.rgb(0, 127, 127);
        else if (pressure >= 25)
            color = Color.rgb(0, 0, 255);
        else if (pressure >= 0)
            color = Color.rgb(63, 127, 63);
    }

}