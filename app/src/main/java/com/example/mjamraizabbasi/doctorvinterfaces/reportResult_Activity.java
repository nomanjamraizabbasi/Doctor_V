package com.example.mjamraizabbasi.doctorvinterfaces;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.example.mjamraizabbasi.doctorvinterfaces.columns.FIRST_COLUMN;
import static com.example.mjamraizabbasi.doctorvinterfaces.columns.SECOND_COLUMN;

public class reportResult_Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    ListView listView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    public static ArrayList<HashMap<String, String>> list, list2;
    NavigationView navigationView;
    String [] testnames;
    Float [] testValues;
    String server_url= "http://doctorv.000webhostapp.com/connect/save_medicaldata.php";
    String names,values, currentDate, username, gender;
    Context context=this;
    UserDbHelper userDbHelper;
    SQLiteDatabase sqLiteDatabase;
    String evaluation="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_result_);

        list = new ArrayList<HashMap<String,String>>();
        listView=(ListView)findViewById(R.id.listView_result);
        drawerLayout= (DrawerLayout)findViewById(R.id.drawerid);

        actionBarDrawerToggle= new ActionBarDrawerToggle(this,drawerLayout,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Color the acion bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff2b78b4));

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        names = preferences.getString("testnames", "defaultValue");
        values = preferences.getString("testvalues", "defaultValue");
        username = preferences.getString("username", "defaultValue");
        gender = preferences.getString("gender", "defaultValue");

        //Save names in testnames array
        testnames= names.split(",");
        testValues= new Float[values.split(",").length];

        //Save values in testvalues array
        for(int i=0; i<values.split(",").length;i++){
            testValues[i]= Float.parseFloat(values.split(",")[i]);
        }

        populate_Listview(this);
        //saveResultsInMySql(evaluation);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get the selected item text from ListView

                HashMap<String,String> selectedRow = (HashMap<String,String>)adapterView.getItemAtPosition(i);
                float currentValue = Float.parseFloat(selectedRow.get("Second"));


                if(selectedRow.get("First").contains("RBC")){
                    String [] str= getResult("RBC", currentValue);
                    showAlert(str[0],str[1],view.getContext());

                }

                else if(selectedRow.get("First").contains("Hb") || selectedRow.get("First").contains("Hemoglobin")
                        || selectedRow.get("First").contains("Heamoglobin")){
                    String [] str= getResult("Hb", currentValue);
                    showAlert(str[0],str[1],view.getContext());
                }

                else if(selectedRow.get("First").contains("WBC")){
                    String [] str= getResult("WBC", currentValue);
                    showAlert(str[0],str[1],view.getContext());
                }

                else if(selectedRow.get("First").contains("Platelet")){
                    String [] str= getResult("Platelet", currentValue);
                    showAlert(str[0],str[1],view.getContext());
                }

                else if(selectedRow.get("First").contains("ESR")){
                    String [] str= getResult("ESR", currentValue);
                    showAlert(str[0],str[1],view.getContext());
                }

                else if(selectedRow.get("First").contains("ALT") || selectedRow.get("First").contains("SGPT")){
                    String [] str= getResult("ALT", currentValue);
                    showAlert(str[0],str[1],view.getContext());
                }

                else if(selectedRow.get("First").contains("ALK") || selectedRow.get("First").contains("Alk.")
                        || selectedRow.get("First").contains("alk.")){
                    String [] str= getResult("ALK-Phos", currentValue);
                    showAlert(str[0],str[1],view.getContext());
                }

                else if(selectedRow.get("First").contains("Bilirubin")){
                    String [] str= getResult("Bilirubine", currentValue);
                    showAlert(str[0],str[1],view.getContext());
                }
            }
        });
    }

    public String[] getResult(String name,float currentValue){
        switch(name){
            case "ALT":
                if (currentValue < 7) {;
                    return new String[]{"Your ALT/SGPT level is below the normal range of 7 - 55 mg/dL. \n",getText(R.string.low_ALT_Symptoms) + "\n\n" + R.string.low_ALK_Reasons,"true"};
                } else if (currentValue > 55) {
                    return new String[]{"Your  ALT/SGPT level is above the normal range of 7 - 55 mg/dL. \n",getText(R.string.high_ALT_Symptoms) + "\n\n" + getText(R.string.high_ALT_Reasons),"true"};
                } else {
                    return new String[]{"Normal ALT/SGPT","Your ALT/SGPT level count is normal and within the range of 7 - 55 mg/dL.","false"};
                }

            case "ALK-Phos":
                if (currentValue < 45) {
                    return new String[]{"Your ALK level is below the normal range of 45 - 115 mg/dL. \n",getText(R.string.low_ALT_Symptoms) + "\n\n" + getText(R.string.low_ALK_Reasons),"true"};
                } else if (currentValue > 115) {
                    return new String[]{"Your  ALK level is above the normal range of 45 - 115 mg/dL. \n",getText(R.string.high_ALT_Symptoms) + "\n\n" + getText(R.string.high_ALT_Reasons),"true"};
                } else {
                    return new String[]{"Normal ALK", "Your ALK level count is normal and within the range of 45 - 115 mg/dL.", "false"};
                }

            case "Bilirubine":
                if (currentValue < 0.1) {
                    return new String[]{"Your Bilirubin is below the normal range of 0.1 - 1.2 mg/dL. \n",getText(R.string.low_Bil_Symptoms) + "\n\n" + getText(R.string.low_Bil_Reasons),"true"};
                } else if (currentValue > 1.2) {
                    return new String[]{"Your  Bilirubin is above the normal range of 0.1 - 1.2 mg/dL. \n",R.string.high_Bil_Symptoms + "\n\n" + R.string.high_Bil_Reasons,"true"};
                } else {
                    return new String[]{"Normal Bilirubin", "Your Bilirubin count is normal and within the range of 0.1 - 1.2 mg/dL.", "false"};
                }

            case "WBC":
                if (currentValue < 4500.0) {
                    return new String[]{"Your WBC is below the normal range of 4500 - 10000 cells/mL. \n",getText(R.string.low_WBC_Symptoms) + "\n\n" + getText(R.string.low_WBC_Reasons),"true"};
                } else if (currentValue > 10000.0) {
                    return new String[]{"Your WBC is above the normal range of 4500 - 10000 cells/mL. \n",getText(R.string.high_WBC_Symptoms) + "\n\n" + getText(R.string.high_WBC_Reasons),"true"};
                } else {
                    return new String[]{"Normal WBC", "Your WBC count is normal and within the range of 4500 - 10000 cells/mL.", "false"};
                }

            case "Platelet":
                if (currentValue < 150000.0) {
                    return new String[]{"Your Platelets are below the normal range of 150000 - 450000 cells/mL. \n",getText(R.string.low_Platelets_Symptoms) + "\n\n" + getText(R.string.low_Platelets_Reasons),"true"};
                } else if (currentValue > 450000.0) {
                    return new String[]{"Your Platelets is above the normal range of 150000 - 450000 cells/mL. \n",getText(R.string.high_Platelets_Symptoms) + "\n\n" + getText(R.string.high_Platelets_Reasons),"true"};
                } else {
                    return new String[]{"Normal Platelets", "Your HB count is normal and within the range of 150000 - 450000 cells/mL.", "false"};
                }

            case "ESR":
                if (currentValue > 20.0) {
                    return new String[]{"Your ESR is above the normal range of 20 mm/hour. \n",getText(R.string.high_ESR_Symptoms) + "\n\n" + getText(R.string.high_ESR_Reasons),"true"};
                } else {
                    return new String []{"Normal ESR", "Your ESRs value is in normal range of 0-20 mm/hour. ", "false"};
                }

            case "RBC":
                if(gender.equals("female")){
                    if (currentValue < 4.2) {
                        return new String[]{"Your RBC is below the normal range of 4.2 - 5.4 mill/mL. \n",getText(R.string.low_RBC_Symptoms) + "\n\n" + getText(R.string.low_RBC_Reasons),"true"};
                     } else if (currentValue > 5.4) {
                        return new String[]{"Your RBC is above the normal range of 4.2 - 5.4 mill/mL. \n",getText(R.string.high_RBC_Symptoms) + "\n\n" + getText(R.string.high_RBC_Reasons),"true"};
                     } else {
                        return new String[]{"Normal RBC", "Your RBC count is normal and within the range of 4.2 - 5.4 mill/mL.", "false"};
                    }
                }

                if(gender.equals("male")){
                    if (currentValue < 4.6) {
                        return new String[]{"Your RBC is below the normal range of 4.2 - 5.4 mill/mL. \n",getText(R.string.low_RBC_Symptoms) + "\n\n" + getText(R.string.low_RBC_Reasons),"true"};
                    } else if (currentValue > 6.1) {
                        return new String[]{"Your RBC is above the normal range of 4.2 - 5.4 mill/mL. \n",getText(R.string.high_RBC_Symptoms) + "\n\n" + getText(R.string.high_RBC_Reasons),"true"};
                    } else {
                        return new String[]{"Normal RBC", "Your RBC count is normal and within the range of 4.2 - 5.4 mill/mL.", "false"};
                    }
                }



            case "Hb":
                if(gender.equals("female")){
                    if (currentValue < 12.0) {
                        return new String[]{"Your Hb is below the normal range of 12 - 15 g/dL. \n",getText(R.string.low_HB_Symptoms) + "\n\n" + getText(R.string.low_HB_Reasons),"true"};
                    } else if (currentValue > 15.0) {
                       return new String[]{"Your Hb is above the normal range of 12 - 15 g/dL. \n",getText(R.string.high_HB_Symptoms) + "\n\n" + getText(R.string.high_HB_Reasons),"true"};
                    } else {
                        return new String[]{"Normal Hb", "Your HB count is normal and within the range of 12 - 15 g/dL.", "false"};
                    }
                }

                if(gender.equals("male")){
                    if (currentValue < 14.0) {
                        return new String []{"Your Hb is below the normal range of 14 - 17 g/dL. \n",getText(R.string.low_HB_Symptoms) + "\n\n" + getText(R.string.low_HB_Reasons),"true"};
                    } else if (currentValue > 17.0) {
                        return new String[]{"Your Hb is above the normal range of 14 - 17 g/dL. \n",getText(R.string.high_HB_Symptoms) + "\n\n" + getText(R.string.high_HB_Reasons),"true"};
                    } else {
                        return new String []{"Normal Hb", "Your HB count is normal and within the range of 14 - 17 g/dL.","false"};
                    }
                }

            default:
                return new String[]{"","","false"};
        }

    }

    public void populate_Listview(Context context){
        list.clear();
        for(int i=0; i<testValues.length; i++){
            if(testnames[i].contains("WBC") || testnames[i].contains("RBC") || testnames[i].contains("Hemoglobin") || testnames[i].contains("Hb")
                    || testnames[i].contains("Haemoglobin") || testnames[i].contains("Platelet") || testnames[i].contains("ESR")
                    ||testnames[i].contains("Bilirubin") || testnames[i].equals("ALT") || testnames[i].equals("SGPT")
                    || testnames[i].equals("ALK-Phos") || testnames[i].contains("Alk.")
                    || testnames[i].equals("ALK")){
                HashMap<String,String> temp=new HashMap<String, String>();
                temp.put(FIRST_COLUMN,testnames[i]);
                temp.put(SECOND_COLUMN,testValues[i].toString());
                list.add(temp);
                String [] strArray=null;
                if(testnames[i].contains("Bilirubin")){
                    strArray= getResult("Bilirubine",testValues[i]);
                }
                else if(testnames[i].contains("ALT") || testnames[i].contains("SGPT")){
                    strArray= getResult("ALT",testValues[i]);
                }
                else if(testnames[i].contains("ALK") || testnames[i].contains("Alk.")){
                    strArray= getResult("ALK-Phos",testValues[i]);
                }
                else if(testnames[i].contains("Hb") || testnames[i].contains("Hemoglobin")|| testnames[i].contains("Heamoglobin")){
                    strArray= getResult("Hb",testValues[i]);
                }
                else if(testnames[i].contains("Platelet")){
                    strArray= getResult("Hb",testValues[i]);
                }
                else if(testnames[i].contains("WBC")){
                    strArray= getResult("ALK-Phos",testValues[i]);
                }
                else if(testnames[i].contains("RBC") ){
                    strArray= getResult("Hb",testValues[i]);
                }
                else{
                   getResult(testnames[i],testValues[i]);
                }

                String result= strArray[2];
                if(result.equals("true")){
                    //save results in a string
                    evaluation= evaluation + strArray[0]+strArray[1];
                }

            }
        }
        historyListviewAdapter adapter = new historyListviewAdapter(reportResult_Activity.this, list,context);
        listView.setAdapter(adapter);

        Calendar calendar= Calendar.getInstance();
        currentDate = DateFormat.getDateInstance().format(new Date());

        //send results to sqlite and mysql
        saveResultInSqlite(evaluation);
        saveResultsInMySql(evaluation);
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

    public static void showAlert(String Title, String message, Context con) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(con);
        dialog.setMessage(message);
        dialog.setTitle(Title);
        dialog.setPositiveButton(" OK ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

            }
        });
        dialog.show();

    }

    public void saveResultInSqlite(String Result){
        userDbHelper= new UserDbHelper(context);
        sqLiteDatabase= userDbHelper.getWritableDatabase();
        userDbHelper.add_newrecord(username,currentDate,"Report Analysis",Result,sqLiteDatabase);

    }

    public  void saveResultsInMySql(final String Result){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {

                if (response.equals("inserted")) {
                    //Toast.makeText(reportResult_Activity.this, "data saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(reportResult_Activity.this, "Network Error", Toast.LENGTH_SHORT).show();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(reportResult_Activity.this, "Error", Toast.LENGTH_SHORT);
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("date", currentDate);
                params.put("type", "Report Analysis");
                params.put("details", Result);

                return params;

            }
        };
        MySingleton.getInstance(reportResult_Activity.this).addToRequestQueue(stringRequest);

    }
}
