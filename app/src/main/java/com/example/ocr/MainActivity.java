package com.example.ocr;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.Bitmap;//
import android.graphics.BitmapFactory;//
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.yalantis.ucrop.UCrop;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import android.Manifest;



public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private OCRManager ocrManager;
    private static final int REQUEST_IMAGE_SELECT = 101;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1001;

    private String recognizedText;
    private TextToSpeech textToSpeech;
    private static final String UTTERANCE_ID = "com.example.ocr.UTTERANCE_ID";
    private Spinner languageSpinner;
    private Button buttonStopTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ocrManager = new OCRManager(this, "eng");

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, this);

        // Check for READ_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        // Button to select image from gallery
        findViewById(R.id.button).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_SELECT);
        });

        // Button to speak recognized text
        Button buttonSpeak = findViewById(R.id.button2);
        buttonSpeak.setOnClickListener(v -> speakOut());

        //stop speech
        buttonStopTTS = findViewById(R.id.button5);
        buttonStopTTS.setOnClickListener(v -> stopTTS());

        // Language spinner
        languageSpinner = findViewById(R.id.spinner4);
        String[] languageOptions = getResources().getStringArray(R.array.language_options);
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, languageOptions);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String languageCode = getLanguageCode(position);
                textToSpeech.setLanguage(new Locale(languageCode));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK && data!= null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
                // Perform OCR
                recognizedText = ocrManager.doOCR(bitmap);

                // Display recognized text
                TextView textView = findViewById(R.id.textView);
                textView.setText(recognizedText);

                // Display the image in an ImageView
                ImageView imageView = findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void stopTTS() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }
    @Override
    protected void onDestroy() {
        // Release TextToSpeech resources
        if (textToSpeech!= null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        // Release OCR resources
        if (ocrManager!= null) {
            ocrManager.onDestroy();
        }
        super.onDestroy();
    }

    // TextToSpeech initialization callback
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language for TextToSpeech
            int result = textToSpeech.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to speak out the recognized text
    private void speakOut() {
        if (textToSpeech!= null &&!TextUtils.isEmpty(recognizedText)) {
            // Configure the utterance for TTS
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    // Optional: Handle TTS start
                }

                @Override
                public void onDone(String utteranceId) {
                    // Optional: Handle TTS completion
                }

                @Override
                public void onError(String utteranceId) {
                    // Optional: Handle TTS error
                }
            });

            // Speak the text
            textToSpeech.speak(recognizedText, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
        }
    }

    // Method to get language code from spinner position
    private String getLanguageCode(int position) {
        String[] languageCodes = getResources().getStringArray(R.array.language_codes);
        return languageCodes[position];
    }
}