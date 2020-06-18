package com.example.myfirstapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static android.app.usage.UsageEvents.Event.NONE;

public class ImageCaptureActivity extends AppCompatActivity {

    Button mCaptureBtn;
    Button trashButton;
    ShapeDrawingView mImageView;
    Spinner modeSpnr;
    Button brt_btn;
    String mode;
    private final int PERMISSION_CODE = 1000;
    int progress = 128;

    public static final String[] paths = {"Box", "Circle", "Oval", "Polygon"};

    private static final String TAG = "ImageCaptureActivity";

    public Uri image_uri;

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
        trashButton = findViewById(R.id.trash);

        trashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(getApplicationContext(), view);
                int i = 1;
                for(Box box : mImageView.mBoxen)
                {
                    menu.getMenu().add(1, i, NONE, box.label);
                    i++;
                }
                i = 1;
                for(Circle circle : mImageView.mCirclen)
                {
                    menu.getMenu().add(2, i, NONE, circle.label);
                    i++;
                }
                i = 1;
                for(Polygon poly : mImageView.mPolygonen)
                {
                    menu.getMenu().add(3, i, NONE, poly.label);
                    i++;
                }
                menu.show();

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        int group = menuItem.getGroupId();
                        int i = 1;

                        if(group == 3 && mImageView.mPolygonen.get(id - 1).label.equals(""))
                            return false;

                        try {
                            if(group == 1)
                            {
                                mImageView.mBoxen.remove(id - 1);
                                mImageView.removeFromOrder('b', id);
                            }
                            if(group == 2)
                            {
                                mImageView.mCirclen.remove(id - 1);
                                mImageView.removeFromOrder('c', id);
                            }
                            if(group == 3)
                            {
                                mImageView.mPolygonen.remove(id - 1);
                                mImageView.removeFromOrder('p', id);
                            }
                            mImageView.reDraw();
                            return true;
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            return false;
                        }
                    }
                });
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
                        break;
                    case 1:
                        mode = "Circle";
                        mImageView.mode = "Circle";
                        break;
                    case 2:
                        mode = "Oval";
                        mImageView.mode = "Oval";
                        break;
                    case 3:
                        mode = "Polygon";
                        mImageView.mode = "Polygon";
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
                if(!ShapeDrawingView.backgroundSet || mImageView.bitmap == null)
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
                ShapeDrawingView boxDr = findViewById(R.id.boxView);
                boxDr.undoAction();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mImageView.mCurrentPolygon!=null && mImageView.mCurrentPolygon.label.equals("") )
                {
                    Toast.makeText(getApplicationContext(), "Finish naming the polygon first!", Toast.LENGTH_LONG).show();
                    return;
                }
                if(mImageView.mCurrentCircle!=null && mImageView.mCurrentCircle.label.equals("New Circle") )
                {
                    Toast.makeText(getApplicationContext(), "Finish naming the Circle first!", Toast.LENGTH_LONG).show();
                    return;
                }
                if(mImageView.mCurrentBox!=null && mImageView.mCurrentBox.label.equals("New Box") )
                {
                    Toast.makeText(getApplicationContext(), "Finish naming the box first!", Toast.LENGTH_LONG).show();
                    return;
                }
                if(mImageView.bitmap != null && ShapeDrawingView.backgroundSet) {
                    saveImage(mImageView.getBitmapFromView());
                    //share it
                    send();
                }
                else
                    Toast.makeText(getApplicationContext(), "Set an image first!", Toast.LENGTH_LONG).show();
            }
        });
        mImageView = findViewById(R.id.boxView);
        mImageView.mode = mode;

        mCaptureBtn = findViewById(R.id.capture_image_btn);

        mCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImageAction();
            }
        });
        captureImageAction();
    }

    public void captureImageAction()
    {
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

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        int IMAGE_CAPTURE_CODE = 1001;
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
                ShapeDrawingView boxDr =  this.findViewById(R.id.boxView);
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
                ShapeDrawingView.backgroundSet = true;
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
    private static void saveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root + "/saved_images");
        Log.i("Directory", "==" + myDir);
        myDir.mkdirs();

        String fname = "Image-test" + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void send() {
        try {
            File myFile = new File("/storage/emulated/0/saved_images/Image-test.jpg");
            Log.i("Send Function", "File successfully created");
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String ext = myFile.getName().substring(myFile.getName().lastIndexOf(".") + 1);
            String type = mime.getMimeTypeFromExtension(ext);
            Intent sharingIntent = new Intent("android.intent.action.SEND");
            sharingIntent.setType(type);
            sharingIntent.putExtra("android.intent.extra.STREAM", Uri.parse((myFile.toString())));
            Log.i("Send Function", "Intent set correctly");
            startActivity(Intent.createChooser(sharingIntent, "Share using"));
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}



