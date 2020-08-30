package com.example.quranpagerforinjazi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.quranforinjaziandroid.SourceActivity;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent intent = new Intent(this, SourceActivity.class);
        intent.putExtra("surahIndex" , 5);
        intent.putExtra("CurrentVerse", 38);
        startActivity(intent);
    }

}