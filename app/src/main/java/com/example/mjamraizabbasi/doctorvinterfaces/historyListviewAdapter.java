package com.example.mjamraizabbasi.doctorvinterfaces;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.mjamraizabbasi.doctorvinterfaces.columns.FIRST_COLUMN;
import static com.example.mjamraizabbasi.doctorvinterfaces.columns.SECOND_COLUMN;

/**
 * Created by M Jamraiz Abbasi on 2/13/2018.
 */

public class historyListviewAdapter extends BaseAdapter {

    public ArrayList<HashMap<String, String>> list;
    Activity activity;
    TextView txtFirst;
    TextView txtSecond;
    Context context;
    String gender;

    public historyListviewAdapter(Activity activity,ArrayList<HashMap<String, String>> list,Context context) {
        super();
        this.activity=activity;
        this.list=list;
        this.context = context;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.gender = preferences.getString("gender", "defaultValue");
        int a = 0;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        LayoutInflater inflater=activity.getLayoutInflater();

        if(convertView == null) {

            convertView = inflater.inflate(R.layout.history_log_row_structure, null);

            txtFirst = (TextView) convertView.findViewById(R.id.Date);
            txtSecond = (TextView) convertView.findViewById(R.id.Result);
        }

        HashMap<String, String> map=list.get(position);
        txtFirst.setText(map.get(FIRST_COLUMN));
        txtSecond.setText(map.get(SECOND_COLUMN));
        Log.d("row",map.get(FIRST_COLUMN)+","+map.get(SECOND_COLUMN));

        if(map.get(FIRST_COLUMN).contains("WBC")){ //recheck this logic
            if(Float.parseFloat(map.get(SECOND_COLUMN))<4500.0|| Float.parseFloat(map.get(SECOND_COLUMN))>10000.0)
                convertView.setBackgroundColor(Color.RED);
            else{
                convertView.setBackgroundColor(Color.GREEN);
            }
        }
        else if(map.get(FIRST_COLUMN).contains("RBC") && gender.equals("male")){ //rechek this logic
            if(Float.parseFloat(map.get(SECOND_COLUMN))<4.6|| Float.parseFloat(map.get(SECOND_COLUMN))>6.1)
                convertView.setBackgroundColor(Color.RED);
            else{
                convertView.setBackgroundColor(Color.GREEN);
            }
        }
        else if(map.get(FIRST_COLUMN).contains("RBC") && gender.equals("female")){ //recheck logic
            if(Float.parseFloat(map.get(SECOND_COLUMN))<4.2|| Float.parseFloat(map.get(SECOND_COLUMN))>5.4)
                convertView.setBackgroundColor(Color.RED);
            else{
                convertView.setBackgroundColor(Color.GREEN);
            }
        }
        else if(map.get(FIRST_COLUMN).contains("Platelet")){ //recheck logic
            if(Float.parseFloat(map.get(SECOND_COLUMN))<1500000.0 || Float.parseFloat(map.get(SECOND_COLUMN))>450000.0){
                convertView.setBackgroundColor(Color.RED);
            }
            else{
                convertView.setBackgroundColor(Color.GREEN);
            }
        }
        else if(map.get(FIRST_COLUMN).contains("Hb") || map.get(FIRST_COLUMN).contains("HB")  //recheck logic
                || map.get(FIRST_COLUMN).contains("Hemoglobin") && gender.equals("male")){
            if(Float.parseFloat(map.get(SECOND_COLUMN))<14|| Float.parseFloat(map.get(SECOND_COLUMN))>17)
                convertView.setBackgroundColor(Color.RED);
            else{
                convertView.setBackgroundColor(Color.GREEN);
            }
        }
        else if(map.get(FIRST_COLUMN).contains("Hb") || map.get(FIRST_COLUMN).contains("HB")
                || map.get(FIRST_COLUMN).contains("Hemoglobin") && gender.equals("female")){
            if(Float.parseFloat(map.get(SECOND_COLUMN))<12|| Float.parseFloat(map.get(SECOND_COLUMN))>15)
                convertView.setBackgroundColor(Color.RED);
            else{
                convertView.setBackgroundColor(Color.GREEN);
            }
        }
        else if(map.get(FIRST_COLUMN).contains("Bilirubin")){
            if(Float.parseFloat(map.get(SECOND_COLUMN))<0.1 || Float.parseFloat(map.get(SECOND_COLUMN))>1.2){
                getView(position,convertView,viewGroup).setBackgroundColor(Color.RED);
            }
            else{
                convertView.setBackgroundColor(Color.GREEN);
            }

        }
        else if(map.get(FIRST_COLUMN).contains("ALT")){
            if(Float.parseFloat(map.get(SECOND_COLUMN))<7.0 || Float.parseFloat(map.get(SECOND_COLUMN))>55.0){
                convertView.setBackgroundColor(Color.RED);
            }
            else{
                convertView.setBackgroundColor(Color.GREEN);
            }

        }
        else if(map.get(FIRST_COLUMN).contains("ALK") || map.get(FIRST_COLUMN).contains("Alk.")){
            if(Float.parseFloat(map.get(SECOND_COLUMN))<45 || Float.parseFloat(map.get(SECOND_COLUMN))>115){
                convertView.setBackgroundColor(Color.RED);
            }
            else{
                convertView.setBackgroundColor(Color.GREEN);
            }

        }


        return convertView;
    }
}
