package com.plk.smartq.smartalert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "SmartAlert";

    EditText edt_hoscode, edt_number;

    public void readSetting() {
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);

        String hoscode = pref.getString("hoscode", "");
        String number = pref.getString("number", "");
        edt_hoscode.setText(hoscode);
        edt_number.setText(number);


    }

    public void writeSetting() {
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        String hoscode = edt_hoscode.getText().toString().trim();
        String number = edt_number.getText().toString().trim();
        String number_up = number.toUpperCase();
        editor.putString("hoscode", hoscode);
        editor.putString("number", number_up);
        editor.commit();
    }


    public void bind_widget() {
        edt_hoscode = (EditText) findViewById(R.id.hoscode);
        edt_number = (EditText) findViewById(R.id.number);

        readSetting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bind_widget();
        Log.d("smart_alert", "Start");

    }

    @Override
    protected void onResume() {
        Log.d("smart_alert", "Resume");
        super.onResume();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }


    public void onBtnClick(View v) {
        Log.d("smart_alert", "save setting.");
        writeSetting();

        Intent intent = new Intent(this, WaitActivity.class);
        startActivity(intent);

        finish();
    }


}
