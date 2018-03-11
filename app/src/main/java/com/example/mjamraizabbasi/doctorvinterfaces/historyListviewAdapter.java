package com.example.mjamraizabbasi.doctorvinterfaces;

import android.app.Activity;
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

    public historyListviewAdapter(Activity activity,ArrayList<HashMap<String, String>> list) {
        super();
        this.activity=activity;
        this.list=list;
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
        return convertView;
    }
}
