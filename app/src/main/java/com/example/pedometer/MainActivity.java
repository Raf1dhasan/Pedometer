package com.example.pedometer;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Request code for activity recognition permission
    private static final int PERMISSION_REQUEST_CODE = 100;
    private SensorManager sensorManager; // Manages sensors
    private Sensor stepSensor; // Step detector sensor
    private TextView stepCountTextView; // Displays the step count
    private Button startButton; // Button to start/stop step tracking
    private boolean isTracking = false; // Tracks if step tracking is active
    private int stepCount = 0; // Holds the step count

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        stepCountTextView = findViewById(R.id.stepCountTextView);
        startButton = findViewById(R.id.startButton);

        // Initialize SensorManager and step detector sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // Check if step sensor is available
        if (stepSensor == null) {
            stepCountTextView.setText("Step sensor not available!");
        }

        // Load saved step count from SharedPreferences
        loadStepCount();

        // Set button click listener to toggle tracking
        startButton.setOnClickListener(v -> toggleTracking());
    }

    // Toggles tracking on/off based on isTracking flag
    private void toggleTracking() {
        if (isTracking) {
            stopTracking();
        } else {
            startTracking();
        }
    }

    // Starts step tracking by registering the sensor listener
    private void startTracking() {
        // Check for necessary permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
            isTracking = true;
            startButton.setText("Stop Tracking"); // Update button text
        } else {
            // Request permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PERMISSION_REQUEST_CODE);
        }
    }

    // Stops step tracking by unregistering the sensor listener
    private void stopTracking() {
        sensorManager.unregisterListener(this, stepSensor);
        isTracking = false;
        startButton.setText("Start Tracking"); // Update button text
    }

    // Called when the sensor detects a step
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isTracking && event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++; // Increment step count
            stepCountTextView.setText("Steps: " + stepCount); // Update UI
        }
    }

    // Not used in this app, but required for the SensorEventListener interface
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // Request permission if needed when the app resumes
    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PERMISSION_REQUEST_CODE);
        }
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTracking(); // Start tracking if permission is granted
            } else {
                stepCountTextView.setText("Permission denied!"); // Show message if denied
            }
        }
    }

    // Load the saved step count from SharedPreferences
    private void loadStepCount() {
        SharedPreferences prefs = getSharedPreferences("PedometerPrefs", MODE_PRIVATE);
        stepCount = prefs.getInt("stepCount", 0); // Default to 0 if no data
        stepCountTextView.setText("Steps: " + stepCount); // Update UI
    }

    // Save the current step count to SharedPreferences
    private void saveStepCount() {
        SharedPreferences.Editor editor = getSharedPreferences("PedometerPrefs", MODE_PRIVATE).edit();
        editor.putInt("stepCount", stepCount);
        editor.apply(); // Commit the changes
    }

    // Save step count when the app is paused
    @Override
    protected void onPause() {
        super.onPause();
        saveStepCount();
    }
}
