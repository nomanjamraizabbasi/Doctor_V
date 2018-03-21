package com.example.mjamraizabbasi.doctorvinterfaces;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

public class xrayAnalysisResult extends AppCompatActivity{
    RadioButton arm,leg,other,fractured,normal;
    Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_xray_analysis_result);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        arm = (RadioButton) findViewById(R.id.arm);
        leg = (RadioButton) findViewById(R.id.leg);
        other = (RadioButton) findViewById(R.id.other);
        fractured = (RadioButton) findViewById(R.id.fractured);
        normal = (RadioButton) findViewById(R.id.normal);
        doneButton = (Button) findViewById(R.id.doneBtn);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),homepageActivity.class);
                view.getContext().startActivity(intent);
            }
        });

        String name = preferences.getString("xray_name", "other");
        boolean broken = preferences.getBoolean("fractured",false);

        String result = "XRAY DETECTED: "+name+","+"BONE STATUS: "+broken;
        //INSERT RESULT IN DATABASE IN XRAY LOG

        switch (name){
            case ("ARM"):
                arm.setChecked(true);
                break;
            case ("LEG"):
                leg.setChecked(true);
                break;
            default:
                other.setChecked(true);
        }

        switch (Boolean.toString(broken)){
            case ("false"):
                normal.setChecked(true);
                break;
            default:
                fractured.setChecked(true);
        }

    }
}