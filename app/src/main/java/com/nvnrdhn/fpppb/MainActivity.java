package com.nvnrdhn.fpppb;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;
import com.nvnrdhn.fpppb.api.FaceData;
import com.nvnrdhn.fpppb.api.UtilsApi;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    TextView anger, disgust, fear, happiness, neutral, sadness, surprised;
    Button predict;
    ProgressBar loading;
    String mCurrentPhotoPath;
    private static final int REQUEST_CAMERA = 1234;
    private static final int CAMERA = 1235;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        anger = findViewById(R.id.anger);
        disgust = findViewById(R.id.disgust);
        fear = findViewById(R.id.fear);
        happiness = findViewById(R.id.happiness);
        neutral = findViewById(R.id.neutral);
        sadness = findViewById(R.id.sadness);
        surprised = findViewById(R.id.surprised);
        predict = findViewById(R.id.predict);
        loading = findViewById(R.id.loading);
        UtilsApi.getAPIService(getString(R.string.KEY));
        predict.setOnClickListener(v -> requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePhotoFromCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CANCELED) return;

        if (requestCode == CAMERA) {
            try {
                Uri uri = Uri.parse(mCurrentPhotoPath);
                File file = new File(uri.getPath());
                predict(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void takePhotoFromCamera() {
        File temp = null;
        try {
            temp = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (temp != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), getPackageName()+".fileprovider", temp));
            startActivityForResult(intent, CAMERA);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "FP-PPB_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpeg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void predict(File photo) {
        loading.setVisibility(View.VISIBLE);
        predict.setVisibility(View.GONE);
        UtilsApi.apiService.detect(
                MultipartBody.Part.createFormData("image", "Attach", RequestBody.create(MediaType.parse("image/*"), photo))
//                photo
        ).enqueue(new Callback<FaceData>() {
            @Override
            public void onResponse(Call<FaceData> call, Response<FaceData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FaceData face = response.body();
                    anger.setText("Marah: "+face.output.Angry);
                    disgust.setText("Jijik: "+face.output.Disgust);
                    fear.setText("Takut: "+face.output.Fear);
                    happiness.setText("Bahagia: "+face.output.Happy);
                    neutral.setText("Netral: "+face.output.Neutral);
                    sadness.setText("Sedih: "+face.output.Sad);
                    surprised.setText("Kaget: "+face.output.Surprise);
                    Toast.makeText(MainActivity.this, face.message, Toast.LENGTH_SHORT).show();
                }
                else Snackbar.make(findViewById(android.R.id.content), "Error: "+response.code(), Snackbar.LENGTH_LONG).show();

                loading.setVisibility(View.GONE);
                predict.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<FaceData> call, Throwable t) {
                t.printStackTrace();
                loading.setVisibility(View.GONE);
                predict.setVisibility(View.VISIBLE);
            }
        });
    }
}
