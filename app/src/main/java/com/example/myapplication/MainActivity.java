package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import android.Manifest;
public class MainActivity extends AppCompatActivity {
    Button ssbtn;
    TextView sensonAngle;
    File path;

    //shake event
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ssbtn = findViewById(R.id.ssbtn);
        sensonAngle = findViewById(R.id.sensonAngle);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor shakeeventsensor =
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Sensor rotationVectorSensor =
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mAccel = 10f;
        mAccelCurrent = sensorManager.GRAVITY_EARTH;
        mAccelLast = sensorManager.GRAVITY_EARTH;

        SensorEventListener seslistener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];
                    float z = sensorEvent.values[2];
                    mAccelLast = mAccelCurrent;
                    mAccelCurrent = (float) Math.sqrt((double) (z * z));
                    float delta = mAccelCurrent - mAccelLast;
                    mAccel = mAccel * 0.9f + delta;
                    if (mAccel > 12) {
                        Toast.makeText(getApplicationContext(), "Shake event detected in z-axis" + "current" + mAccelCurrent + "mAccel" + mAccel, Toast.LENGTH_SHORT).show();
                    }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        // Create a listener
        SensorEventListener rvListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                // More code goes here
                float[] rotationMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(
                        rotationMatrix, sensorEvent.values);

                // Remap coordinate system
                float[] remappedRotationMatrix = new float[16];
                SensorManager.remapCoordinateSystem(rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        remappedRotationMatrix);
// Convert to orientations
                float[] orientations = new float[3];
                SensorManager.getOrientation(remappedRotationMatrix, orientations);
                for (int i = 0; i < 3; i++) {
                    orientations[i] = (float) (Math.toDegrees(orientations[i]));
                }
                sensonAngle.setText(Float.toString(Math.round(orientations[2])));
                if (orientations[2] > 45) {
                    getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.yellow_300));
                } else if (orientations[2] < -45) {
                    getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.purple_300));
                } else if (Math.abs(orientations[2]) < 10) {
                    getWindow().getDecorView().setBackgroundColor(Color.WHITE);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }

            ;
        };
// Register it
        sensorManager.registerListener(rvListener,
                rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Register it
        sensorManager.registerListener(seslistener,
                shakeeventsensor, SensorManager.SENSOR_DELAY_NORMAL);

        ssbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                // create bitmap screen capture
                View v1 = getWindow().getDecorView().getRootView();
                v1.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                v1.setDrawingCacheEnabled(false);
                savescreenshot(bitmap);
                // Toast.makeText(MainActivity.this, path.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savescreenshot(Bitmap bitmap) {
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Image_" + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "Screenshots");
                Uri imageuri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(imageuri));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Objects.requireNonNull(fos);

                Toast.makeText(this, "Screenshot saved successfully", Toast.LENGTH_SHORT).show();

            }
        } catch (Exception e) {
            Toast.makeText(this, "Error occured" + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }
}