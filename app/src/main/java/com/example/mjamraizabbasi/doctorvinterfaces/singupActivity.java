package com.example.mjamraizabbasi.doctorvinterfaces;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.kofigyan.stateprogressbar.StateProgressBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.icu.text.IDNA.Info;

public class singupActivity extends Activity {
    public boolean done;
    Button next_btn;
    Spinner dateSpinner, monthSpinner, yearSpinner, locationSpinner;
    String fname, date_of_birth, contactNo, location, errorString;
    TextView countrycodeTextView;
    EditText fullname,contact;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_singup);

        RequestQueue requestQueue;

        dateSpinner=(Spinner)findViewById(R.id.day_spinner);
        monthSpinner=(Spinner)findViewById(R.id.month_spinner);
        yearSpinner = (Spinner) findViewById(R.id.year_spinner);
        locationSpinner= (Spinner) findViewById(R.id.location_spinner);
        next_btn = (Button) findViewById(R.id.nextBtn) ;
        countrycodeTextView= (TextView) findViewById(R.id.countrycode);
        fullname=(EditText)findViewById(R.id.fullnameET);
        contact=(EditText)findViewById(R.id.contact_ET);
        //Populate year_spinner with years list
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        ArrayAdapter<String> adapter;
        List<String> list;

        list = new ArrayList<String>();
        list.add("Year");
        for(int i = 1950; i <= year; i++)
        {
            list.add(Integer.toString(i));
        }

        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.year_spinner_item, list);
        adapter.setDropDownViewResource(R.layout.year_spinner_dropdown);
        yearSpinner.setAdapter(adapter);

        //Click event of next button
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean checkvalidation= validate();
                if(checkvalidation==true){
                    save_info();
                    //Toast.makeText(singupActivity.this,fname,Toast.LENGTH_LONG).show();
                    Intent myintent =new Intent(singupActivity.this, signupActivity1.class);
                    myintent.putExtra("fname", fname);
                    myintent.putExtra("location", location);
                    myintent.putExtra("dob", date_of_birth);
                    myintent.putExtra("contact", contactNo);
                    startActivity(myintent);
//
                }
                else
                    return;

            }
        });

        //click event for location spinner select item
        //Set country code based on what is selected in the location
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                //save location
                location= locationSpinner.getSelectedItem().toString();

                if(location.equals("Pakistan")){
                    countrycodeTextView.setText("+92");
                }
                else if(location.equals("India")){
                    countrycodeTextView.setText("+91");
                }
                else if(location.equals("USA")){
                    countrycodeTextView.setText("+1");
                }
                else if(location.equals("UK")){
                    countrycodeTextView.setText("+44");
                }

                //append country code in contactNo
                contactNo= countrycodeTextView.getText().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //Click event for day spinner
        //Save date
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                date_of_birth= dateSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Save month
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                date_of_birth= date_of_birth + "-"+ monthSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Save year
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                date_of_birth= date_of_birth + "-"+ yearSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private boolean validate(){
        done=true;
        errorString=" ";
        //if full name textfield is empty
        if(fullname.getText().toString().equals("")){
            errorString=errorString+"Input your fullname\n ";
            done=false;
        }

        //if contact textfield is empty
        if(contact.getText().toString().equals("")){
            errorString=errorString+"Input your contact\n";
            done=false;
        }

        //if location is not selected
        if(locationSpinner.getSelectedItemPosition()==0){
            errorString=errorString+"Select your Location\n";
            countrycodeTextView.setText("+00");
            done=false;
        }

        //if no date is sleectec
        if(dateSpinner.getSelectedItemPosition()==0){
            errorString=errorString+"Select your Date of Birth\n";
            done=false;
        }

        //if no date is sleectec
        if(monthSpinner.getSelectedItemPosition()==0){
            errorString=errorString+"Select your Month of Birth\n";
            done=false;
        }


        //if no date is sleectec
        if(yearSpinner.getSelectedItemPosition()==0){
            errorString=errorString+"Select your Year of Birth\n";
            done=false;
        }




       //Toast.makeText(singupActivity.this, errorString, Toast.LENGTH_LONG).show();
        return done;
    }

    //Process information entered
    public void save_info(){
        //save fullname
        fname= fullname.getText().toString();

        //save contact number
        contactNo= contactNo+ contact.getText().toString();

        //Toast.makeText(singupActivity.this, fname + date_of_birth + contactNo + location, Toast.LENGTH_LONG).show();
    }
}
