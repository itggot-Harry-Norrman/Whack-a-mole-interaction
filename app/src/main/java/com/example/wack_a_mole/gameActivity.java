package com.example.wack_a_mole;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Timestamp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;

public class gameActivity extends AppCompatActivity implements SensorEventListener{

    private float[] acceleration = new float[3];
    private float[] magneticField = new float[3];
    private float[] orientation = new float[3];
    private float startingDeg;
    private boolean start = false;
    private Timestamp t;
    private long foundMoleTimestamp;
    private long starttime;
    private long endtime;
    private long gameLength;
    private boolean foundMole = true;
    private float moleDeg;
    private int currentDeg;
    private int lastMoleDeg;
    private TextView deg, moleTxt, highScore;
    private int scoreCounter = 0;
    private Sensor accelerometerSensor, magnetometerSensor, rotationVectorSensor;

    private SensorManager sensorManager;
    private boolean search = true;

    private Vibrator v;
    private MediaPlayer siren,bonk,coin,popOut, warning2, pop2, hit2, leave, pop3, win;
    private ImageView moleView;
    private float accX, accY, accZ;

    private boolean outOfBounds = false;
    private int upperBound;
    private int lowerBound;
    private boolean tutorialMode = true;

    private TimerThread timerThread;
    private TimerThread oobThread;
    private TimerThread moleThread;
    private boolean oob,oobTimerStarted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //hides top-menu

        //starttime = System.currentTimeMillis();
        gameLength = 30000;

        moleView = findViewById(R.id.molev);
        popOut = MediaPlayer.create(this, R.raw.popout);
        bonk = MediaPlayer.create(this, R.raw.bonk);
        coin = MediaPlayer.create(this, R.raw.coin);
        siren = MediaPlayer.create(this, R.raw.siren);
        hit2 = MediaPlayer.create(this, R.raw.hit2);
        pop2 = MediaPlayer.create(this, R.raw.pop2);
        warning2 = MediaPlayer.create(this, R.raw.warning2);
        leave = MediaPlayer.create(this, R.raw.leave);
        pop3 = MediaPlayer.create(this, R.raw.pop3);
        win = MediaPlayer.create(this, R.raw.win);
        setContentView(R.layout.activity_game);
        deg = findViewById(R.id.deg);
        moleTxt = findViewById(R.id.moleDeg);
        highScore = findViewById(R.id.highScore);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        oob = false;
        oobTimerStarted = false;

        setTutorialVisibility(View.VISIBLE);

        // vet low key inte om detta funkar pga kunde ej testa med emulatorn
        ImageView gifImageView2 = findViewById(R.id.molev);
        Glide.with(this)
                .load(R.drawable.molebig)
                .into(gifImageView2);
        gifImageView2.setVisibility(View.INVISIBLE);
        
    }

    private void setTutorialVisibility(int mode) {
        ImageView tutorial = (ImageView)findViewById(R.id.start_popup);
        tutorial.setVisibility((mode));
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
        long currentTime = System.currentTimeMillis() - starttime;
        if (currentTime >= gameLength && !tutorialMode) {
            System.out.println("game over");
            gameOver();
            // do something
           // Intent intent = new Intent(gameActivity.this, MainActivity.class);
            //startActivity(intent);
        }
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    System.arraycopy(event.values, 0, acceleration, 0, 3);
                    accX = event.values[0];
                    accY = event.values[1];
                    accZ = event.values[2];
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
                }
            }

            if(isOutOfBounds() && !oob){
                oob=true;
                oobThread = new TimerThread(false, 1000);
                oobThread.start();
                oobTimerStarted = true;

            } else if(isOutOfBounds() && oobTimerStarted){
                if(oobTimerStarted && oobThread.getTimerValue()<=0){
                    //siren.start();
                    warning2.start();
                }
            } else if(!isOutOfBounds()){
                //siren.pause();
                if(warning2.isPlaying()){
                    warning2.pause();
                }
                oob=false;
                oobTimerStarted = false;
            }

            if(!search) {
                whack(accX, accY, accZ); // accelerometer-sensor
            }
            highScore.setText(String.valueOf(scoreCounter));
        }

    private void gameOver() {
        // Suppose 'activity' is an instance of your current activity
        onPause();
        // highscore smth?
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                win.start();
                new AlertDialog.Builder(gameActivity.this)
                        .setTitle("Tiden är ute!")
                        .setMessage("Din poäng: " + scoreCounter + "\n" + "Vill du starta om spelet?")
                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                restart();
                                onResume();
                            }
                        })
                        .setNegativeButton("Nej", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(gameActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        });
    }

    private void restart() {
        ImageView gifImageView2 = findViewById(R.id.molev);
        gifImageView2.setVisibility(View.INVISIBLE);
        starttime = System.currentTimeMillis();
        scoreCounter = 0;
    }

    private float GetRandomDeg() {
            Random rand = new Random();

            Log.d("currentDeg:", String.valueOf(startingDeg));
            int randomDeg = rand.nextInt(121) - 60 + (int)startingDeg;

            //dont spawn mole on top of player current deg
            if(randomDeg > lastMoleDeg - 30 && randomDeg < lastMoleDeg + 30) {
                randomDeg = (int)GetRandomDeg(); // regenerate a new degree
            }
            if(randomDeg > 180) {
                //Log.d("beforeDeg: ", String.valueOf(randomDeg));
                randomDeg = -360 + randomDeg;
               // Log.d("random > 180: ", String.valueOf(randomDeg));
            } else if(randomDeg < -180) {
               // Log.d("beforeDeg: ", String.valueOf(randomDeg));
                randomDeg = 360 +randomDeg;
                //Log.d("end < -180: ", String.valueOf(randomDeg));
            }
            lastMoleDeg = randomDeg;
            return randomDeg;
        }
        private boolean checkDeg() {
            if(currentDeg < moleDeg+10 && currentDeg > moleDeg - 10) {
                //Log.d("found mole +", "jadå");
                return true;
            }
            if((moleDeg > 170 || moleDeg < -170) && (currentDeg > 170 || currentDeg < -170)) {
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
            lastMoleDeg = (int) startingDeg;
            starttime = System.currentTimeMillis();
            lowerBound = (int) startingDeg-60;
            upperBound = (int) startingDeg+60;
            if(upperBound > 180) {
                upperBound = -360 + upperBound;
            }
            if(lowerBound < -180) {
                lowerBound = 360 + lowerBound;
            }
            Log.d("startingdeg",String.valueOf(startingDeg));
            Log.d("uooerBound", String.valueOf(upperBound));
            Log.d("lowerBound", String.valueOf(lowerBound));
            start = true;
            //Glide.with(this)
            //        .load(R.drawable.mole01)
            //        .into(moleView);
        }
        if(checkDeg() && timerThread.getTimerValue()<=0) {
            //popOut.start();
            //pop2.start();
            pop3.start();
            //Glide.with(this)
            //        .load(R.drawable.mole01)
            //        .into(moleView);
            foundMole = true;
        }
        if(foundMole) {
            if (!tutorialMode) {
                doVibration(150);
            }
            ImageView gifImageView2 = findViewById(R.id.molev);
            gifImageView2.setVisibility(View.VISIBLE);
            moleDeg = GetRandomDeg();
            moleTxt.setText(String.valueOf(moleDeg));
            search = false;
            foundMole = false;
            foundMoleTimestamp = System.currentTimeMillis();
        }
        deg.setText(String.valueOf((int)azimuth));
        // Do something with the angle values here
        //}
    }

    private void doVibration(int time) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    /** x, y, z värden från
         *  float x = sensorEvent.values[0];
         *  float y = sensorEvent.values[1];
         *  float z = sensorEvent.values[2];
         *             */
        private void whack(float x, float y, float z) {
            ImageView gifImageView2 = findViewById(R.id.molev);
            if(System.currentTimeMillis()>=foundMoleTimestamp+2000 && !tutorialMode){
                gifImageView2.setVisibility(View.INVISIBLE);
                search = true;
                leave.start();
                return;
            }
            if(calcForces(x, y, z, 4F)) {
                //bonk.start();
                hit2.start();
                if(pop3.isPlaying()){
                    pop3.pause();
                    pop3.seekTo(0);
                }


                gifImageView2.setVisibility(View.INVISIBLE);

                // check if the tutorial popup is visible
                // then set it to invisible and return, update starttime
                if (tutorialMode) {
                    setTutorialVisibility(View.INVISIBLE);
                    starttime = System.currentTimeMillis();
                    doVibration(300);
                    search = true;
                    tutorialMode = false;
                    return;
                }

                float whackTime = System.currentTimeMillis() - foundMoleTimestamp;
                if (whackTime > 1000) {
                    scoreCounter = scoreCounter + 50;
                } else {
                    int point = (int) (1000 - whackTime);
                    scoreCounter = scoreCounter + point;
                }

                search = true;
                doVibration(300);
            }
            timerThread = new TimerThread(true,0);
            timerThread.start();
        }
        private boolean checkPunch(int x, int y, int z) {
            // z > -7 && z < 7 && y > -3 && y < 5
            return x > 15 && z > -7 && z < 7;
        }

    private boolean calcForces(float x, float y, float z, float threshold) {
        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        // gForce will be close to 1 when there is no movement.
        float gForce = (float)Math.sqrt(gX * gX + gY * gY + gZ * gZ);
       //System.out.println(gForce);
        //System.out.println("x: "+x + "y: " + y + "z: " + z);
        return gForce > threshold;
    }
    private boolean isOutOfBounds(){

        if(currentDeg<lowerBound || currentDeg>upperBound){
            //Log.d("outOfBoundsCalled","out");
            return true;
        }
        //Log.d("outOfBoundsCalled","in");
        return false;
    }
    private static class TimerThread extends Thread {

        Random rand = new Random();
        private int timerValue;

        public TimerThread(boolean isRandom, int time) {
            if (isRandom){
                this.timerValue = 1000+rand.nextInt(1000);
            } else {
                this.timerValue = time;
            }
        }

        public int getTimerValue() {
            return timerValue;
        }

        public void run() {
            while (timerValue > 0) {
                try {
                    Thread.sleep(100); //checks every 100 milliseconds (deca second)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timerValue-=100;
            }
        }
    }
    }

