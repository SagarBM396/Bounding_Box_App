package com.example.myfirstapp;

import android.graphics.PointF;

public class Circle {
    public String label;
    public static int total = 0;
    private PointF mOrigin;
    private PointF mCurrent;
    public Circle(PointF origin) {
        mOrigin = origin;
        mCurrent = origin;
        label="New Circle";
    }
    public PointF getCurrent() {
        return mCurrent;
    }
    public void setCurrent(PointF current) {
        mCurrent = current;
    }
    public PointF getOrigin() {
        return mOrigin;
    }
}

