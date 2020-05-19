package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sendMessage2(View view) {
        Intent intent = new Intent(this, Button2Activity.class);
        startActivity(intent);
    }
    public void sendMessage3(View view) {
        Intent intent = new Intent(this, DragAndDrawActivity.class);
        startActivity(intent);
    }
}
