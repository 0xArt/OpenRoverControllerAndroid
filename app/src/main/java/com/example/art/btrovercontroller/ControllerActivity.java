package com.example.art.btrovercontroller;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.art.btrovercontroller.BTService.MyLocalBinder;

public class ControllerActivity extends AppCompatActivity {

    public SeekBar speedBar = null;
    public byte speed = 0;
    private static final String TAG = "ControllerActivity";
    BTService BTservice;
    boolean isBound = false;
    public boolean newData = false;
    public byte[] receivedBytes = new byte[3];
    public Handler mHandler;
    public String theme;
    public byte outputStartbyte;
    public byte inputStartbyte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadPrefsToVars();
        updateTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        setToolbar();
        speedBar();
        Intent i = new Intent(this, BTService.class);
        bindService(i, myConnection, Context.BIND_AUTO_CREATE);
        setListenerAndRunnable(findViewById(R.id.upButton),(byte)1, R.drawable.up, R.drawable.up_pressed);
        setListenerAndRunnable(findViewById(R.id.rightButton),(byte)2, R.drawable.right, R.drawable.right_pressed);
        setListenerAndRunnable(findViewById(R.id.downButton),(byte)3, R.drawable.down, R.drawable.down_pressed);
        setListenerAndRunnable(findViewById(R.id.leftButton),(byte)4, R.drawable.left, R.drawable.left_pressed);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id) {
            case R.id.settings:
                goToSettings();
                break;
            case R.id.help:
                break;
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onBackPressed() {
        if(BTservice.isConnected()){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(ControllerActivity.this);
            builder1.setMessage("Going back will disconnect you from the device. Continue?");
            builder1.setCancelable(true);
            builder1.setPositiveButton(
                    "YES",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            BTservice.disconnect();
                            dialog.cancel();
                            changeLayoutToMain();
                        }
                    });
            builder1.setNegativeButton(
                    "NO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }

        else{
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityIfNeeded(intent, 0);
        }
    }

    public void loadPrefsToVars(){
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        theme = sharedPreferences.getString("Theme", "Light");
        outputStartbyte = (byte)Integer.parseInt(sharedPreferences.getString("Output start byte", "255"));
        inputStartbyte = (byte)Integer.parseInt(sharedPreferences.getString("Input start byte", "255"));
    }

    public void setToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("ORC Command Center");
        setSupportActionBar(toolbar);
        if (theme.equals("Dark")) {
            toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Dark);
        }
    }

    public void updateTheme() {
        int version = android.os.Build.VERSION.SDK_INT;
        switch (theme) {
            case "Dark":
                if (version > 21) {
                    setTheme(android.R.style.ThemeOverlay_Material_Dark);
                }else{
                    setTheme(android.R.style.Theme_Holo_NoActionBar);
                }
                break;
            case "Light":
                if(version > 21) {
                    setTheme(android.R.style.ThemeOverlay_Material_Light);
                }
                else{
                    setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
                }
                break;
            default:
                break;
        }
    }

    private ServiceConnection myConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            MyLocalBinder binder = (MyLocalBinder) service;
            BTservice = binder.getService();
            isBound = true;
            Thread inputThread = new Thread(inputStream);
            inputThread.start();
            setDeviceName();
        }

        @Override
        public void onServiceDisconnected(ComponentName name){
            isBound = false;
        }
    };

    public void speedBar( ){
        speedBar = findViewById(R.id.speedBar);
        final TextView text_view = findViewById(R.id.speedText);
        text_view.setText("Speed : " + speedBar.getProgress() + " / " +speedBar.getMax());
        speedBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress_value;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progress_value = progress;
                        text_view.setText("Speed: " + progress + " / " +speedBar.getMax());
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        text_view.setText("Speed: " + progress_value + " / " +speedBar.getMax());
                    }
                }
        );

    }

    public void setListenerAndRunnable(final View view, final byte command, final int nPressed, final int pressed){
        view.setOnTouchListener(new View.OnTouchListener() {

            @Override public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ((ImageButton) view).setImageResource(pressed);
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 100);
                        break;
                    case MotionEvent.ACTION_UP:
                        ((ImageButton) view).setImageResource(nPressed);
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        movePause();
                        break;
                }
                return false;
            }
            Runnable mAction = createRunnable(command);
        });
    }

    private Runnable createRunnable(final byte command) {
        Runnable runnable = new Runnable() {
            public void run() {
                if(mHandler != null) {
                    byte[] toSend = {outputStartbyte, command, speed, 0};
                    toSend[2] = (byte) speedBar.getProgress();
                    toSend[3] = (byte) (command ^ toSend[2]);
                    BTservice.sendByte(toSend);
                    mHandler.postDelayed(this, 100);
                }
            }
        };
        return runnable;
    }

    public void setDeviceName(){
        String display = "Device: " + BTservice.getDeviceName();
        TextView deviceText = findViewById(R.id.deviceText);
        deviceText.setText(display);
    }

    public void goToSettings(){
        if(BTservice.isConnected()){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(ControllerActivity.this);
            builder1.setMessage("Opening settings will disconnect you from the device. Continue?");
            builder1.setCancelable(true);
            builder1.setPositiveButton(
                    "YES",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            BTservice.disconnect();
                            dialog.cancel();
                            changeLayoutToSettings();
                        }
                    });
            builder1.setNegativeButton(
                    "NO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
        else{
            changeLayoutToSettings();
        }
    }

    public void movePause() {
        byte command = 5;
        byte[] toSend = {-1, command, speed, (byte)(command^speed)};
        BTservice.sendByte(toSend);
    }


    public void disconnect(View view) {
            if(BTservice.isConnected()) {
                BTservice.disconnect();
            }
            setDeviceName();
            changeLayoutToMain();
    }

    public void changeLayoutToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void changeLayoutToSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    Runnable inputStream = new Runnable() {
        public void run() {
            boolean recvInProgress = false;
            byte ndx = 0;
            byte numBytes = 3;
            while(BTservice.isConnected()) {
                if(!newData) {
                    if(BTservice.inputAvailable()) {
                        byte readByte = (BTservice.getInput())[0];
                        if (recvInProgress) {
                            if (ndx < numBytes) {
                                receivedBytes[ndx] = readByte;
                                ndx++;
                            }
                            if (ndx == numBytes) {
                                recvInProgress = false;
                                ndx = 0;
                                newData = true;
                            }
                        }
                        if (readByte == inputStartbyte) {
                            recvInProgress = true;
                            //Log.d(TAG, "found start marker");
                        }
                    }
                }
                if(newData){
                    processData(receivedBytes);
                }
            }
        }
    };

    public void processData(byte[] receivedBytes){
        byte xoredByte = (byte) (receivedBytes[0] ^ receivedBytes[1]);
        if(xoredByte == receivedBytes[2]) {
            //Log.d(TAG, "correct checksum");
            updateVoltage(receivedBytes[0]);
            updateCurrent(receivedBytes[1]);
        }
        else{
            Log.e(TAG, "incorrect checksum");
        }
        newData = false;
    }

    public void updateVoltage(final byte voltage){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                String maxVoltage = sharedPreferences.getString("Max volts", "9");
                int max = Integer.parseInt(maxVoltage);
                int convertedVoltage = byteToInt(voltage);
                float voltage = (float)max * ((float)convertedVoltage/255);
                TextView voltageText = findViewById(R.id.voltageText);
                String display = "Volts: " + String.format("%.02f", voltage);
                voltageText.setText(display);
            }
        });
    }

    public void updateCurrent(final byte current){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                String maxCurrent = sharedPreferences.getString("Max amps", "2");
                int max = Integer.parseInt(maxCurrent);
                int convertedVoltage = byteToInt(current);
                float current = (float)max * ((float)convertedVoltage/255);
                TextView currentText = findViewById(R.id.currentText);
                String display = "Amps: " + String.format("%.02f", current);
                currentText.setText(display);
            }
        });
    }

    public Integer byteToInt(byte givenByte){
        if((int)givenByte < 0){
            return (int)givenByte + 256;
        }
        return (int)givenByte;
    }

}


