package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.example.myfirstapp.R;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button pickImageBtn;
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickImageBtn = findViewById(R.id.pick_activity_btn);
        pickImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
    }

    public void captureImage(View view) {
        Intent intent = new Intent(this, Button2Activity.class);
        startActivity(intent);
    }
    public void drawBoxes(View view) {
        Intent intent = new Intent(this, DragAndDrawActivity.class);
        startActivity(intent);
    }

    public void pickImage()
    {
        Intent intent = new Intent(this, PickImage.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Toast.makeText(this, "Sagar is the coolest!", Toast.LENGTH_LONG).show();
                return true;
            case R.id.feedback:
                Toast.makeText(this, "All hail the mighty Sagar!", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
