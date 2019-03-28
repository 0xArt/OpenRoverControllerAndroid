package com.example.art.btrovercontroller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.example.art.btrovercontroller.BTService.MyLocalBinder;

public class MainActivity extends AppCompatActivity {

    String item = "empty";
    public final static int REQUEST_ENABLE_BT = 1;
    public BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String TAG = "MainActivity";
    BTService BTservice;
    boolean isBound = false;
    public ConnectBtTask connectBtTask =  new ConnectBtTask(MainActivity.this);
    public String theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        theme = sharedPreferences.getString("Theme", "Light");
        updateTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkBluetooth();
        Intent i = new Intent(this, BTService.class);
        bindService(i, myConnection, Context.BIND_AUTO_CREATE);
        setToolbar();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), 0);
            }
        });
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }catch (Exception e) {
            Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
            toast.show();
        }
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
                goToHelp();
                break;
        }
        return true;
    }

    public void setToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("ORC");
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

    public void goToSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(intent, 0);
    }

    public void goToHelp(){

    }

    @Override
    public void onBackPressed() {
        if(connectBtTask != null) {
            connectBtTask.cancel(true);
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private ServiceConnection myConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            MyLocalBinder binder = (MyLocalBinder) service;
            BTservice = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name){
            isBound = true;
        }
    };

    public void changeLayout(){
        Intent intent = new Intent(this, ControllerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(intent, 0);
    }

    public void testLayout(View view){
        Intent intent = new Intent(this, ControllerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(intent, 0);
    }

    public void checkBluetooth(){
        if(mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                listDevices();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode ==REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setMessage("Bluetooth must be ON to use this application. Please turn ON your device's bluetooth and open this application again.");
                builder1.setCancelable(true);
                builder1.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                finish();
                                System.exit(0);
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
            else{
                listDevices();
            }
        }
    }

    public void listDevices(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        List<String> btNames = new ArrayList<>();
        List<String> btMac = new ArrayList<>();
        for (BluetoothDevice bt : pairedDevices)
            btNames.add(bt.getName());
        for (BluetoothDevice bt : pairedDevices)
            btMac.add(bt.getAddress());
        ListAdapter deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, btNames);
        ListView deviceListView = findViewById(R.id.listView1);
        deviceListView.setAdapter(deviceAdapter);
        deviceListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position,
                                    long id) {
                // TODO Auto-generated method stub
                item = adapter.getItemAtPosition(position).toString();
                connectBtTask =  new ConnectBtTask(MainActivity.this);
                connectBtTask.execute();
            }
        });
    }

    private class ConnectBtTask extends AsyncTask<Void, Void, Boolean> {
        private  ProgressDialog pd = null;
        private Context pContext = null;
        private AlertDialog.Builder builder1 = null;
        ConnectBtTask(Context context){
            pContext = context;
        }

        protected Boolean doInBackground(Void... params) {
            if (connectBtTask.isCancelled()) {
                pd.dismiss();
                return false;
            }
            return BTservice.connectDevice(item);
        }

        protected void onPreExecute() {
            pd = new ProgressDialog(pContext);
            pd.setMessage("Establishing connection...");
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
            pd.show();
            builder1 =  new AlertDialog.Builder(pContext);
        }

        protected void onPostExecute(Boolean result) {
            if(result) {
                pd.dismiss();
                Log.d(TAG, "Socket connection success");
                changeLayout();
            }
            else {
                pd.dismiss();
                if (!connectBtTask.isCancelled()) {
                    builder1.setMessage("Connection failed, please try again. If connecting continues to fail, try restarting this app or power cycling the bluetooth devices");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(
                            "Dismiss",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                    Log.d(TAG, "Socket connection failed");
                    checkBluetooth();
                }
            }
        }
    }

}
