package com.biomap.application.bio_app.Mapping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Creates a square within a Grid... WITHIN A GRID. Shit is real complex.
 * I refer to this object as an 'inner grid'.
 */
public class BitmapSquare extends View {

    public interface OnToggledListener {
        void OnToggled(BitmapSquare v, boolean touchOn);
    }

    boolean touchOn;
    boolean mDownTouch = false;
    private OnToggledListener toggledListener;

    private static final String TAG = "SQAURE";
    private int x = 0; //default
    private int y = 0; //default
    private int gridID;
    private int pressure;
    private int color;

    /**
     * Primary constructor.
     *
     * @param context  Layout we're adding into.
     * @param x        X-coordinate of the square within the inner grid.
     * @param y        Y-coordinate of the square within the inner grid.
     * @param pressure Pressure of the node within the inner grid.
     */
    public BitmapSquare(Context context, int x, int y, int gridID, int pressure) {
        super(context);
        this.x = x;
        this.y = y;
        this.gridID = gridID;
        this.pressure = pressure;
        color = getColorFromPressure(pressure);
        init();
    }

    public BitmapSquare(Context context) {
        super(context);
        init();
    }

    public BitmapSquare(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BitmapSquare(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Honestly don't know the exact reason I have to include this. It may have something to do
     * with the parent GridView.
     *
     * @param widthMeasureSpec  n/a
     * @param heightMeasureSpec n/a
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
    }

    /**
     * Color the background of this square.
     *
     * @param canvas The square itself (square is a Canvas object).
     */
    @Override
    protected void onDraw(Canvas canvas) {
        Log.e(TAG, "Square drawn - width: " + canvas.getWidth());
        canvas.drawColor(this.color);
    }

    /**
     * Determine the color of the square based on the pressure reading during initialization.
     */
    private int getColorFromPressure(int input) {
        int color;
        int pressure = input * 5;

        if (pressure > 255 && pressure < 370)
            color = Color.argb(255, 255, 140 + (pressure - 255), 180);
        else if (pressure >= 370 && pressure < 445)
            color = Color.argb(255, 255, 255, 180 + (pressure - 370));
        else if (pressure >= 445)
            color = Color.WHITE;
        else
            color = Color.argb(pressure, pressure, 140, 180);

        return color;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                touchOn = !touchOn;
                invalidate();

                if (toggledListener != null) {
                    toggledListener.OnToggled(this, touchOn);
                }

                mDownTouch = true;
                return true;

            case MotionEvent.ACTION_UP:
                if (mDownTouch) {
                    mDownTouch = false;
                    performClick();
                    return true;
                }
        }
        return false;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public void setOnToggledListener(OnToggledListener listener) {
        toggledListener = listener;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public int getXCoordinate() {
        return x;
    }

    public int getYCoordinate() {
        return y;
    }

    public void setXCoordinate(int x) {
        this.x = x;
    }

    public void setYCoordinate(int y) {
        this.y = y;
    }

    public int getGridID() {
        return gridID;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    private void init() {
        touchOn = false;
    }
}