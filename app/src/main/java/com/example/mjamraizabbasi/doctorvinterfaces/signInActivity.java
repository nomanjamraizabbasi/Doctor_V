package com.example.mjamraizabbasi.doctorvinterfaces;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
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

import static com.example.mjamraizabbasi.doctorvinterfaces.columns.FIRST_COLUMN;
import static com.example.mjamraizabbasi.doctorvinterfaces.columns.SECOND_COLUMN;

public class signInActivity extends Activity {

    Boolean done;
    Button loginbtn;
    EditText usernameET,passwordET;
    String uname, pass, errorString;
    String server_url="http://doctorv.000webhostapp.com/connect/login.php";
    Context context=this;
    SQLiteDatabase sqLiteDatabase;
    UserDbHelper userDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sign_in);
        loginbtn = (Button) findViewById(R.id.loginBtn);
        usernameET = (EditText) findViewById(R.id.username_login_ET);
        passwordET = (EditText) findViewById(R.id.password_login_ET);

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                boolean checkValidation = validate();
                if (checkValidation) {
                    uname = usernameET.getText().toString();
                    pass = passwordET.getText().toString();

                    //Save username so it can be passed to further activities
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(signInActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("username", uname);
                    editor.apply();

                    userDbHelper = new UserDbHelper(context);
                    sqLiteDatabase = userDbHelper.getReadableDatabase();
                    Cursor cursor = userDbHelper.checkLoginDetails(uname, sqLiteDatabase);

                    if (cursor != null) {
                        //Log.e("Value of get count: ", Integer.toString(cursor.getCount()));
                        if (cursor.moveToFirst()) {
                            String returnedPassword = cursor.getString(cursor.getColumnIndex("password"));

                            //Log.e("Database Operations", returnedPassword);
                            if (returnedPassword.equals(pass)) {
                                Intent homescreen = new Intent(view.getContext(), homepageActivity.class);
                                startActivity(homescreen);
                            } else {
                                Toast.makeText(signInActivity.this, "Password Incorrect", Toast.LENGTH_SHORT).show();
                            }
                        }
                        //if username doesnt exist in sqlite
                        else {
                            //Toast.makeText(signInActivity.this, "Incorrect Username", Toast.LENGTH_SHORT).show();
                            //Check if mysql contains the username
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    //Toast.makeText(signInActivity.this, "after responce", Toast.LENGTH_SHORT).show();
                                    try {
                                        JSONArray jsonArray = new JSONArray(response);
                                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                                        String code = jsonObject.getString("code");

                                        //if mysql also doesnt have the username and user password
                                        if (code.equals("login_failed")) {
                                            Toast.makeText(signInActivity.this, "Wrong User Details", Toast.LENGTH_SHORT).show();
                                        }
                                        //if mysql contains the username
                                        else {
                                            //insert the username and password in the sqlite
                                            userDbHelper= new UserDbHelper(context);
                                            sqLiteDatabase= userDbHelper.getWritableDatabase();
                                            userDbHelper.add_newuser(uname,pass,sqLiteDatabase);
                                            Intent homescreen = new Intent(view.getContext(), homepageActivity.class);
                                            startActivity(homescreen);
                                        }
//
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                    Toast.makeText(signInActivity.this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                                }
                            }) {

                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("username", uname);
                                    params.put("password", pass);

                                    //Toast.makeText(signInActivity.this, "In getParams", Toast.LENGTH_SHORT).show();
                                    return params;

                                }

                            };
                            MySingleton.getInstance(signInActivity.this).addToRequestQueue(stringRequest);

                        }


                    }

                }

                //If fields are not validated
                else {
                    Toast.makeText(signInActivity.this, "Input Username/Password", Toast.LENGTH_SHORT).show();
                }


//                    StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url, new Response.Listener<String>() {
//                        @Override
//                        public void onResponse(String response) {
//                            //Toast.makeText(signInActivity.this, "after responce", Toast.LENGTH_SHORT).show();
//                            try {
//                                JSONArray jsonArray = new JSONArray(response);
//                                JSONObject jsonObject = jsonArray.getJSONObject(0);
//                                String code = jsonObject.getString("code");
//                                if (code.equals("login_failed")) {
//                                    Toast.makeText(signInActivity.this, code, Toast.LENGTH_SHORT).show();
//                                } else {
//                                    Intent homescreen = new Intent(view.getContext(), homepageActivity.class);
//                                    startActivity(homescreen);
//                                }
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//                    }, new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//
//                            Toast.makeText(signInActivity.this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
//                        }
//                    }) {
//
//                        @Override
//                        protected Map<String, String> getParams() throws AuthFailureError {
//                            Map<String, String> params = new HashMap<String, String>();
//                            params.put("username", uname);
//                            params.put("password", pass);
//
//                            //Toast.makeText(signInActivity.this, "In getParams", Toast.LENGTH_SHORT).show();
//                            return params;
//
//                        }
//
//                    };
//                    MySingleton.getInstance(signInActivity.this).addToRequestQueue(stringRequest);
//                } else {
//                    Toast.makeText(signInActivity.this, errorString, Toast.LENGTH_LONG).show();
//                }
//
//            }
////            else {
////                    Toast.makeText(signInActivity.this, "Input Username/Password", Toast.LENGTH_SHORT).show();
////                }
                }
        });
    }
//        });
//    }


    public boolean validate(){
        done= true;
        errorString="";
        if(usernameET.getText().toString().equals("")){
            errorString= errorString+ "Insert an Username\n";
            done=false;
        }

        if(passwordET.getText().toString().equals("")){
            errorString= errorString+ "Insert password\n";
            done=false;
        }

        return done;
    }
}
