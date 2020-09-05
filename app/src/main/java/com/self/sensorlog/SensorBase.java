package com.self.sensorlog;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SensorBase implements SensorEventListener {
  private static final String TAG = SensorBase.class.getCanonicalName();
  private Context mContext;
  private Sensor selSensor;
  private Sensor grav;
  private Sensor gyro;
  private Sensor acc;
  private String selSensorId;
  private SensorManager mSensorManager;
  private int kMinSamplingPeriodUS = 2 * 1000; //micro seconds
  private final String kfolderPath = Environment.getExternalStorageDirectory() + "/sensorLogs/";
  private FileWriter mWriter;

  public SensorBase(Context ctx){
    mContext = ctx;
    mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
  }

  public boolean initSensor(String sensorId){
    if(sensorId.equals(MainActivity.SENSORS_LIST.ACCELEROMETER.toString()))
      selSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    else if(sensorId.equals(MainActivity.SENSORS_LIST.GYROSCOPE.toString()))
      selSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    else if(sensorId.equals(MainActivity.SENSORS_LIST.GRAVITY_SENSOR.toString()))
      selSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

    else if(sensorId.equals(MainActivity.SENSORS_LIST.ALL.toString())){
      acc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
      gyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
      grav = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    boolean ret;

    if(sensorId.equals(MainActivity.SENSORS_LIST.ALL.toString()))
      ret = !(acc == null || gyro == null || grav == null);
    else
      ret = !(selSensor == null);

    Log.v(TAG, "selSenosr " + selSensor + " ret " + ret);
    if(ret) selSensorId = sensorId;
    return ret;
  }

  private void registerListener(){
    if(selSensorId.equals(MainActivity.SENSORS_LIST.ALL.toString())){
      boolean accRc = mSensorManager.registerListener(this, acc, acc.getMinDelay());
      boolean gravRc = mSensorManager.registerListener(this, grav, grav.getMinDelay());
      boolean gyroRc = mSensorManager.registerListener(this, gyro, gyro.getMinDelay());

      if(!accRc || !gravRc || !gyroRc)
        sensorNotSupportedErrFunc();

      return;
    }

    int minDelay = selSensor.getMinDelay();
    boolean sensorRc = mSensorManager.registerListener(this, selSensor, minDelay > 0 ? minDelay : kMinSamplingPeriodUS);

    if(!sensorRc) sensorNotSupportedErrFunc();
  }

  void sensorNotSupportedErrFunc(){
    stopLogger();
    Toast.makeText(mContext, "sensor not supported for specified update interval", Toast.LENGTH_LONG).show();
    throw new RuntimeException("sensor not supported for specified update interval");
  }

  private void prepareDirectory(){
    File folder = new File(kfolderPath);
    Log.d(TAG, "prepareDirectory: " + folder);
    if(!folder.exists() && folder.mkdirs()){
      Log.d(TAG, "folder created " + kfolderPath);
    }
  }

  void startLogger() throws IOException {
    prepareDirectory();
    if(mWriter != null){
      mWriter.close();
      mWriter = null;
    }

    mWriter = new FileWriter(new File(kfolderPath, selSensorId + ".csv"), true);
    registerListener();
  }

  void stopLogger(){
    mSensorManager.flush(this);
    if(selSensorId.equals(MainActivity.SENSORS_LIST.ALL.toString())){
      mSensorManager.unregisterListener(this, acc);
      mSensorManager.unregisterListener(this, grav);
      mSensorManager.unregisterListener(this, gyro);
    }
    else
      mSensorManager.unregisterListener(this, selSensor);

    selSensor = null;
    acc = null;
    grav = null;
    gyro = null;
    selSensorId = "";
  }

  void insertActivityBreak(){
    if(mWriter != null) {
      try {
        mWriter.write("Activity Break ==============================================>>>>\n");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  void insertFallBreak(){
    if(mWriter != null) {
      try {
        mWriter.write("Fall Break ==============================================>>>>\n");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onSensorChanged(SensorEvent evt) {
    if(mWriter != null){
      try {
        if(selSensorId.equals(MainActivity.SENSORS_LIST.ALL.toString())){
          mWriter.write(evt.sensor.getStringType()+ " ::: " + String.format("%d; %f; %f; %f;\n", evt.timestamp, evt.values[0], evt.values[1], evt.values[2]));
        }
        else {
          mWriter.write(String.format("%d; %f; %f; %f;\n", evt.timestamp, evt.values[0], evt.values[1], evt.values[2]));
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int i) {
    Log.i(TAG, "Sensor accuracy changed " + i);
  }
}
