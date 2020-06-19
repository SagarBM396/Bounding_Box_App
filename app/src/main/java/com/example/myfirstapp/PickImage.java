package com.example.myfirstapp;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
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
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import static android.app.usage.UsageEvents.Event.NONE;

public class PickImage extends AppCompatActivity {

    String mode;

    Button pickImagebutton;
    Button undoButton;
    Button sendButton;
    Button trashButton;
    Spinner modeSpnr;
    Button contrastButton;
    ShapeDrawingView mImageView;
    Button brt_btn;

    String filepath = null;

    public static final String[] paths = {"Box", "Circle", "Oval", "Polygon"};

    int brightnessProgress = 128;
    int contrastProgress = 128;

    private static final String TAG = "BoxDrawingView";
    private final int PERMISSION_CODE = 1000;

    private static final int FILE_SELECT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_image);
        mode = "Box";
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        brightnessProgress = 128;
        contrastProgress = 128;

        pickImagebutton = findViewById(R.id.pick_image_btn);
        undoButton = findViewById(R.id.undo_button2);
        sendButton = findViewById(R.id.button_send2);
        modeSpnr = findViewById(R.id.mode_spinner2);
        brt_btn = findViewById(R.id.brightness_btn2);
        contrastButton = findViewById(R.id.contrast_btn2);
        trashButton = findViewById(R.id.trash2);

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

        brt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ShapeDrawingView.backgroundSet || mImageView.bitmap == null)
                {
                    Toast.makeText(getApplicationContext(), "Background not set", Toast.LENGTH_LONG).show();
                    return;
                }
                ShowDialog(mImageView.originalBitmap, "Brightness");
            }
        });

        contrastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ShapeDrawingView.backgroundSet || mImageView.bitmap == null)
                {
                    Toast.makeText(getApplicationContext(), "Background not set", Toast.LENGTH_LONG).show();
                    return;
                }
                ShowDialog(mImageView.originalBitmap, "Contrast");
            }
        });

        ArrayAdapter<String>adapter = new ArrayAdapter<String>(this,
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



       /* modeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mImageView.mCurrentPolygon != null)
                {
                    mImageView.mPolygonen.remove(mImageView.mPolygonen.size() - 1);
                    mImageView.mCurrentPolygon = null;
                    mImageView.reDraw();
                }
                modeBtn.setText(changeMode());
            }
        });*/

        mImageView = findViewById(R.id.boxView2);
        mImageView.mode = "Box";

        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShapeDrawingView boxDr = findViewById(R.id.boxView2);
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

        pickImagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageAction();
            }
        });
        pickImageAction();
    }

    public void pickImageAction()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

                requestPermissions(permission, PERMISSION_CODE);
            } else {
                getImagePath();
            }
        } else {
            getImagePath();
        }
    }

    private void getImagePath()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");                                      //use "image/jpeg" for jpg images only
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImagePath();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the Uri of the selected file
                Uri uri = data.getData();
                assert uri != null;
                Log.d(TAG, "File Uri: " + uri.toString());
                // Get the path
                try {
                    String path = getImagePath2(uri);
                    File imgFile = new File(path);
                    if (imgFile.exists()) {

                        filepath = path;

                        ExifInterface ei = new ExifInterface(path);
                        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED);

                        Bitmap rotatedBitmap = null;
                        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        switch (orientation) {

                            case ExifInterface.ORIENTATION_ROTATE_90:
                                rotatedBitmap = rotateImage(bitmap, 90);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_180:
                                rotatedBitmap = rotateImage(bitmap, 180);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_270:
                                rotatedBitmap = rotateImage(bitmap, 270);
                                break;

                            case ExifInterface.ORIENTATION_NORMAL:
                            default:
                                rotatedBitmap = rotateImage(bitmap, 0);
                        }

                        ShapeDrawingView boxDr = this.findViewById(R.id.boxView2);
                        boxDr.mBoxen.clear();
                        boxDr.mCirclen.clear();
                        boxDr.mCurrentPolygon = null;
                        boxDr.mPolygonen.clear();

                        Circle.total = 0;
                        Polygon.total = 0;
                        Box.total = 0;

                        boxDr.bitmap = rotatedBitmap;
                        boxDr.originalBitmap = rotatedBitmap;
                        ShapeDrawingView.backgroundSet = true;

                        brightnessProgress = 128;
                        contrastProgress = 128;
                        //Picasso.get().load(imgFile).into(mImageView);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @SuppressLint("Recycle")
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        assert parcelFileDescriptor != null;
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
    public String getImagePath2(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        assert cursor != null;
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        //Toast.makeText(this, path, Toast.LENGTH_LONG).show();
        cursor.close();

        return path;
    }
    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
        bitmap = Bitmap.createScaledBitmap(bitmap,mImageView.getWidth(), mImageView.getHeight(), true);
        return bitmap;
    }

    public void ShowDialog(final Bitmap bmp, final String mode){
        //Toast.makeText(this, "Reached ShowDialog", Toast.LENGTH_LONG).show();
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final SeekBar seek = new SeekBar(this);
        seek.setMax(256);
        seek.setKeyProgressIncrement(1);
        if(mode.equals("Brightness"))
            seek.setProgress(brightnessProgress);
        else
            seek.setProgress(contrastProgress);

        popDialog.setIcon(R.drawable.ic_brightness_high_black_24dp);
        if(mode.equals("Brightness"))
            popDialog.setTitle("Please Select Into Your Desired Brightness");
        else
            popDialog.setTitle("Please Select Into Your Desired Contrast");
        popDialog.setView(seek);


        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //mImageView.reDraw(changeBitmapContrastBrightness(bmp, (float) progress / 100f, 1));

                if(mode.equals("Brightness"))
                {
                    ArgbEvaluator evaluator = new ArgbEvaluator();

                    float fraction = progress / 256.0F;

                    int mul = (int) evaluator.evaluate(fraction, 0xFF7F7F7F, 0xFFFFFFFF);
                    int add = (int) evaluator.evaluate(fraction, 0x00000000, 0x00222222);
                    mImageView.reDraw(new LightingColorFilter(mul, add));
                }

                else
                {
                    mImageView.reDraw(changeBitmapContrastBrightness(bmp, (float) progress / 128f, 1));
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                mImageView.reDraw();

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mImageView.reDraw();
                if(mode.equals("Brightness"))
                    brightnessProgress = seekBar.getProgress();
                else
                    contrastProgress = seekBar.getProgress();
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

    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
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
