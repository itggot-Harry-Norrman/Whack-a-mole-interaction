package com.example.wack_a_mole;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

import java.util.Random;

public class gameActivity extends AppCompatActivity implements SensorEventListener{

    private float[] acceleration = new float[3];
    private float[] magneticField = new float[3];
    private float[] orientation = new float[3];
    private float startingDeg;
    private boolean start = false;

    private boolean foundMole = true;
    private float moleDeg;
    private int currentDeg;

    private TextView deg, moleTxt;

    private Sensor accelerometerSensor;

    private Sensor magnetometerSensor;

    private Sensor rotationVectorSensor;

    private SensorManager sensorManager;
    private boolean search = true;

    private Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        deg = findViewById(R.id.deg);
        moleTxt = findViewById(R.id.moleDeg);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }
    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    System.arraycopy(event.values, 0, acceleration, 0, 3);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    System.arraycopy(event.values, 0, magneticField, 0, 3);
                    break;
            }

        //boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, acceleration, magneticField);

        //if (success) {
        float[] rotationVector = event.values;

        // Convert the rotation vector to a rotation matrix
        float[] rotationMatrix = new float[9];
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            if(search) {
                search(rotationVector, rotationMatrix);
            } else {
                whack();
            }

        }
        }

        private float GetRandomDeg() {
            Random rand = new Random();

            Log.d("currentDeg:", String.valueOf(startingDeg));

            int randomDeg = rand.nextInt(181) - 90 + (int)startingDeg;
            //int randomDeg = 225;
            if(randomDeg > 180) {
                Log.d("beforeDeg: ", String.valueOf(randomDeg));
                randomDeg = -360 + randomDeg;
                Log.d("random > 180: ", String.valueOf(randomDeg));
            } else if(randomDeg < -180) {
                Log.d("beforeDeg: ", String.valueOf(randomDeg));
                randomDeg = 360 -randomDeg;
                Log.d("end < -180: ", String.valueOf(randomDeg));
            }

            return randomDeg;
        }
        private boolean checkDeg() {
            if(moleDeg > 0 && currentDeg+10 > moleDeg && currentDeg - 10 < moleDeg) {
                Log.d("found mole +", "jadå");
                return true;
            }
            if(moleDeg < 0 && currentDeg +10 > moleDeg && currentDeg - 10 < moleDeg){
                Log.d("found mole -", "jadå");
                return true;
            }
            return false;
        }
        private void search(float[] rotationVector, float[] rotationMatrix) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

            // Get the device's orientation from the rotation matrix
            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);

            // The orientation array contains the device's orientation in radians
            // orientation[0] = azimuth (yaw), orientation[1] = pitch, orientation[2] = roll

            SensorManager.getOrientation(rotationMatrix, orientation);
            float azimuth = (float) Math.toDegrees(orientation[0]);
            currentDeg = (int)azimuth;
            float pitch = (float) Math.toDegrees(orientation[1]);
            float roll = (float) Math.toDegrees(orientation[2]);
            if(!start) {
                startingDeg = azimuth;
                start = true;
            }
            if(checkDeg()) {
                foundMole = true;
            }
            if(foundMole) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                }
                moleDeg = GetRandomDeg();
                moleTxt.setText(String.valueOf(moleDeg));
                search = false;
                foundMole = false;
            }
            deg.setText(String.valueOf((int)azimuth));
            // Do something with the angle values here
            //}
        }
        private void whack() {
            search = true;
        }
    };