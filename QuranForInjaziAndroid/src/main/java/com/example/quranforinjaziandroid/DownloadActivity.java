package com.example.quranforinjaziandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadActivity extends AppCompatActivity {



    static String path, urlToDownload , fileNameWithExtension = "";
    ProgressDialog mProgressDialog;
    private int toOpenThisPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);


        Bundle extra = getIntent().getExtras();
        if (extra != null) {

            //main path(path) + filename("/1.zip") + url to download

            path = extra.getString("path", Environment.getExternalStorageDirectory().getAbsolutePath());
            fileNameWithExtension = extra.getString("fileNameWithExtension","");
            urlToDownload = extra.getString("urlToDownload", SourceActivity.Downloadfiles.PART1.getPartUrl());

            Log.d("TAG2", "onCreate: " + fileNameWithExtension + "      " + urlToDownload + "   " + path);
        }



//        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("جار تحميل ملفات...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);

        try {
            downloadAndUnzip();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void downloadAndUnzip() throws IOException {
        //download first
        //the method should take the url of the part
        downloadRequiredFiles(urlToDownload);
    }

    private void downloadRequiredFiles(String partToDownload) {
        final DownloadTask downloadTask = new DownloadTask(this);
        downloadTask.execute(partToDownload);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true); //cancel the task
            }
        });

    }

    // usually, subclasses of AsyncTask are declared inside the activity class.
    // that way, you can easily modify the UI thread from here
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();

                output = new FileOutputStream(path + fileNameWithExtension);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();


                Zipper.UnzippingClass unzippingClass = new Zipper.UnzippingClass();
                try {
                    Log.d("Dangarous", "onPostExecute: " + path + fileNameWithExtension);

                    unzippingClass.unzip(DownloadActivity.this, path + fileNameWithExtension, path);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    deleteZipFile(path + fileNameWithExtension);
                    finish();
                }

                }
        }

    public static void deleteZipFile(String s) {
        File fdelete = new File(s + fileNameWithExtension);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.d("Deleted", "deleteZipFile: " + s + fileNameWithExtension);
            } else {
                Log.d("Deleted N", "deleteZipFile: " + s + fileNameWithExtension);
            }
        }
        }

    }
