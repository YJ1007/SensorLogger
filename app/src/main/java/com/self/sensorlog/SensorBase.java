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

    boolean ret = !(selSensor == null);
    Log.v(TAG, "selSenosr " + selSensor + " ret " + ret);
    if(ret) selSensorId = sensorId;
    return ret;
  }

  private void registerListener(){
    int minDelay = selSensor.getMinDelay();
    boolean sensorRc = mSensorManager.registerListener(this, selSensor, minDelay > 0 ? minDelay : kMinSamplingPeriodUS);
    if(!sensorRc){
      stopLogger();
      Toast.makeText(mContext, "sensor not supported for specified update interval", Toast.LENGTH_LONG).show();
      throw new RuntimeException("sensor not supported for specified update interval");
    }
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
    mSensorManager.unregisterListener(this, selSensor);
    selSensor = null;
    selSensorId = "";
  }

  @Override
  public void onSensorChanged(SensorEvent evt) {
    if(mWriter != null){
      try {
        mWriter.write(String.format("%d; %f; %f; %f;\n", evt.timestamp, evt.values[0], evt.values[1], evt.values[2]));
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
