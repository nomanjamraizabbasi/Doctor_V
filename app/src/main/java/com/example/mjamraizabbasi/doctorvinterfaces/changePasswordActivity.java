package com.example.mjamraizabbasi.doctorvinterfaces;

import android.app.AlertDialog;
import android.app.Application;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
public class changePasswordActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    EditText currPass, newPass, confirmPass;
    Button resetPassword;
    String username,password, url="http://doctorv.000webhostapp.com/connect/getPassword.php", url1="http://doctorv.000webhostapp.com/connect/setPassword.php";
    JSONArray jsonArray;
    AlertDialog.Builder builder;
    Context context=this;
    SQLiteDatabase sqLiteDatabase;
    UserDbHelper userDbHelper;

    NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        drawerLayout= (DrawerLayout)findViewById(R.id.drawerid);
        actionBarDrawerToggle= new ActionBarDrawerToggle(this,drawerLayout,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Color the acion bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff2b78b4));

        builder = new AlertDialog.Builder(changePasswordActivity.this);

        //Get Username from the login activity
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        username = preferences.getString("username", "defaultValue");

        currPass= (EditText)findViewById(R.id.currentpasswordET);

        //set current password field non editable
        currPass.setFocusable(false);
        currPass.setClickable(false);
        currPass.setCursorVisible(false);
        //display current password in the currPass field
        displayCurrentPassword();

        newPass=(EditText)findViewById(R.id.newpasswordET);
        confirmPass=(EditText)findViewById(R.id.newpasswordretypeET);

        resetPassword= (Button)findViewById(R.id.resetpassword_btn2);

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savenewPassword();
            }
        });
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



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);

    }

    public void displayCurrentPassword(){

        StringRequest stringRequest= new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {

                        try {
                            jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            currPass.setText(jsonObject.getString("Pass"));


                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(changePasswordActivity.this, "Error", Toast.LENGTH_SHORT);

            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params= new HashMap<String, String>();
                params.put("username", username);
                return params;

            }
        };
        MySingleton.getInstance(changePasswordActivity.this).addToRequestQueue(stringRequest);

    }

    public void savenewPassword(){
        if(validatePassword()){
            StringRequest stringRequest= new StringRequest(Request.Method.POST, url1, new Response.Listener<String>() {
                @Override
                public void onResponse(final String response) {
                    builder.setTitle("Change Password");
                    builder.setMessage("Your Password Has Been Reset! \nSign In Again!");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(response.equals("inserted")){

                            }
                            else {
                                Toast.makeText(changePasswordActivity.this,"Password not Updated", Toast.LENGTH_LONG);
                                userDbHelper= new UserDbHelper(context);
                                sqLiteDatabase= userDbHelper.getWritableDatabase();
                                boolean inserted=userDbHelper.updatePassword(username,confirmPass.getText().toString(),sqLiteDatabase);
                                if(inserted){
                                    Intent signInScreen = new Intent(resetPassword.getContext(),signInActivity.class);
                                    startActivity(signInScreen);
                                }
                                else
                                    Toast.makeText(changePasswordActivity.this,"Error In Insertion In sqlite db", Toast.LENGTH_SHORT);
                            }
                        }
                    });
                    AlertDialog alertDialog= builder.create();
                    alertDialog.show();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(changePasswordActivity.this,"Error", Toast.LENGTH_SHORT);
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params= new HashMap<String, String>();
                    params.put("username", username);
                    params.put("pass", confirmPass.getText().toString());
                    return params;

                }
            };
            MySingleton.getInstance(changePasswordActivity.this).addToRequestQueue(stringRequest);
        }
        else{
            Toast.makeText(changePasswordActivity.this,"Mismatch Passwords, Type Again", Toast.LENGTH_SHORT);
            confirmPass.setText("");
        }

    }

    public Boolean validatePassword(){
        if(newPass.getText().toString().equals(confirmPass.getText().toString())){
            return true;
        }
        return false;
    }
}
