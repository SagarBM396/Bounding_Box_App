package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
}
