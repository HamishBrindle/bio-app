package com.biomap.application.bio_app.Mapping.Heatmap;

/**
 * Created by Hamish Brindle on 2017-07-31.
 */

public class PressureNode {

    private int startingPressure;
    private int sensitivity;
    private int pressure;
    private int xPos;
    private int yPos;

    public PressureNode(int startingPressure, int xPos, int yPos) {
        this.startingPressure = startingPressure;
        this.xPos = xPos;
        this.yPos = yPos;
        //calibrate();
    }

    private void calibrate() {
        if (startingPressure < 10)
            startingPressure = 0;
    }

    public int getPressure() {
        return pressure;
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public void setPressure(int pressure) {
        if (pressure > startingPressure)
            this.pressure = pressure - startingPressure;
        else
            this.pressure = 0;
    }

    public int getStartingPressure() {
        return startingPressure;
    }
}
