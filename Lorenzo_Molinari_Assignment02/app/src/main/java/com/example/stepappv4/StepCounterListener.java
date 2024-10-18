package com.example.stepappv4;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

public class  StepCounterListener implements SensorEventListener {

    private long lastSensorUpdate = 0;
    public static int accStepCounter = 0;
    ArrayList<Integer> accSeries = new ArrayList<Integer>();
    private double accMag = 0;
    private int lastAddedIndex = 1;
    int stepThreshold = 6;
    private  Context context;

    static int stepCount = 0;

    TextView stepCountsView;
    ProgressBar pb;




    public StepCounterListener(Context context, TextView stepCountsView, ProgressBar pb, int stepToday)
    {
        this.stepCountsView = stepCountsView;
        this.context = context;

        this.pb = pb;
        this.stepCount = stepToday;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType())
        {
            case Sensor.TYPE_LINEAR_ACCELERATION:

                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                long currentTimeInMilliSecond = System.currentTimeMillis();
                long timeUntilSensorEvent =(SystemClock.elapsedRealtimeNanos()  - sensorEvent.timestamp )/1000000;

                long SensorEventTimestampInMilliSecond =  currentTimeInMilliSecond - timeUntilSensorEvent;

                SimpleDateFormat sensorEventTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sensorEventTimestamp.setTimeZone(TimeZone.getTimeZone("GMT+2"));

                String sensorEventDate = sensorEventTimestamp.format(SensorEventTimestampInMilliSecond);


                if ((currentTimeInMilliSecond - lastSensorUpdate) > 1000)
                {
                    lastSensorUpdate = currentTimeInMilliSecond;
                    String sensorRawValues = "  x = "+ String.valueOf(x) +"  y = "+ String.valueOf(y) +"  z = "+ String.valueOf(z);
                    Log.d("Acc. Event", "last sensor update at " + String.valueOf(sensorEventDate) + sensorRawValues);
                }


                accMag=Math.sqrt((x*x)+(y*y)+(z*z));

                accSeries.add((int)accMag);

                peakDetection();

                break;

            case Sensor.TYPE_STEP_DETECTOR:
                // TODO (Assignment 02): Use the STEP_DETECTOR  to count the number of steps
                // TODO (Assignment 02): The STEP_DETECTOR is triggered every time a step is detected
                // TODO (Assignment 02): The sensorEvent.values of STEP_DETECTOR has only one value for the detected step count
                stepCount = stepCount+1;
                countSteps(stepCount);
                Log.d("Step Det. Event", "Step counted: " + stepCount);


        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void peakDetection() {

        int windowSize = 20;
        /* Peak detection algorithm derived from: A Step Counter Service for Java-Enabled Devices Using a Built-In Accelerometer Mladenov et al.
         */
        int currentSize = accSeries.size(); // get the length of the series
        if (currentSize - lastAddedIndex < windowSize)
        {
            // if the segment is smaller than the processing window size skip it
            return;
        }

        List<Integer> valuesInWindow = accSeries.subList(lastAddedIndex,currentSize);
        lastAddedIndex = currentSize;

        for (int i = 1; i < valuesInWindow.size()-1; i++) {
            int forwardSlope = valuesInWindow.get(i + 1) - valuesInWindow.get(i);
            int downwardSlope = valuesInWindow.get(i) - valuesInWindow.get(i - 1);

            if (forwardSlope < 0 && downwardSlope > 0 && valuesInWindow.get(i) > stepThreshold) {
                accStepCounter += 1;
                Log.d("ACC STEPS: ", String.valueOf(accStepCounter));

                //stepCountsView.setText(String.valueOf(accStepCounter));

                //saveStepInDatabase();

                //pb.setProgress(accStepCounter);


            }
        }
    }

    //Implementation of countSteps method MWCA2
    private void countSteps(int step)
    {
        saveStepInDatabase();
        stepCountsView.setText(String.valueOf(step));
        pb.setProgress(step);
    }

    private void saveStepInDatabase()
    {
        //get current Timestamp
        long timeInMillis = System.currentTimeMillis();
        // Convert the timestamp to yyyy-MM-dd HH:mm:ss:SSS format
        SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        jdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        final String dateTimestamp = jdf.format(timeInMillis);
        String currentDay = dateTimestamp.substring(0,10);
        String hour = dateTimestamp.substring(11,13);



        ContentValues values = new ContentValues();
        values.put(StepAppOpenHelper.KEY_TIMESTAMP, dateTimestamp);
        values.put(StepAppOpenHelper.KEY_DAY, currentDay);
        values.put(StepAppOpenHelper.KEY_HOUR, hour);

        //Get the writable database
        StepAppOpenHelper databaseOpenHelper =   new StepAppOpenHelper(this.context);;
        SQLiteDatabase database = databaseOpenHelper.getWritableDatabase();
        long id = database.insert(StepAppOpenHelper.TABLE_NAME, null, values);
    }


}