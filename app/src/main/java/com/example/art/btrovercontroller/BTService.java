package com.example.art.btrovercontroller;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import static android.content.ContentValues.TAG;


public class BTService extends Service {

    private final IBinder myBinder = new MyLocalBinder();
    public static InputStream btIn;
    public static OutputStream btOut;
    public  static BluetoothSocket btSock;
    public final static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public BTService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }


    public class MyLocalBinder extends Binder{
        BTService getService(){
            return BTService.this;
        }
    }

    public boolean connectDevice(String device) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bt : pairedDevices) {
            if ((bt.getName()).equals(device)) {
                try {
                    // Get a BluetoothSocket to connect with the given BluetoothDevice.
                    // MY_UUID is the app's UUID string, also used in the server code.
                    BluetoothSocket tmp = bt.createRfcommSocketToServiceRecord(MY_UUID);
                    tmp.connect();
                    InputStream tmpIn = tmp.getInputStream();
                    OutputStream tmpOut = tmp.getOutputStream();
                    Log.e(TAG, "Socket's create() method success");
                    btSock = tmp;
                    btIn = tmpIn;
                    btOut = tmpOut;
                    return true;
                } catch (IOException e) {
                    Log.e(TAG, "Socket's create() method failed", e);
                    btIn = null;
                    btOut = null;
                    btSock = null;
                    return false;
                }
            }
        }
        return false;
    }

    public boolean disconnect() {
        try {
            if(btSock != null) {
                btSock.close();
                //inputStreamFuture.cancel(true);
                Log.e(TAG, "sock closed");
                return true;
            }
            else{
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isConnected() {
        if(btSock == null) {
            return false;
        }
        else {
            return btSock.isConnected();
        }
        }

    public static boolean sendByte(byte[] toSend){
        try {
            if(btOut != null) {
                btOut.write(toSend);
                btOut.flush();
                return true;
            }
            else{
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    return false;
    }

    public boolean inputAvailable(){
        try{
            if(btSock != null) {
                int bytesAvailable = btIn.available();
                if(bytesAvailable > 0) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public byte[] getInput(){
        byte[] bytes = new byte[1];
        if(btIn != null) {
            try {
                btIn.read(bytes);
                String s = new String(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    public String getDeviceName(){
        if(isConnected()) {
            BluetoothDevice device = btSock.getRemoteDevice();
            return device.getName();
        }
        return "N/A";
    }



}
