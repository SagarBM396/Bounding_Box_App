package com.example.myfirstapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BoxDrawingView extends androidx.appcompat.widget.AppCompatImageView {
    private static final String TAG = "BoxDrawingView";

    public String mode;
    public List<Character> order = new ArrayList<>();

    public Polygon mCurrentPolygon;
    public List<Polygon> mPolygonen = new ArrayList<>();

    public static boolean backgroundSet = false;
    public Bitmap bitmap;
    public static boolean validStringAdd = true;
    public Box mCurrentBox;
    public List<Box> mBoxen = new ArrayList<>();

    public Circle mCurrentCircle;
    public List<Circle> mCirclen = new ArrayList<>();

    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    // Used when creating the view in code
    public BoxDrawingView(Context context) {
        this(context, null);
    }
    // Used when inflating the view from XML
    public BoxDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Activity a = (Activity) context;
        //a.setContentView(R.layout.fragment_drag_and_draw);
        //Button button_undo = (Button) a.findViewById(R.id.undo_button);
        /*button_undo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                undoAction();

            }
        });*/
        // Paint the boxes a nice semitransparent red (ARGB)
        mBoxPaint = new Paint();
        mBoxPaint.setColor(Color.RED);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth((float) 5.0);
        // Paint the background off-white
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.RED);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        // Fill the background
        //canvas.drawPaint(mBackgroundPaint);
        if(backgroundSet == true)
        {
            try{
            canvas.drawBitmap(bitmap, 0, 0 , mBackgroundPaint);}
            catch(Exception e) {
                e.printStackTrace();
                backgroundSet = false;
                                }
        }
        String text = "New Box";
        for (Box box : mBoxen) {
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);
            if(!box.oval)
                canvas.drawRect(left, top, right, bottom, mBoxPaint);
            else
                canvas.drawOval(left, top, right, bottom, mBoxPaint);
            mBoxPaint.setColor(Color.RED);
            mBoxPaint.setTextSize(40);
            text = box.label;
            if(!box.oval)
                canvas.drawText(box.label, left, top, mBoxPaint);
            else
                canvas.drawText(box.label, (left+right)/2.0f, (top + bottom)/2.0f, mBoxPaint);
        }
        text = "New Circle";
        for (Circle circle : mCirclen) {
            float x1 = circle.getOrigin().x;                                        //Starting point x
            float x2 = circle.getCurrent().x;                                       //End point x
            float y1 = circle.getOrigin().y;                                        //Start point y
            float y2 = circle.getCurrent().y;                                       //End point y
            float midx = (x1 + x2)/2;                                               //Midpoint x
            float midy = (y1 + y2)/2;                                               //Midpoint y
            float dist = (float)Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
            canvas.drawCircle(midx, midy, dist/2, mBoxPaint);
            mBoxPaint.setColor(Color.RED);
            mBoxPaint.setTextSize(40);
            text = circle.label;
            canvas.drawText(circle.label, midx, midy, mBoxPaint);
        }
        text = "";
        for (Polygon poly : mPolygonen) {
            if(poly.points.size() == 1)
            {
                canvas.drawPoint(poly.points.get(0).x, poly.points.get(0).y, mBoxPaint);
            }
            PointF previous = poly.getOrigin();
            for(PointF point : poly.points)
            {
                if(point == poly.getOrigin())
                    continue;
                canvas.drawLine(previous.x, previous.y, point.x, point.y, mBoxPaint);
                previous = point;
            }
            canvas.drawLine(previous.x, previous.y, poly.getOrigin().x, poly.getOrigin().y, mBoxPaint);
            text = poly.label;
            mBoxPaint.setColor(Color.RED);
            mBoxPaint.setTextSize(40);
            if(!poly.label.equals(""))
                canvas.drawText(text, poly.getOrigin().x, poly.getOrigin().y, mBoxPaint);
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());
        String action = "";
        switch (mode) {
            case "Box":
            case "Oval":{
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        action = "ACTION_DOWN";
                        order.add('b');

                        // Reset drawing state
                        mCurrentBox = new Box(current);
                        if(mode.equals("Oval")) {
                            mCurrentBox.oval = true;
                            mCurrentBox.label = "New Oval";
                        }
                        mBoxen.add(mCurrentBox);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        action = "ACTION_MOVE";
                        if (mCurrentBox != null) {
                            mCurrentBox.setCurrent(current);
                            invalidate();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        action = "ACTION_UP";

                        showAddItemDialog(getContext(), mCurrentBox);

                        Box.total += 1;
                        mCurrentBox = null;
                        invalidate();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        action = "ACTION_CANCEL";
                        mCurrentBox = null;
                        invalidate();
                        break;
                }
                break;
            }


            case "Circle":
            {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        action = "ACTION_DOWN";
                        order.add('c');
                        // Reset drawing state
                        mCurrentCircle = new Circle(current);
                        mCirclen.add(mCurrentCircle);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        action = "ACTION_MOVE";
                        if (mCurrentCircle != null) {
                            mCurrentCircle.setCurrent(current);
                            invalidate();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        action = "ACTION_UP";

                        showAddItemDialog(getContext(), mCurrentCircle);

                        Circle.total += 1;
                        mCurrentCircle = null;
                        invalidate();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        action = "ACTION_CANCEL";
                        mCurrentCircle = null;
                        invalidate();
                        break;
                }
                break;
            }

            case "Polygon":
            {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        action = "ACTION_DOWN";
                        // Reset drawing state
                        if(mCurrentPolygon == null)
                        {
                            order.add('p');

                            mCurrentPolygon = new Polygon(current);
                            mPolygonen.add(mCurrentPolygon);
                        }
                        if(mCurrentPolygon.points.size() >= 1)
                            mCurrentPolygon.points.add(current);
                        //Toast.makeText(getContext(), String.valueOf(mCurrentPolygon.points.size()), Toast.LENGTH_LONG).show();
                        invalidate();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        action = "ACTION_MOVE";
                        if (mCurrentPolygon != null) {
                            mCurrentPolygon.setCurrent(current);
                            invalidate();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        action = "ACTION_UP";

                        //showAddItemDialog(getContext(), mCurrentPolygon);
                        if (mCurrentPolygon != null) {
                            mCurrentPolygon.setCurrent(current);
                            invalidate();
                        }

                        assert mCurrentPolygon != null;
                        if(mCurrentPolygon.points.size() == 0)
                        {
                            mCurrentPolygon.setOrigin(current);
                        }
                        //Toast.makeText(getContext(), String.valueOf(mCurrentPolygon.points.size()), Toast.LENGTH_LONG).show();

                        //mCurrentPolygon = null;
                        invalidate();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        action = "ACTION_CANCEL";
                        //mCurrentPolygon = null;
                        invalidate();
                        break;
                }
                break;
            }
            default:
                break;

        }
        Log.i(TAG, action + " at x=" + current.x +
                ", y=" + current.y);
        return true;
    }
    private void showAddItemDialog(Context c, final Box box) {
        final EditText taskEditText = new EditText(c);
        final Context con = c;
        AlertDialog dialog = new AlertDialog.Builder(c, R.style.MyAlertDialogStyle)
                .setTitle("Label")
                .setMessage("Enter the label text")
                .setView(taskEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String textval = String.valueOf(taskEditText.getText());
                        if(textval.length() == 0)
                        {
                            Toast.makeText(con, "Please enter a valid Label", Toast.LENGTH_LONG).show();
                            validStringAdd = false;
                            undoAction();
                            invalidate();
                        }
                        else {
                            validStringAdd = true;
                            box.label = textval;
                            invalidate();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        undoAction();
                        invalidate();
                    }
                })
                .create();
        dialog.show();
    }

    private void showAddItemDialog(Context c, final Circle circle) {
        final EditText taskEditText = new EditText(c);
        final Context con = c;
        AlertDialog dialog = new AlertDialog.Builder(c, R.style.MyAlertDialogStyle)
                .setTitle("Label")
                .setMessage("Enter the label text")
                .setView(taskEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String textval = String.valueOf(taskEditText.getText());
                        if(textval.length() == 0)
                        {
                            Toast.makeText(con, "Please enter a valid Label", Toast.LENGTH_LONG).show();
                            validStringAdd = false;
                            undoAction();
                            invalidate();
                        }
                        else {
                            validStringAdd = true;
                            circle.label = textval;
                            invalidate();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        undoAction();
                        invalidate();
                    }
                })
                .create();
        dialog.show();
    }

    public void showAddItemDialog(Context c, final Polygon polygon) {
        final EditText taskEditText = new EditText(c);
        final Context con = c;
        AlertDialog dialog = new AlertDialog.Builder(c, R.style.MyAlertDialogStyle)
                .setTitle("Label")
                .setMessage("Enter the label text")
                .setView(taskEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String textval = String.valueOf(taskEditText.getText());
                        if(textval.length() == 0)
                        {
                            Toast.makeText(con, "Please enter a valid Label", Toast.LENGTH_LONG).show();
                            validStringAdd = false;
                            undoAction();
                            invalidate();
                        }
                        else {
                            validStringAdd = true;
                            polygon.label = textval;
                            invalidate();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        undoAction();
                        invalidate();
                    }
                })
                .create();
        dialog.show();
    }

    public void showDialogPoly()
    {
        if(mPolygonen.size() == 0)
        {
            Toast.makeText(getContext(), "No polygon has been drawn yet!", Toast.LENGTH_LONG).show();
        }
        showAddItemDialog(getContext(), mCurrentPolygon);
    }


    public void undoAction() {
        if(order.size() == 0)
            return;
        char last = order.get(order.size() - 1);
        //Toast.makeText(getContext(), String.valueOf(order.size()), Toast.LENGTH_LONG).show();
        switch (last)
        {
            case 'b':
                if (Box.total == 0 || mBoxen.size() == 0)
                    return;
                else {
                    Box b = mBoxen.remove(Box.total - 1);
                    Box.total -= 1;
                    char temp = order.remove(order.size() - 1);
                    invalidate();
                }
                break;
            case 'c':
                if (Circle.total == 0 || mCirclen.size() == 0)
                    return;
                else {
                    Circle c = mCirclen.remove(Circle.total - 1);
                    Circle.total -= 1;
                    char temp = order.remove(order.size() - 1);
                    invalidate();
                }
                break;
            case 'p':
                if (mPolygonen.size() == 0)
                    return;
                else {
                    if(mCurrentPolygon != null)
                    {
                        mCurrentPolygon = null;
                        Polygon p = mPolygonen.remove(mPolygonen.size() - 1);
                        invalidate();
                        char temp = order.remove(order.size() - 1);
                        return;
                    }
                    Polygon p = mPolygonen.remove(Polygon.total - 1);
                    Polygon.total -= 1;
                    char temp = order.remove(order.size() - 1);
                    invalidate();
                }
            default:
                break;
        }

        }
        public void reDraw(Bitmap bmp)
        {
            bitmap = bmp;
            invalidate();
        }
        public void reDraw(LightingColorFilter lf)
        {
            mBackgroundPaint.setColorFilter(lf);
            invalidate();
        }
        public void reDraw()
        {
            invalidate();
        }
}

