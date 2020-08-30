package com.example.quranforinjaziandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

    class Zipper {

         public static class UnzippingClass {

        private ProgressDialog mProgressDialog;
        String StorezipFileLocation = "";
        String DirectoryName = "";

        public void unzip(Context context, String zipFileLocation, String directoryName) throws IOException {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage("جار فك ملفات...");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            StorezipFileLocation = zipFileLocation;
            DirectoryName = directoryName;

            new UnZipTask().execute(zipFileLocation, directoryName);

        }

        public class UnZipTask extends AsyncTask<String, Void, Boolean> {
            @SuppressWarnings("rawtypes")
            @Override
            protected Boolean doInBackground(String... params) {
                String filePath = params[0];
                String destinationPath = params[1];

                File archive = new File(filePath);
                try {
                    ZipFile zipfile = new ZipFile(archive);
                    for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                        ZipEntry entry = (ZipEntry) e.nextElement();
                        unzipEntry(zipfile, entry, destinationPath);
                    }

                    UnzipUtil d = new UnzipUtil(StorezipFileLocation, DirectoryName);
                    d.unzip();

                } catch (Exception e) {
                    return false;
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                mProgressDialog.dismiss();
            }


            private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir) throws IOException {

                if (entry.isDirectory()) {
                    createDir(new File(outputDir, entry.getName()));
                    return;
                }

                File outputFile = new File(outputDir, entry.getName());
                if (!outputFile.getParentFile().exists()) {
                    createDir(outputFile.getParentFile());
                }

                // Log.v("", "Extracting: " + entry);
                BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

                try {

                } finally {
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                }
            }

            private void createDir(File dir) {
                if (dir.exists()) {
                    return;
                }
                if (!dir.mkdirs()) {
                    throw new RuntimeException("Can not create dir " + dir);
                }
            }
        }
    }


    public static class UnzipUtil
    {
        private String zipFile;
        private String location;

        public UnzipUtil(String zipFile, String location)
        {
            this.zipFile = zipFile;
            this.location = location;
            dirChecker("");
        }

        public void unzip()
        {
            try
            {
                FileInputStream fin = new FileInputStream(zipFile);
                ZipInputStream zin = new ZipInputStream(fin);
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null)
                {
                    Log.v("Decompress", "Unzipping " + ze.getName());

                    if(ze.isDirectory())
                    {
                        dirChecker(ze.getName());
                    }
                    else
                    {
                        FileOutputStream fout = new FileOutputStream(location +"/"+ ze.getName());

                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zin.read(buffer)) != -1)
                        {
                            fout.write(buffer, 0, len);
                        }
                        fout.close();

                        zin.closeEntry();

                    }

                }
                zin.close();
            }
            catch(Exception e)
            {
                Log.e("Decompress", "unzip", e);
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