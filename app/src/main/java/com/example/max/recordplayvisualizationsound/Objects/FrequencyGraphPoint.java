package com.example.max.recordplayvisualizationsound.Objects;

import java.io.Serializable;

/**
 * Created by Max on 16.04.2017.
 */

public class FrequencyGraphPoint implements Serializable{

    private double pointX;
    private double pointY;

    public double getPointX() {
        return pointX;
    }

    public void setPointX(double pointX) {
        this.pointX = pointX;
    }

    public double getPointY() {
        return pointY;
    }

    public void setPointY(double pointY) {
        this.pointY = pointY;
    }
}
