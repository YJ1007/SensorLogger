package com.self.sensorlog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import java.util.ArrayList;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
  private static final String TAG = MainActivity.class.getCanonicalName();
  private Spinner sensorSpinner;
  private Button but;
  private Button setActivity;
  private Button fallTestStartBut;
  private String selectedSensor = "";
  private SensorBase sb;
  private boolean isRunning = false;
  private final String[] permissions = {
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
  };

  public enum SENSORS_LIST {
    ACCELEROMETER,
    GYROSCOPE,
    GRAVITY_SENSOR,
    ALL
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    sb = new SensorBase(this);
    initSpinner();
    but = findViewById(R.id.startBut);
    but.setText("START");

    setActivity = findViewById(R.id.setActivity);
    setActivity.setText("insert activity break");

    fallTestStartBut = findViewById(R.id.fallTestStart);
    fallTestStartBut.setText("insert fall testing break");

    if(but != null) but.setOnClickListener(this);
    if(setActivity != null) setActivity.setOnClickListener(this);
    if(fallTestStartBut != null) fallTestStartBut.setOnClickListener(this);
  }

  private void initSpinner(){
    sensorSpinner = findViewById(R.id.sensorPicker);
    sensorSpinner.setOnItemSelectedListener(this);

    List<SENSORS_LIST> sensor_list = new ArrayList<>();
    sensor_list.add(SENSORS_LIST.ACCELEROMETER);
    sensor_list.add(SENSORS_LIST.GYROSCOPE);
    sensor_list.add(SENSORS_LIST.GRAVITY_SENSOR);
    sensor_list.add(SENSORS_LIST.ALL);

    ArrayAdapter<SENSORS_LIST> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sensor_list);

    sensorSpinner.setAdapter(dataAdapter);
  }

  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.startBut:
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
          runLogger();
        } else {
          requestPermissions(permissions, 200);
        }
        break;

      case R.id.setActivity:
        if(isRunning)
          sb.insertActivityBreak();
        else
          Toast.makeText(this, "Start logging to insert break", Toast.LENGTH_SHORT).show();
        break;

      case R.id.fallTestStart:
        if(isRunning)
          sb.insertFallBreak();
        else
          Toast.makeText(this, "Start logging to insert break", Toast.LENGTH_SHORT).show();
        break;
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
      runLogger();
    }
    else{
      Toast.makeText(this, "Permission denied ", Toast.LENGTH_LONG).show();
    }
  }

  private void runLogger(){
    if(!isRunning && !sb.initSensor(selectedSensor)){
      Toast.makeText(this, "Selected sensor not available ", Toast.LENGTH_LONG).show();
      return;
    }

    if(isRunning){
      sb.stopLogger();
      but.setText("START");
    }
    else{
      try {
        sb.startLogger();
        but.setText("STOP");
      } catch (IOException e) {
        Log.e(TAG, "exception onClick ", e);
      }
    }
    isRunning = !isRunning;
  }

  @Override
  public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    selectedSensor = adapterView.getItemAtPosition(i).toString();
  }

  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {

  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    sb.stopLogger();
  }
}
