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

    public static boolean backgroundSet = false;
    public Bitmap bitmap;
    public static boolean validStringAdd = true;
    public Box mCurrentBox;
    public List<Box> mBoxen = new ArrayList<>();

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
            canvas.drawRect(left, top, right, bottom, mBoxPaint);
            mBoxPaint.setColor(Color.RED);
            mBoxPaint.setTextSize(40);
            text = box.label;
            canvas.drawText(box.label, left, top, mBoxPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());
        String action = "";
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                // Reset drawing state
                mCurrentBox = new Box(current);
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

                Box.total+=1;
                mCurrentBox = null;
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                mCurrentBox = null;
                invalidate();
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
    public void undoAction()
    {
        if(Box.total == 0)
            return;
        else {
            Box b = mBoxen.remove(Box.total-1);
            Box.total-=1;
            invalidate();
        }
    }

}

