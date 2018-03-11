package com.example.mjamraizabbasi.doctorvinterfaces;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class diagnosisAcivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    ListView listView;
    EditText chat_text;
    Button send;
    ImageButton mic;
    boolean position= false, done=false;
    chatAdapter adapter;
    Context ctx=this;
    String server_url= "http://doctorv.000webhostapp.com/connect/save_medicaldata.php";
    String username, currentDate;
    NavigationView navigationView;
    UserDbHelper userDbHelper;
    SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosis_acivity);

        drawerLayout= (DrawerLayout)findViewById(R.id.drawerid);
        mic=(ImageButton)findViewById(R.id.mic_btn);
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

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //save the diagnosis result in the medical log
                done=true;
                if(done) {
                    //get system date
                    Calendar calendar= Calendar.getInstance();
                    currentDate = DateFormat.getDateInstance().format(new Date());

                    userDbHelper= new UserDbHelper(ctx);
                    sqLiteDatabase= userDbHelper.getWritableDatabase();
                    userDbHelper.add_newrecord(username,currentDate,"Diagnosis","abcgsh",sqLiteDatabase);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {

                            if (response.equals("inserted")) {
                                Toast.makeText(diagnosisAcivity.this, "data saved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(diagnosisAcivity.this, "data not saved", Toast.LENGTH_SHORT).show();
                            }

                        }

                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(diagnosisAcivity.this, "Error", Toast.LENGTH_SHORT);
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {

                            Map<String, String> params = new HashMap<String, String>();
                            params.put("username", username);
                            params.put("date", currentDate);
                            params.put("type", "Diagnosis");
                            params.put("details", "abcdefg");

                            return params;

                        }
                    };
                    MySingleton.getInstance(diagnosisAcivity.this).addToRequestQueue(stringRequest);
                }

            }
        });


        listView=(ListView)findViewById(R.id.chat_list_view);
        chat_text= (EditText)findViewById(R.id.chattext);
        send= (Button)findViewById(R.id.send_button);
        adapter= new chatAdapter(ctx, R.layout.single_message_layout);
        listView.setAdapter(adapter);
        //set the listview scrollable
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        //set the message to display at the end
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(listView.getCount()-1);
            }
        });

        //set on click listener on the send button
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.add(new DataProvider(position,chat_text.getText().toString()));

                //set it to opposite to display next message on the alterate side
                position=!position;
                chat_text.setText("");
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);

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
