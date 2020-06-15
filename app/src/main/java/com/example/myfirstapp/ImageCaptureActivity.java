package com.example.myfirstapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ImageCaptureActivity extends AppCompatActivity {

    Button mCaptureBtn;
    BoxDrawingView mImageView;
    Spinner modeSpnr;
    Button brt_btn;
    Button checkButton;
    String mode;
    private final int PERMISSION_CODE = 1000;
    int progress = 128;

    public static final String[] paths = {"Box", "Circle", "Oval", "Polygon"};

    private static final String TAG = "Button2Activity";

    public Uri image_uri;
    private final int IMAGE_CAPTURE_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button2);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mode = "Box";
        //setContentView(new CustomView(this));

        progress = 128;

        Button undoButton = findViewById(R.id.undo_button);
        Button sendButton = findViewById(R.id.button_send);
        brt_btn = findViewById(R.id.brightness_btn);
        modeSpnr = findViewById(R.id.mode_spinner);
        checkButton = findViewById(R.id.accept);
        checkButton.setVisibility(View.INVISIBLE);
        checkButton.setClickable(false);

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mImageView.mCurrentPolygon == null)
                {
                    return;
                }
                mImageView.showDialogPoly();
                mImageView.mPolygonen.set(mImageView.mPolygonen.size()-1, mImageView.mCurrentPolygon);
                mImageView.mCurrentPolygon = null;
                Polygon.total += 1;
            }
        });


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpnr.setAdapter(adapter);
        modeSpnr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(mImageView.mCurrentPolygon != null)
                {
                    mImageView.mPolygonen.remove(mImageView.mPolygonen.size() - 1);
                    mImageView.mCurrentPolygon = null;
                    mImageView.reDraw();
                }
                switch (i) {
                    case 0:
                        mode = "Box";
                        mImageView.mode = "Box";
                        checkButton.setVisibility(View.INVISIBLE);
                        checkButton.setClickable(false);
                        break;
                    case 1:
                        mode = "Circle";
                        mImageView.mode = "Circle";
                        checkButton.setVisibility(View.INVISIBLE);
                        checkButton.setClickable(false);
                        break;
                    case 2:
                        mode = "Oval";
                        mImageView.mode = "Oval";
                        checkButton.setVisibility(View.INVISIBLE);
                        checkButton.setClickable(false);
                        break;
                    case 3:
                        mode = "Polygon";
                        mImageView.mode = "Polygon";
                        checkButton.setVisibility(View.VISIBLE);
                        checkButton.setClickable(true);
                        break;
                    default:
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getApplicationContext(), "Set a mode", Toast.LENGTH_LONG).show();
            }
        });


        brt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!BoxDrawingView.backgroundSet || mImageView.bitmap == null)
                {
                    Toast.makeText(getApplicationContext(), "Background not set", Toast.LENGTH_LONG).show();
                    return;
                }
                ShowDialog(mImageView.bitmap);
            }
        });

        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BoxDrawingView boxDr = findViewById(R.id.boxView);
                boxDr.undoAction();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Send Button Working", Toast.LENGTH_LONG).show();
            }
        });
        mImageView = findViewById(R.id.boxView);
        mImageView.mode = mode;

        mCaptureBtn = findViewById(R.id.capture_image_btn);

        mCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        openCamera();
                    }
                } else {
                    openCamera();
                }
            }
        });
    }
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK) {
            try {
                Log.d(TAG, "File Uri: " + image_uri.toString());
                BoxDrawingView boxDr =  this.findViewById(R.id.boxView);
                boxDr.mBoxen.clear();
                boxDr.mCirclen.clear();
                boxDr.mPolygonen.clear();
                boxDr.mCurrentPolygon = null;
                //boxDr.mCurrentCircle = null;
                //boxDr.mCurrentBox = null;

                Circle.total = 0;
                Box.total = 0;
                Polygon.total = 0;

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
                bitmap = rotateImageIfRequired(getApplicationContext(), bitmap, image_uri);
                boxDr.bitmap = bitmap;
                BoxDrawingView.backgroundSet = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public Bitmap rotateAndScale(Bitmap bp, int angle) throws IOException {
        Matrix matrix = new Matrix();
        Bitmap bitmap;
        matrix.postRotate(angle);
        bitmap = Bitmap.createBitmap(bp, 0, 0, bp.getWidth(), bp.getHeight(), matrix, true);
        bitmap = Bitmap.createScaledBitmap(bitmap,mImageView.getWidth(), mImageView.getHeight(), true);
        bp.recycle();
        return bitmap;
    }
    private Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23) {
            assert input != null;
            ei = new ExifInterface(input);
        }
        else
            ei = new ExifInterface(Objects.requireNonNull(selectedImage.getPath()));

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateAndScale(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateAndScale(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateAndScale(img, 270);
            default:
                return rotateAndScale(img, 0);
        }
    }

    public void ShowDialog(final Bitmap bmp){
        //Toast.makeText(this, "Reached ShowDialog", Toast.LENGTH_LONG).show();
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final SeekBar seek = new SeekBar(this);
        seek.setMax(256);
        seek.setKeyProgressIncrement(1);
        seek.setProgress(progress);

        popDialog.setIcon(R.drawable.ic_brightness_high_black_24dp);
        popDialog.setTitle("Please Select Into Your Desired Brightness ");
        popDialog.setView(seek);


        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //mImageView.reDraw(changeBitmapContrastBrightness(bmp, (float) progress / 100f, 1));

                ArgbEvaluator evaluator = new ArgbEvaluator();

                float fraction = progress / 256.0F;

                int mul = (int) evaluator.evaluate(fraction, 0xFF7F7F7F, 0xFFFFFFFF);
                int add = (int) evaluator.evaluate(fraction, 0x00000000, 0x00222222);
                mImageView.reDraw(new LightingColorFilter(mul, add));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                mImageView.invalidate();

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mImageView.invalidate();
                progress = seekBar.getProgress();
            }
        });
        // Button OK
        popDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        popDialog.create();
        popDialog.show();
    }
}



