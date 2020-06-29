package com.example.imageproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

public class DateView extends AppCompatActivity {

    private Button mButton_home_in_date;
    private Button mButton_face_check_in_date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_view);

        mButton_home_in_date = findViewById(R.id.button_home_in_date);
        mButton_face_check_in_date = findViewById(R.id.button_face_check_in_date);

    }
}