package com.example.art.btrovercontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.LinkedList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    public String theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        theme = sharedPreferences.getString("Theme", "Light");
        updateTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setToolbar();
        listSettings();
    }

    @Override
    public void onBackPressed() {
        changeLayoutToMain();
    }

    public void changeLayoutToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(intent, 0);
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

    public void setToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("ORC Settings");
        setSupportActionBar(toolbar);
        if (theme.equals("Dark")) {
            toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Dark);
        }
    }

    public void listSettings(){
        final SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String maxVoltage = sharedPreferences.getString("Max volts", "9");
        String maxCurrent = sharedPreferences.getString("Max amps", "2");
        String inputStartByte = sharedPreferences.getString("Output start byte", "255");
        String outputStartByte = sharedPreferences.getString("Input start byte", "255");
        String theme = sharedPreferences.getString("Theme", "Light");
        final List<String[]> settingsList = new LinkedList<String[]>();
        settingsList.add(new String[] {"Max volts", maxVoltage});
        settingsList.add(new String[] {"Max amps", maxCurrent});
        settingsList.add(new String[] {"Output start byte", inputStartByte});
        settingsList.add(new String[] {"Input start byte", outputStartByte});
        settingsList.add(new String[] {"Theme", theme});

        ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]>(SettingsActivity.this, android.R.layout.simple_list_item_2, android.R.id.text1,  settingsList){
            @Override
            @NonNull
            public View getView(int position, View convertView, ViewGroup parent){
                View view = super.getView(position, convertView, parent);
                    String[] entry = settingsList.get(position);
                    TextView text1 = view.findViewById(android.R.id.text1);
                    TextView text2 = view.findViewById(android.R.id.text2);
                    text1.setText(entry[0]);
                    text2.setText(entry[1]);
                    return view;
                }
            };
        ListView listView = findViewById(R.id.listView1);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position,
                                    long id) {
                // TODO Auto-generated method stub
                    String[] temp =  (String[]) adapter.getItemAtPosition(position);
                    settingsPopupPortal(temp[0]);
                    listSettings();
            }
        });
    }

    public void settingsPopupPortal(String key){
        switch(key){
            case "Max volts":
                settingsNumericPopup(key, "Enter any positive integer" );
                break;
            case "Max amps":
                settingsNumericPopup(key, "Enter any positive integer");
                break;
            case "Theme":
                settingsThemePopup();
                break;
            case "Output start byte":
                settingsNumericPopup(key, "Enter new value (0-255)");
                break;
            case "Input start byte":
                settingsNumericPopup(key, "Enter new value (0-255)");
                break;
            default:
                break;
        }
    }


    public void updateNumericValue(String key, String value){
        if(checkNumericValue(key, value)) {
            final SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.apply();
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Value is not acceptable!",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
        listSettings();
    }

    public boolean checkNumericValue(String key, String value){
        if (value.matches("[0-9]+") && value.length() > 0) {
            if(key.equals("Output start byte") || key.equals("Input start byte")){
                if(Integer.parseInt(value) < 256 && Integer.parseInt(value) >=0){
                    return true;
                }
            }
            else{
                return true;
            }
        }
        return false;
    }


    public void updateSharedPref (String key, String value){
        final SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
        listSettings();
    }

    public void settingsNumericPopup(final String key, String message){
        int style = R.style.ThemeOverlay_AppCompat_Light;
        if(theme.equals("Dark")){
            style = R.style.ThemeOverlay_AppCompat_Dark;
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, style));
        final EditText edittext = new EditText(new ContextThemeWrapper(this, style));
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
        alertDialogBuilder .setMessage(message);
        alertDialogBuilder .setTitle(key);
        alertDialogBuilder .setView(edittext);
        alertDialogBuilder .setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = edittext.getText().toString();
                updateNumericValue(key, value);
                dialog.dismiss();
            }
        });
        alertDialogBuilder .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.show();
    }

    public void settingsThemePopup(){
        int style = R.style.ThemeOverlay_AppCompat_Light;
        if(theme.equals("Dark")){
            style = R.style.ThemeOverlay_AppCompat_Dark;
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, style));
        RadioGroup themes = new RadioGroup(new ContextThemeWrapper(this, style));
        final RadioButton lightTheme = new RadioButton(new ContextThemeWrapper(this, style));
        lightTheme.setText("Light");
        themes.addView(lightTheme);
        final RadioButton darkTheme = new RadioButton(new ContextThemeWrapper(this, style));
        darkTheme.setText("Dark");
        themes.addView(darkTheme);
        int paddingDp = 10;
        float density = this.getResources().getDisplayMetrics().density;
        int paddingPixel = (int)(paddingDp * density);
        themes.setPadding(paddingPixel,paddingPixel,0,0);
        alertDialogBuilder .setTitle("Choose theme");
        alertDialogBuilder .setView(themes);
        alertDialogBuilder .setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                if(darkTheme.isChecked()){
                    updateSharedPref("Theme", "Dark");
                }
                if(lightTheme.isChecked()){
                    updateSharedPref("Theme", "Light");
                }
                dialog.dismiss();
                restartActivity();
            }
        });
        alertDialogBuilder .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.show();
    }

    public void restartActivity(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
