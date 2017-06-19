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

    public BitmapView(Context context, int x, int y, int color) {
        super(context);
        idX = x;
        idY = y;
        this.color = color;
        init();
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

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public int getIdX(){
        return idX;
    }

    public int getIdY(){
        return idY;
    }

}