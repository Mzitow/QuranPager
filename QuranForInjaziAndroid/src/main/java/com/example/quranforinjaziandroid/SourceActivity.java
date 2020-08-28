package com.example.quranforinjaziandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SourceActivity extends AppCompatActivity {


    ImageView imageView;
    int pageIndex = 0;
    File directory;
    String[] pageNames = new String[604];
    final String folder_main = "Injaazi_quran_images";
    String path = "";

    //permissions
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 10089;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    public enum Downloadfiles {
        PART1("https://injaazy.com/Quran_Images/1.zip"),
        PART2("https://injaazy.com/Quran_Images/2.zip"),
        PART3("https://injaazy.com/Quran_Images/3.zip"),
        PART4("https://injaazy.com/Quran_Images/4.zip"),
        PART5("https://injaazy.com/Quran_Images/5.zip"),
        PART6("https://injaazy.com/Quran_Images/6.zip"),
        PART7("https://injaazy.com/Quran_Images/7.zip");

        private final String fileName;

        Downloadfiles(String code) {
            this.fileName = code;
        }

        public String getPartUrl() {
            return this.fileName;
        }
    }

    //int[] partBoundaries = {1,107,209,294,360,441,521};
    private final String[] partChecks = {"page_6", "page_112", "page_212", "page_295", "page_365", "page_446", "page_526"};

    private final int[] partBoundaries = {1,107,209,294,360,441,521};
    private GestureDetector gdt;
    private static final int MIN_SWIPPING_DISTANCE = 50;
    private static final int THRESHOLD_VELOCITY = 50;

    private int currentVerse = 0;
    private int currentChapterIndex = 0;
    private String surahName = "";
    int toOpenThisPage = 1;

    Button button;
    @SuppressLint("ClickableViewAccessibility")


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_display);

        for (int i = 0; i < 604 ; i++)
        {
            pageNames[i] = "page-" + Integer.toString(i);
            System.out.println(pageNames[i]);
        }
        //check Permission
        checkPermissions();

        button = findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SourceActivity.this, DownloadActivity.class);
                intent.putExtra("path", path);
                startActivity(intent);

            }
        });

        Bundle extra = getIntent().getExtras();
        if (extra != null) {

            surahName = extra.getString("surahName", "الناس");
            currentVerse = extra.getInt("CurrentVerse");
            currentChapterIndex = extra.getInt("currentChapterIndex", 114);
            //open this page
            toOpenThisPage = ProcessPage.pageNumber(currentVerse, surahName);
        }



        if (2 > 8)//check if it exist in the pages directory)
        {
            //loadIntoImageView
        } else {
            //find the part it falls under
            //download the zip file **use dialog
            //unpack it into the images folder ** use dialog
            //load into the imageView
        }




//        for(int i = 0; i < bitmaps.length; i++)
//        {
//            Bitmap bm = BitmapFactory.decodeResource(getResources(), this.getResources().getIdentifier(pageNames[i], "raw", this.getPackageName()));
//            bitmaps[i] = bm;
//            saveToInternalStorage(bm,pageNames[i]);
//        }

//        Bitmap bm = BitmapFactory.decodeResource(getResources(), this.getResources().getIdentifier("page_2", "raw", this.getPackageName()));
//        saveToInternalStorage(bm ,"page_2");

        imageView = findViewById(R.id.quran_page_display);
        //imageView.setImageBitmap(loadImageFromStorage(directory.getPath(), pageNames[pageIndex]));


        //on swipe listener
        gdt = new GestureDetector(new GestureListener());
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                gdt.onTouchEvent(event);
                return true;
            }
        });


    }

    private String existInPartUrl(int toOpenThisPage) {

        int index = 0;

        if (toOpenThisPage >= 521) return Downloadfiles.PART7.getPartUrl();

        for (int i = 0, j = 1; i < 7 && j < 6; i++, j++) {
            if ((toOpenThisPage >= partBoundaries[i]) && (toOpenThisPage < partBoundaries[j])) {
                index = i;
                break;
            }
        }
        return Downloadfiles.values()[index].getPartUrl();
    }

        private int existInPart (int pageIndex)
        {

            int index = 0;

            if (toOpenThisPage >= 521) return Downloadfiles.PART7.ordinal();

            for (int i = 0, j = 1; i < 7 && j < 6; i++, j++) {
                if ((toOpenThisPage >= partBoundaries[i]) && (toOpenThisPage < partBoundaries[j])) {
                    index = i;
                    break;
                }
            }

            return Downloadfiles.values()[index].ordinal();

        }

        private String saveToExternalFolder (Bitmap bitmapImage, String pageNumber)
        {
            return null;
        }

        private boolean existsInEternalStorage(String pageNumber)
        {
            return false;
        }

        private Bitmap loadImageFromFolder (String path, String pageNumber)
        {
            return null;
        }

        private class GestureListener extends GestureDetector.SimpleOnGestureListener
        {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                if (e1.getX() - e2.getX() > MIN_SWIPPING_DISTANCE && Math.abs(velocityX) > THRESHOLD_VELOCITY) {
                    Toast.makeText(getApplicationContext(), "You have swipped left side", Toast.LENGTH_SHORT).show();
                    /* Code that you want to do on swiping left side Load previos page*/
                    if (pageIndex > 0) pageIndex--;
                    if (pageIndex == 4) pageIndex = 3;
                    //imageView.setImageBitmap(loadImageFromStorage(directory.getPath(), pageNames[pageIndex]));


//                String uri = "@drawable/"+ pageNames[pageIndex];  // where myresource (without the extension) is the file
//                int imageResource = getResources().getIdentifier(uri, null, getPackageName());
//                Drawable res = getResources().getDrawable(imageResource);
//                imageView.setImageDrawable(res);


                    Log.d("OnSwipePage", "onFling: Left " + pageIndex + " " + directory.getPath());


                    return false;
                } else if (e2.getX() - e1.getX() > MIN_SWIPPING_DISTANCE && Math.abs(velocityX) > THRESHOLD_VELOCITY) {
                    Toast.makeText(getApplicationContext(), "You have swipped right side", Toast.LENGTH_SHORT).show();

                    if (pageIndex < 5) pageIndex++;
                    //imageView.setImageBitmap(loadImageFromStorage(directory.getPath(),pageNames[pageIndex]));

//                String uri = "@drawable/"+ pageNames[pageIndex];  // where myresource (without the extension) is the file
//                int imageResource = getResources().getIdentifier(uri, null, getPackageName());
//                Drawable res = getResources().getDrawable(imageResource);
//                imageView.setImageDrawable(res);


                    Log.d("OnSwipePage", "onFling: Right " + pageIndex);
                    /* Code that you want to do on swiping right side*/
                    return false;
                }
                return false;
            }
        }


        /**
         * Checks the dynamically-controlled permissions and requests missing permissions from end user.
         */
        protected void checkPermissions ()
        {
            final List<String> missingPermissions = new ArrayList<String>();
            // check all required dynamic permissions
            for (final String permission : REQUIRED_SDK_PERMISSIONS) {
                final int result = ContextCompat.checkSelfPermission(this, permission);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(permission);
                }
            }
            if (!missingPermissions.isEmpty()) {
                // request all missing permissions
                final String[] permissions = missingPermissions
                        .toArray(new String[missingPermissions.size()]);
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
                Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
                onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                        grantResults);
            }
        }

        @Override
        public void onRequestPermissionsResult ( int requestCode, @NonNull String permissions[],
        @NonNull int[] grantResults)
        {
            switch (requestCode) {
                case REQUEST_CODE_ASK_PERMISSIONS:
                    for (int index = permissions.length - 1; index >= 0; --index) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            // exit the app if one permission is not granted
                            Toast.makeText(this, "Required permission '" + permissions[index]
                                    + "' not granted, exiting", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }
                    // all permissions were granted
                    createInjaaziDirectory();
                    break;
            }
        }

        private String createInjaaziDirectory()
        {

            File quranPages = new File(Environment.getExternalStorageDirectory(), folder_main);
            if (!quranPages.exists()) {
                quranPages.mkdirs();
                path = quranPages.getAbsolutePath();
            }
            else
            {
                path = quranPages.getAbsolutePath();
            }
            Log.d("path", "createInjaaziDirectory: " + path);
            return path;
        }

}
