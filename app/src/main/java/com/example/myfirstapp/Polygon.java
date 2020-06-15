package com.example.myfirstapp;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

public class Polygon {
    public String label;
    public static int total = 0;
    public ArrayList<PointF> points;
    private PointF mOrigin;
    private PointF mCurrent;
    public Polygon(PointF origin) {
        mOrigin = origin;
        mCurrent = origin;
        points = new ArrayList<>();
        //points.add(origin);
        label="";
    }
    public PointF getCurrent() {
        return mCurrent;
    }
    public void setCurrent(PointF current)
    {
        mCurrent = current;
        if(points.size() != 0)
        {
            points.set(points.size() - 1, current);
        }
    }
    public PointF getOrigin() {
        return mOrigin;
    }
    public void setOrigin(PointF point) { mOrigin = point; points.add(point); }
}