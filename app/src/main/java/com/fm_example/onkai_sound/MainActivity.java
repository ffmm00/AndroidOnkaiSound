package com.fm_example.onkai_sound;

import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mMagField;
    private Sensor mAccelerometer;
    private static final int AZIMUTH_THRESHOLD = 15;

    private static final int MATRIX_SIZE = 16;
    private float[] mValues = new float[3];
    private float[] acValues = new float[3];

    private int nowScale = 0;
    private int oldScale = 8;
    private int nowAzimuth = 0;
    private int oldAzimuth = 0;

    private MediaPlayer[] mplayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, 120000);
        mSensorManager.registerListener(this, mMagField, 120000);

        TypedArray notes = getResources().obtainTypedArray(R.array.notes);
        mplayer = new MediaPlayer[notes.length()];
        for (int i = 0; i < notes.length(); i++)
            mplayer[i] = MediaPlayer.create(this, notes.getResourceId(i, -1));

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagField);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] inR = new float[MATRIX_SIZE];
        float[] outR = new float[MATRIX_SIZE];
        float[] I = new float[MATRIX_SIZE];
        float[] orValues = new float[3];
        TextView text01 = (TextView) findViewById(R.id.text01);

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                acValues = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mValues = event.values.clone();
                break;
        }

        if (acValues != null && mValues != null) {
            SensorManager.getRotationMatrix(inR, I, acValues, mValues);

            SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
            SensorManager.getOrientation(outR, orValues);


            StringBuilder strBuild = new StringBuilder();
            strBuild.append("方位角(アジマス)：");
            strBuild.append(rad2Deg(orValues[0]));
            strBuild.append("\n");
            strBuild.append("傾斜角(ピッチ)：");
            strBuild.append(rad2Deg(orValues[1]));
            strBuild.append("\n");
            nowScale = rad2Deg(orValues[1]) / 10;
            strBuild.append("index:" + nowScale);
            strBuild.append("\n");
            strBuild.append("音階：");

            switch (nowScale) {
                case 0:
                    strBuild.append("高いレ");
                    break;
                case 1:
                    strBuild.append("高いド");
                    break;
                case 2:
                    strBuild.append("シ");
                    break;
                case 3:
                    strBuild.append("ラ");
                    break;
                case 4:
                    strBuild.append("ソ");
                    break;
                case 5:
                    strBuild.append("ファ");
                    break;
                case 6:
                    strBuild.append("ミ");
                    break;
                case 7:
                    strBuild.append("レ");
                    break;
                case 8:
                    strBuild.append("ド");
                    break;
                case 9:
                    break;
            }

            nowAzimuth = rad2Deg(orValues[0]);
            text01.setText(strBuild.toString());


            if (nowScale != oldScale) {
                playSound(nowScale);
                oldScale = nowScale;
                oldAzimuth = nowAzimuth;
            } else if (Math.abs(oldAzimuth - nowAzimuth) > AZIMUTH_THRESHOLD) {
                playSound(nowScale);
                oldAzimuth = nowAzimuth;
            }

        }
    }

    private int rad2Deg(float rad) {
        return (int) Math.floor(Math.abs(Math.toDegrees(rad)));
    }

    void playSound(int scale) {
        mplayer[scale].seekTo(0);
        mplayer[scale].start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
