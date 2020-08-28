package com.example.quranforinjaziandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class DownloadActivity extends AppCompatActivity {

    public ProgressBar bar;
    TextView info, sizeDisplay;
    Button doneBtn;
    String path, partToDownload = "";
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);


         bar = findViewById(R.id.downloadProgress);
//        info = findViewById(R.id.info_text);
//        sizeDisplay = findViewById(R.id.progress_percentage_display);
//        doneBtn = findViewById(R.id.button);


        Bundle extra = getIntent().getExtras();
        if (extra != null) {

            //main path + filename + url to download
            path = extra.getString("path", Environment.getExternalStorageDirectory().getAbsolutePath());
            partToDownload = extra.getString("path", "");

        }

        Decompress decompress = new Decompress(path + "/1.zip", path,this);
        decompress.execute();


        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("جار تحميل ملفات...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);


        //downloadRequiredFiles("https://injaazy.com/Quran_Images/1.zip");

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
                output = new FileOutputStream(path + "/1.zip");

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
            if (result != null)
                Toast.makeText(context,"Download error: " + result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();

            Decompress decompress = new Decompress("/1.zip", path + "/1.zip" ,context);
            decompress.execute();
                finish();
        }

    }

    //Unzipping the file

    public class Decompress extends AsyncTask<Void, Integer, Integer> {

        private final static String TAG = "Decompress";
        private String zipFile;
        private String location;

        ProgressDialog myProgressDialog;
        Context ctx;

        public Decompress(String zipFile, String location, Context ctx) {
            super();
            this.zipFile = zipFile;
            this.location = location;
            this.ctx = ctx;
            dirChecker("");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            myProgressDialog = new ProgressDialog(ctx);
            myProgressDialog.setMessage("Please Wait... Unzipping");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params){
            int count = 0;

            try  {
                ZipFile zip = new ZipFile(zipFile);
                myProgressDialog.setMax(zip.size());
                FileInputStream fin = new FileInputStream(zipFile);
                ZipInputStream zin = new ZipInputStream(fin);
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {

                    Log.v("Decompress", "Unzipping " + ze.getName());
                    if( ze.isDirectory()) {
                        dirChecker(ze.getName());
                    } else {
                        FileOutputStream fout = new FileOutputStream(location + ze.getName());

                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zin.read(buffer)) != -1) {
                            fout.write(buffer, 0, len);
                            count++;
                            publishProgress(count);// Here I am doing the update of my progress bar
                        }
                        fout.close();
                        zin.closeEntry();

                    }
                }
                zin.close();
            } catch(Exception e) {
                Log.e("Decompress", "unzip", e);
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            myProgressDialog.setProgress(progress[0]); //Since it's an inner class, Bar should be able to be called directly
        }

        protected void onPostExecute(Integer... result) {
            Log.i(TAG, "Completed. Total size: "+ result);
            if(myProgressDialog != null && myProgressDialog.isShowing()){
                myProgressDialog.dismiss();
            }
        }

        private void dirChecker(String dir)
        {
            File f = new File(location + dir);
            if(!f.isDirectory())
            {
                f.mkdirs();
            }
        }
    }
}