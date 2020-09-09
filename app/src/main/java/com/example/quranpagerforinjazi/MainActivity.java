package com.example.quranpagerforinjazi;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.quranforinjaziandroid.ProcessPage;
import com.example.quranforinjaziandroid.SourceActivity;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_FOR_CARD_INPUTS = 970;
    int tajweed = 0, alert = 0,mistake = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent intent = new Intent(this, SourceActivity.class);
        intent.putExtra("surahIndex" , 3);
        intent.putExtra("tajweed",5);
        intent.putExtra("CurrentVerse", 100);
        startActivityForResult(intent,REQUEST_FOR_CARD_INPUTS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "Tajweed: " + tajweed + " Alert: " + alert + " Mistake(s): " + mistake , Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case REQUEST_FOR_CARD_INPUTS:
                if (resultCode == Activity.RESULT_OK)
                {

                    tajweed = data.getIntExtra("tajweed",0);
                    alert = data.getIntExtra("alert",0);
                    mistake = data.getIntExtra("mistake",0);
                    Log.d("TAG3", "onActivityResult: " + tajweed + " " + mistake + " " + alert);
                }
               break;

        }
    }
}