package com.example.mjamraizabbasi.doctorvinterfaces;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class setProfileActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    String username, url1="http://doctorv.000webhostapp.com/connect/update_userInfo.php";
    EditText Fullname,Email,Contact,DOB,Location;
    String json_url="http://doctorv.000webhostapp.com/connect/get_userdata.php";
    ImageButton edit_fname_btn,edit_email_btn, edit_dob_btn, edit_location_btn, edit_contact_btn;
    AlertDialog.Builder builder;
    int month_x, year_x,day_x;
    static final int DIALOG_ID=0;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);
        drawerLayout= (DrawerLayout)findViewById(R.id.drawerid);
        actionBarDrawerToggle= new ActionBarDrawerToggle(this,drawerLayout,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Color the acion bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff2b78b4));

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Get Username from the login activity
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        username = preferences.getString("username", "defaultValue");
        //Toast.makeText(this,username, Toast.LENGTH_SHORT).show();
        //Get Username from the login activity


        builder = new AlertDialog.Builder(setProfileActivity.this);

        Fullname=(EditText)findViewById(R.id.fullnameET);
        Email=(EditText)findViewById(R.id.emailET);
        Contact=(EditText)findViewById(R.id.contactET);
        DOB=(EditText)findViewById(R.id.dateET);
        Location=(EditText)findViewById(R.id.locationET);

        //Set all the field uneditable
        Fullname=(EditText)findViewById(R.id.fullnameET);
        Email=(EditText)findViewById(R.id.emailET);
        Contact=(EditText)findViewById(R.id.contactET);
        DOB=(EditText)findViewById(R.id.dateET);
        Location=(EditText)findViewById(R.id.locationET);


        //Display user data on the respective fields
        display_userinfo();

        //set change focus listener
        Fullname.setOnFocusChangeListener(this);
        Email.setOnFocusChangeListener(this);
        DOB.setOnFocusChangeListener(this);
        Location.setOnFocusChangeListener(this);
        Contact.setOnFocusChangeListener(this);


        edit_contact_btn=(ImageButton)findViewById(R.id.editContactButton);
        edit_contact_btn.setOnClickListener(this);

        edit_fname_btn=(ImageButton)findViewById(R.id.editNameButton);
        edit_fname_btn.setOnClickListener(this);

        edit_dob_btn=(ImageButton)findViewById(R.id.editDateButton);
        edit_dob_btn.setOnClickListener(this);

        edit_location_btn=(ImageButton)findViewById(R.id.editLocationButton);
        edit_location_btn.setOnClickListener(this);

        edit_email_btn=(ImageButton)findViewById(R.id.editEmailButton);
        edit_email_btn.setOnClickListener(this);
    }

    protected void display_userinfo() {

        StringRequest stringRequest= new StringRequest(Request.Method.POST, json_url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {

                        JSONArray jsonArray;
                        try {
                            jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            Fullname.setText(jsonObject.getString("FName"));
                            Email.setText(jsonObject.getString("Email"));
                            Contact.setText(jsonObject.getString("Contact"));
                            DOB.setText(jsonObject.getString("DOB"));
                            Location.setText(jsonObject.getString("Loc"));

                            //set all fields uneditable
                            setFieldsUneditable();
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(setProfileActivity.this, "Error", Toast.LENGTH_SHORT);

            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params= new HashMap<String, String>();
                params.put("username", username);
                return params;

            }
        };
        MySingleton.getInstance(setProfileActivity.this).addToRequestQueue(stringRequest);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editprofile_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.updateTick:
                updateInfo();
                break;
        }


        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.editNameButton:
                edit_fname_btn.setImageResource(R.drawable.pencildisabledbigger);
                //Make name textfield edirable
                Fullname.setFocusable(true);
                Fullname.setFocusableInTouchMode(true);
                Fullname.setCursorVisible(true);
                Fullname.setClickable(true);

                break;
            case  R.id.editDateButton:
                edit_dob_btn.setImageResource(R.drawable.pencildisabledbigger);
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                year_x = c.get(Calendar.YEAR);
                month_x = c.get(Calendar.MONTH);
                day_x = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                DOB.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                edit_dob_btn.setImageResource(R.drawable.pencilsmall);
                            }
                        }, year_x, month_x, day_x);
                datePickerDialog.show();
                datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
                break;
            case  R.id.editLocationButton:
                edit_location_btn.setImageResource(R.drawable.pencildisabledbigger);
                Location.setFocusableInTouchMode(true);
                Location.setFocusable(true);
                Location.setClickable(true);
                Location.setCursorVisible(true);
                break;
            case R.id.editContactButton:
                edit_contact_btn.setImageResource(R.drawable.pencildisabledbigger);
                Contact.setFocusableInTouchMode(true);
                Contact.setFocusable(true);
                Contact.setClickable(true);
                Contact.setCursorVisible(true);
                break;
            case R.id.editEmailButton:
                edit_email_btn.setImageResource(R.drawable.pencildisabledbigger);
                Email.setFocusableInTouchMode(true);
                Email.setFocusable(true);
                Email.setClickable(true);
                Email.setCursorVisible(true);
                break;

        }
    }

    //This methods sets the edittexts uneditable after they lose focus and turn the edit button enabled again
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        switch (view.getId()){
            case   R.id.fullnameET:
                if(!hasFocus){
                    Fullname.setFocusable(false);
                    Fullname.setClickable(false);
                    Fullname.setCursorVisible(false);
                    edit_fname_btn.setImageResource(R.drawable.pencilsmall);
                }
                break;
            case R.id.emailET:
                if(!hasFocus){
                    Email.setFocusable(false);
                    Email.setClickable(false);
                    Email.setCursorVisible(false);
                    edit_email_btn.setImageResource(R.drawable.pencilsmall);
                }
                break;
            case R.id.locationET:
                if(!hasFocus){
                    Location.setFocusable(false);
                    Location.setCursorVisible(false);
                    Location.setClickable(false);
                    edit_location_btn.setImageResource(R.drawable.pencilsmall);
                }
                break;
            case R.id.contactET:
                if(!hasFocus){
                    Contact.setFocusable(false);
                    Contact.setClickable(false);
                    Contact.setCursorVisible(false);
                    edit_contact_btn.setImageResource(R.drawable.pencilsmall);
                }
                break;
        }

    }

    public void updateInfo(){
        StringRequest stringRequest= new StringRequest(Request.Method.POST, url1, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                builder.setTitle("Profile Updation");
                builder.setMessage("Your Profile Has Been Updated!");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(response.equals("updated")){
                            Toast.makeText(setProfileActivity.this,"Password Updated", Toast.LENGTH_LONG);
                            display_userinfo();
                        }
                        else {


                        }
                    }
                });
                AlertDialog alertDialog= builder.create();
                alertDialog.show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(setProfileActivity.this,"Error", Toast.LENGTH_SHORT);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params= new HashMap<String, String>();
                params.put("username", username);
                params.put("email", Email.getText().toString());
                params.put("dob", DOB.getText().toString());
                params.put("contact", Contact.getText().toString());
                params.put("loc", Location.getText().toString());
                params.put("fname", Fullname.getText().toString());
                return params;

            }
        };
        MySingleton.getInstance(setProfileActivity.this).addToRequestQueue(stringRequest);
    }

    public void setFieldsUneditable(){
        Fullname.setFocusable(false);
        Fullname.setClickable(false);
        Fullname.setCursorVisible(false);

        Email.setFocusable(false);
        Email.setClickable(false);
        Email.setCursorVisible(false);

        Contact.setFocusable(false);
        Contact.setClickable(false);
        Contact.setCursorVisible(false);

        DOB.setFocusable(false);
        DOB.setCursorVisible(false);
        DOB.setClickable(false);

        Location.setFocusable(false);
        Location.setCursorVisible(false);
        Location.setClickable(false);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.HomeItem:
                Intent homescreen = new Intent(this,homepageActivity.class);
                startActivity(homescreen);
                break;
            case R.id.ProfileSettingItem:
                Intent profilescreen = new Intent(this,setProfileActivity.class);
                startActivity(profilescreen);
                break;
            case R.id.ChangePasswordItem:
                //INTERNET CONNECTION CHECK COMES HERE
                Intent changepasswordscreen = new Intent(this,changePasswordActivity.class);
                startActivity(changepasswordscreen);
                break;
            case R.id.HistorylogItem:
                Intent historyscreen = new Intent(this,historylogActivity.class);
                startActivity(historyscreen);
                break;
            case R.id.LogoutItem:
                //LOGOUT LOGIC WILL COME HERE
                break;
        }
        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
