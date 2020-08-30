package com.example.quranforinjaziandroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
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
    int surahIndex;
    File directory;
    String[] pageNames = new String[604];
    final String folder_main = "Injaazi_quran_images";
    String path, urlToDownload , fileNameWithExtension = "";
    int thePageWas = 0;
    public static final String MY_PREFS_NAME = "LAST_VERSE";
    SharedPreferences.Editor editor;

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
    private final String[] partChecks = {"page-6", "page-112", "page-212", "page-295", "page-365", "page-446", "page-526"};

    private final int[] partBoundaries = {1,107,209,294,360,441,521};
    private GestureDetector gdt;
    private static final int MIN_SWIPPING_DISTANCE = 50;
    private static final int THRESHOLD_VELOCITY = 50;

    private int currentVerse = 0;
    private String surahName = "";
    int toOpenThisPage = 1;

    Button button;
    @SuppressLint("ClickableViewAccessibility")


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_display);

        editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();

        for (int i = 0; i < 604 ; i++)
        {
            pageNames[i] = "page-" + Integer.toString(i);
        }

        //check Permission
        checkPermissions();

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        thePageWas = prefs.getInt("lastVerse", 0);
//        if (thePageWas != 0)
//            loadFileFromExternalFolder(thePageWas, existInPart(thePageWas));




        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            surahName = extra.getString("surahName", "الناس");
            currentVerse = extra.getInt("CurrentVerse");
            surahIndex = extra.getInt("surahIndex");
            //open this page
            toOpenThisPage = ProcessPage.pageNumber(currentVerse, surahIndex - 1);
            editor.putInt("lastVerse", toOpenThisPage);
            editor.apply();

        }

        Log.d("TAG", "onCreate: " + existInPart(toOpenThisPage));



        imageView = findViewById(R.id.quran_page_display);

        gdt = new GestureDetector(new GestureListener());
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                gdt.onTouchEvent(event);
                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        {
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            thePageWas = prefs.getInt("lastVerse", 1);


            if (partExistsInDirectory( Integer.toString(existInPart(thePageWas))  ))//check if it exist in the pages directory)
            {
                loadFileFromExternalFolder(thePageWas, existInPart(thePageWas));

            } else {
                //find the part it falls under
                //download the zip file **use dialog
                //unpack it into the images folder ** use dialog
                //load into the imageView
                fileNameWithExtension =  ("/" + Integer.toString(existInPart(toOpenThisPage)  )+ ".zip");
                urlToDownload = existInPartUrl(toOpenThisPage);

                Intent intent = new Intent(this,DownloadActivity.class );
                intent.putExtra("fileNameWithExtension",fileNameWithExtension);
                intent.putExtra("path",path);
                intent.putExtra("urlToDownload", urlToDownload );

                editor.putInt("lastVerse", toOpenThisPage);
                editor.apply();

                startActivity(intent);
                loadFileFromExternalFolder(toOpenThisPage,existInPart(toOpenThisPage));
            }


            ImageView imageView = findViewById(R.id.quran_page_display);
            imageView.invalidate();
            loadFileFromExternalFolder(thePageWas, existInPart(thePageWas));
        }
    }

    private void loadFileFromExternalFolder(int toOpenThisPage, int existInPart)
    {

        Log.d("TAG", "loadFileFromExternalFolder: " + path + File.separator + Integer.toString(existInPart)  + File.separator + pageNames[toOpenThisPage] + ".png");

        String pathToImage = path + File.separator + Integer.toString(existInPart)  + File.separator + pageNames[toOpenThisPage] + ".png";


        File imgFile = new  File(pathToImage);

        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            ImageView imageView = (ImageView) findViewById(R.id.quran_page_display);

            imageView.setImageBitmap(myBitmap);

        }

    }

    private String existInPartUrl(int toOpenThisPage) {

        int index = 0;

        if (toOpenThisPage >= 521) return Downloadfiles.PART7.getPartUrl();

        for (int i = 0, j = 1; i < 7 && j < 6; i++, j++,index++) {
            if ((toOpenThisPage >= partBoundaries[i]) && (toOpenThisPage < partBoundaries[j])) {
                break;
            }
        }
        return Downloadfiles.values()[index].getPartUrl();
    }

    private boolean partExistsInDirectory(String partName)
    {
        Log.d("TAG", "partExistsInDirectory: " + partName);

        String fullPath = path + "/" + partName;
        File file = new File(fullPath);
        Log.d("TAG", "partExistsInDirectory: " + fullPath);
        return file.exists();
    }

    private int existInPart (int pageIndex)
    {

        int index = 0;

        if (toOpenThisPage >= 521) return  (Downloadfiles.PART7.ordinal() + 1);

        for (int i = 0, j = 1; i < 7 && j < 6; i++, j++, index++) {
            if ((pageIndex >= partBoundaries[i]) && (pageIndex < partBoundaries[j])) {
                break;
            }
        }

        return  (Downloadfiles.values()[index].ordinal() + 1);
        }


        //Write this function last
        private boolean pageExistsInEternalFolder(int pageNumber)
        {
            return false;
        }


        private class GestureListener extends GestureDetector.SimpleOnGestureListener
        {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                if (e1.getX() - e2.getX() > MIN_SWIPPING_DISTANCE && Math.abs(velocityX) > THRESHOLD_VELOCITY) {

                        toOpenThisPage--;

                        if (toOpenThisPage < 1) toOpenThisPage = 1;
                        /* Code that you want to do on swiping left side Load previos page*/
                        loadFileFromExternalFolder(toOpenThisPage, existInPart(toOpenThisPage));
                        editor.putInt("lastVerse", toOpenThisPage);
                        editor.apply();

                    return false;
                } else if (e2.getX() - e1.getX() > MIN_SWIPPING_DISTANCE && Math.abs(velocityX) > THRESHOLD_VELOCITY) {


                    toOpenThisPage++;

                    if (toOpenThisPage > 604) toOpenThisPage = 604;

                    loadFileFromExternalFolder(toOpenThisPage,existInPart(toOpenThisPage));
                    editor.putInt("lastVerse", toOpenThisPage);
                    editor.apply();

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
