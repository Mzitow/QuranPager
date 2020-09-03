package com.example.quranforinjaziandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SourceActivity extends AppCompatActivity {


    ImageView imageView;
    int pageIndex = 0;
    int surahIndex;
    File directory;
    String[] pageNames = new String[604];
    final String folder_main = ".Injaazi_quran_images";
    String path, urlToDownload , fileNameWithExtension = "";
    int thePageWas = 0;
    ImageView openMistakeCards , closeMistakeCards;

    //bottom sheet;
    FrameLayout tajwedCard;
    FrameLayout mistakeCard;
    FrameLayout alertCard;


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

    private final int[] partBoundaries = {0,106,207,292,358,439,519};
    private GestureDetector gdt;
    private static final int MIN_SWIPPING_DISTANCE = 50;
    private static final int THRESHOLD_VELOCITY = 50;

    private int currentVerse = 0;
    public int mistake = 0, alert = 0, tajweed = 0;
    private String surahName = "";
    int toOpenThisPage = 1;

    Button button;
    @SuppressLint("ClickableViewAccessibility")


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_display);

        imageView = findViewById(R.id.quran_page_display);
        tajwedCard = findViewById(R.id.tajwed);
        alertCard = findViewById(R.id.alert);
        mistakeCard = findViewById(R.id.mistake);



        for (int i = 0; i < 604 ; i++)
        {
            pageNames[i] = "page-" + Integer.toString((i+1));
        }

        //check Permission
        checkPermissions();


        Bundle extra = getIntent().getExtras();
        if (extra != null) {

            mistake = extra.getInt("mistake", 0);
            alert = extra.getInt("alert",0);
            tajweed = extra.getInt("tajweed",0);

            surahName = extra.getString("surahName", "الناس");
            currentVerse = extra.getInt("CurrentVerse");
            surahIndex = extra.getInt("surahIndex");
            //open this page
            toOpenThisPage = ProcessPage.pageNumber(currentVerse, surahIndex - 1);

        }

        ((TextView) tajwedCard.findViewById(R.id.number)).setText(String.format(Locale.ENGLISH, "%02d", tajweed));
        ((TextView) mistakeCard.findViewById(R.id.number)).setText(String.format(Locale.ENGLISH, "%02d", mistake));
        ((TextView) alertCard.findViewById(R.id.number)).setText(String.format(Locale.ENGLISH, "%02d", alert));

        final ImageView imageView = findViewById(R.id.quran_page_display);
        imageView.invalidate();
        loadFileFromExternalFolder(toOpenThisPage, existInPart(toOpenThisPage));



        //my Animations
        final Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        final Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
        final ConstraintLayout mistakeCards = findViewById(R.id.bottom_sheet);



        manageMyMistakeCards();

        openMistakeCards = findViewById(R.id.show_peformance_cards);
        openMistakeCards.setVisibility(View.VISIBLE);

        openMistakeCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            mistakeCards.setVisibility(View.VISIBLE);
            //imageView.startAnimation(bottomUp);
            mistakeCards.startAnimation(bottomUp);
            openMistakeCards.setVisibility(View.GONE);

            }
        });



        closeMistakeCards = findViewById(R.id.hide_bottom_cards);
        closeMistakeCards.setVisibility(View.VISIBLE);
        closeMistakeCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mistakeCards.setVisibility(View.GONE);
                mistakeCards.startAnimation(bottomDown);
                //imageView.startAnimation(bottomDown);
                openMistakeCards.setVisibility(View.VISIBLE);


            }
        });



        gdt = new GestureDetector(new GestureListener());
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                gdt.onTouchEvent(event);
                return true;
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    private void manageMyMistakeCards() {

        ((TextView) tajwedCard.findViewById(R.id.name)).setText("تجويد");
        ((TextView) mistakeCard.findViewById(R.id.name)).setText("خطأ");
        ((TextView) alertCard.findViewById(R.id.name)).setText("تنبيه");

        alertCard.setOnTouchListener(new View.OnTouchListener() {
            int alert = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {


                alert = Integer.parseInt(((TextView) alertCard.findViewById(R.id.number)).getText().toString().trim());

                Rect outRect = new Rect();
                alertCard.getGlobalVisibleRect(outRect);

                // calculate new bottom, to be used for upper part.
                int bottom = (int) (outRect.bottom * 0.95);

                Rect upperRec = new Rect(outRect.left, outRect.top, outRect.right, bottom);

                if (upperRec.contains((int) event.getRawX(), (int) event.getRawY())) {
                    alert++;
                    ((TextView) alertCard.findViewById(R.id.number)).setText(String.format(Locale.ENGLISH, "%02d", alert));
                } else {
                    if (alert > 0) {
                        --alert;
                        ((TextView) alertCard.findViewById(R.id.number)).setText(String.format(Locale.ENGLISH, "%02d", alert));
                    }

                }

                SourceActivity.this.alert = alert;
                Log.d("TAG3", "ALERT_CARD: " +  SourceActivity.this.alert);

                return false;
            }
        });

        mistakeCard.setOnTouchListener(new View.OnTouchListener() {
            int mistake = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mistake = Integer.parseInt(((TextView) mistakeCard.findViewById(R.id.number)).getText().toString().trim());

                Rect outRect = new Rect();
                mistakeCard.getGlobalVisibleRect(outRect);

                // calculate new bottom, to be used for upper part.
                int bottom = (int) (outRect.bottom * 0.95);

                Rect upperRec = new Rect(outRect.left, outRect.top, outRect.right, bottom);

                if (upperRec.contains((int) event.getRawX(), (int) event.getRawY())) {
                    ++mistake;
                    ((TextView) mistakeCard.findViewById(R.id.number)).setText(String.format(Locale.ENGLISH, "%02d", mistake));
                } else {
                    if (mistake > 0) {
                        --mistake;
                        ((TextView) mistakeCard.findViewById(R.id.number)).setText(String.format(Locale.ENGLISH, "%02d", mistake));
                    }

                }
                SourceActivity.this.mistake = mistake;
                return false;
            }
        });

        tajwedCard.setOnTouchListener(new View.OnTouchListener() {

            int tajweed =0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                tajweed = Integer.parseInt(((TextView) tajwedCard.findViewById(R.id.number)).getText().toString().trim());

                Rect outRect = new Rect();
                tajwedCard.getGlobalVisibleRect(outRect);

                // calculate new bottom, to be used for upper part.
                int bottom = (int) (outRect.bottom * 0.95);

                Rect upperRec = new Rect(outRect.left, outRect.top, outRect.right, bottom);

                if (upperRec.contains((int) event.getRawX(), (int) event.getRawY())) {
                    ++tajweed;
                    ((TextView) tajwedCard.findViewById(R.id.number)).setText(String.format(Locale.ENGLISH, "%02d", tajweed));
                } else {
                    if (tajweed > 0) {
                        --tajweed;
                        ((TextView) tajwedCard.findViewById(R.id.number)).setText(String.format(Locale.ENGLISH, "%02d", tajweed));
                    }

                }
                SourceActivity.this.tajweed = tajweed;
                return false;
            }
        });


    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("tajweed",SourceActivity.this.tajweed);
        intent.putExtra("alert", SourceActivity.this.alert);
        intent.putExtra("mistake",SourceActivity.this.mistake);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        {
            ImageView imageView = findViewById(R.id.quran_page_display);
            imageView.invalidate();
            loadFileFromExternalFolder(toOpenThisPage, existInPart(toOpenThisPage));

           DownloadActivity.deleteZipFile(path + "/" + Integer.toString(existInPart(toOpenThisPage)) + ".zip");

            if (partExistsInDirectory( Integer.toString(existInPart(toOpenThisPage))  ))//check if it exist in the pages directory)
            {
                loadFileFromExternalFolder(toOpenThisPage, existInPart(toOpenThisPage));

            } else {
                //find the part it falls under
                //download the zip file **use dialog
                //unpack it into the images folder ** use dialog
                //load into the imageView
                fileNameWithExtension =  ("/" + Integer.toString(existInPart(toOpenThisPage) )+ ".zip");
                urlToDownload = existInPartUrl(toOpenThisPage);

                Intent intent = new Intent(this,DownloadActivity.class );
                intent.putExtra("fileNameWithExtension",fileNameWithExtension);
                intent.putExtra("path",path);
                intent.putExtra("urlToDownload", urlToDownload );

                startActivity(intent);
            }

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

       // int[] partBoundaries = {0,106,207,292,358,439,519};

        if (toOpenThisPage >= 519) return  (Downloadfiles.PART7.ordinal() + 1);

        for (int i = 0, j = 1; i < 7 && j < 6; i++, j++, index++) {
            if ((pageIndex >= partBoundaries[i]) && (pageIndex < partBoundaries[j])) {
                break;
            }
        }

        return  (Downloadfiles.values()[index].ordinal() + 1);
        }


        private class GestureListener extends GestureDetector.SimpleOnGestureListener
        {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                if (e1.getX() - e2.getX() > MIN_SWIPPING_DISTANCE && Math.abs(velocityX) > THRESHOLD_VELOCITY) {
                    toOpenThisPage--;
                    if (pageFileExistsInStorage(toOpenThisPage))
                    {
                        if (toOpenThisPage <= 0) toOpenThisPage = 0;

                        /* Code that you want to do on swiping left side Load previos page*/
                        loadFileFromExternalFolder(toOpenThisPage, existInPart(toOpenThisPage));
                    }
                    else
                    {
                        int lastExistingPage = toOpenThisPage + 1;
                        loadFileFromExternalFolder(lastExistingPage,existInPart(lastExistingPage));
                        toOpenThisPage = lastExistingPage;
                    }
                    return false;
                } else if (e2.getX() - e1.getX() > MIN_SWIPPING_DISTANCE && Math.abs(velocityX) > THRESHOLD_VELOCITY) {

                    toOpenThisPage++;

                    if (pageFileExistsInStorage(toOpenThisPage)) {

                        if (toOpenThisPage >= 603) toOpenThisPage = 603;

                        loadFileFromExternalFolder(toOpenThisPage, existInPart(toOpenThisPage));
                    } else
                    {
                        int lastExistingPage = toOpenThisPage - 1;
                        loadFileFromExternalFolder(lastExistingPage,existInPart(lastExistingPage));
                        toOpenThisPage = lastExistingPage;
                    }
                    /* Code that you want to do on swiping right side*/
                    return false;
                }
                return false;
            }
        }

    private boolean pageFileExistsInStorage(int toOpenThisPage) {

        int partID = existInPart(toOpenThisPage);
        String pathToFile = path + File.separator + Integer.toString(partID)  + File.separator + pageNames[toOpenThisPage] + ".png";
        File file = new File(pathToFile);
        Log.d("TAG", "pageFileExistsInDirectory: " + pathToFile +" "+ file.exists());
        return file.exists();

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
                final String[] permissions = missingPermissions.toArray(new String[missingPermissions.size()]);
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
