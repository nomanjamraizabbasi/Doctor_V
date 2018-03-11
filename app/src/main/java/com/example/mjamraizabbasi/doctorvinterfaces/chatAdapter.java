package com.example.mjamraizabbasi.doctorvinterfaces;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by M Jamraiz Abbasi on 2/12/2018.
 */

public class chatAdapter extends ArrayAdapter<DataProvider> {
    private List<DataProvider> chat_list=new ArrayList<DataProvider>();
    private TextView CHAT_TEXT;
    Context CTX;

    public chatAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        CTX=context;
    }

    //adds a new row to listview
    @Override
    public void add(@Nullable DataProvider object) {
        chat_list.add(object);
        super.add(object);
    }

    //returns total number of rows in listview
    @Override
    public int getCount() {
        return chat_list.size();
    }

    //returns each row of listview
    @Override
    public DataProvider getItem(int position) {
        return chat_list.get(position);
    }

    //returns view of each row of listview
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //for first row
        if ((convertView==null)){
            LayoutInflater inflater= (LayoutInflater) CTX.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView= inflater.inflate(R.layout.single_message_layout,parent,false); //false so it appears on the left always
        }
        CHAT_TEXT = convertView.findViewById(R.id.singleMessage);
        String message;
        boolean POSITION;

        //Get particular row at the certain position
        DataProvider provider= getItem(position);

        //save the message of this particular row in a string
        message= provider.message;

        //Set the position as the position of the item
        POSITION= provider.position;

        //Display message on the textview
        CHAT_TEXT.setText(message);

        //set background image of textview based on the fact that
        //if position is true then yellow background if false then green

        CHAT_TEXT.setBackgroundResource (POSITION ? R.drawable.bubble_green : R.drawable.bubble_yellow);

        //set position of the textview to the right if true and left if false
        LinearLayout.LayoutParams params= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if(!POSITION){
            params.gravity= Gravity.RIGHT;
        }
        else {
            params.gravity= Gravity.LEFT;
        }

        CHAT_TEXT.setLayoutParams(params);
        return convertView;

    }
}
