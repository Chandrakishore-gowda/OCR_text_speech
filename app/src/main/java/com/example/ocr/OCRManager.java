package com.example.ocr;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OCRManager {

    private TessBaseAPI tessBaseAPI;

    public OCRManager(Context context, String language) {
        tessBaseAPI = new TessBaseAPI();

        // Copy eng.traineddata from assets/tessdata to app's internal storage
        String datapath = context.getFilesDir() + "/tesseract/";
        String trainedDataPath = datapath + "/tessdata/";

        // Ensure the destination directory exists
        File tessdataDir = new File(trainedDataPath);
        if (!tessdataDir.exists()) {
            tessdataDir.mkdirs();
        }

        // Check if eng.traineddata already exists in the destination
        if (!new File(trainedDataPath + "eng.traineddata").exists()) {
            try {
                // Copy the file from assets to the destination
                AssetManager assetManager = context.getAssets();
                InputStream inStream = assetManager.open("tessdata/eng.traineddata");
                OutputStream outStream = new FileOutputStream(trainedDataPath + "eng.traineddata");
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, read);
                }
                inStream.close();
                outStream.flush();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Initialize Tesseract with the provided datapath and language code
        tessBaseAPI.init(datapath, language);
    }

    public String doOCR(Bitmap bitmap) {
        tessBaseAPI.setImage(bitmap);
        String result = tessBaseAPI.getUTF8Text();
        return result;
    }

    public void onDestroy() {
        tessBaseAPI.end();
    }
}
