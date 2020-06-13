package com.example.myfirstapp;

import android.graphics.PointF;

public class Box {
    public String label;
    public boolean oval;
    public static int total = 0;
    private PointF mOrigin;
    private PointF mCurrent;
    public Box(PointF origin) {
        mOrigin = origin;
        mCurrent = origin;
        label="New Box";
        oval = false;
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
