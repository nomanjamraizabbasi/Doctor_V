package com.example.mjamraizabbasi.doctorvinterfaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.mjamraizabbasi.doctorvinterfaces.columns.FIRST_COLUMN;
import static com.example.mjamraizabbasi.doctorvinterfaces.columns.SECOND_COLUMN;

public class historylogActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    String Date, secondColumn, selectionType, username;
    Boolean selectedtype_all;
    private DrawerLayout drawerLayout;
    TextView heading1, heading2;
    ListView listView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    public static ArrayList<HashMap<String, String>> list, list2;
    Spinner type_spinner;
    String url="http://doctorv.000webhostapp.com/connect/get_userhistory.php";
    String url2="http://doctorv.000webhostapp.com/connect/get_userhistory_TypeSpecific.php";
    NavigationView navigationView;
    UserDbHelper userDbHelper;
    SQLiteDatabase sqLiteDatabase;
    Context context=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historylog);


        list = new ArrayList<HashMap<String,String>>();

        listView=(ListView)findViewById(R.id.listView_hsitory);
        type_spinner=(Spinner)findViewById(R.id.history_log_spinner);
        heading1=(TextView)findViewById(R.id.textfieldHeading1);
        heading2=(TextView)findViewById(R.id.textfieldHeading2);
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

        type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectionType= type_spinner.getSelectedItem().toString();
                if(selectionType.equals("All")){
                    selectedtype_all=true;
                   heading2.setText("Date");
                   heading1.setText("Type");


                }
                else {
                    heading2.setText("Date");
                    heading1.setText("Result");
                }
                populateListAll();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });






    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);

    }

    private void populateListAll() {
        //StringRequest stringRequest;
        list.clear();
        userDbHelper= new UserDbHelper(context);
        sqLiteDatabase= userDbHelper.getReadableDatabase();
        if(selectionType.equals("All")){

            Cursor cursor= userDbHelper.get_AllLogs(username,sqLiteDatabase);
            if(cursor.moveToFirst()){
                do{
                    Date= cursor.getString(0);
                    secondColumn= cursor.getString(1);
                    HashMap<String,String> temp=new HashMap<String, String>();
                    temp.put(FIRST_COLUMN,Date);
                    temp.put(SECOND_COLUMN,secondColumn);
                    list.add(temp);
                }
                while (cursor.moveToNext());

            }
            historyListviewAdapter adapter = new historyListviewAdapter(historylogActivity.this, list);
            listView.setAdapter(adapter);
        }

        else{

            Cursor cursor= userDbHelper.get_SpecificLogs(username,selectionType,sqLiteDatabase);
            if(cursor.moveToFirst()){
                do{
                    Date= cursor.getString(0);
                    secondColumn= cursor.getString(1);
                    HashMap<String,String> temp=new HashMap<String, String>();
                    temp.put(FIRST_COLUMN,Date);
                    temp.put(SECOND_COLUMN,secondColumn);
                    list.add(temp);
                }
                while (cursor.moveToNext());
            }
            historyListviewAdapter adapter = new historyListviewAdapter(historylogActivity.this, list);
            listView.setAdapter(adapter);
        }

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

