package com.example.mjamraizabbasi.doctorvinterfaces;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import com.kofigyan.stateprogressbar.StateProgressBar;

import java.util.HashMap;
import java.util.Map;

public class signupActivity1 extends Activity {

    Boolean done;
    Button create_accountbtn;
    EditText email, username, password1, password2;
    String errorString, emailString, usernameString, passwordString, password2string, contactNo, fullname, location, dob;
    String server_url= "http://doctorv.000webhostapp.com/connect/save_userdata.php";
    AlertDialog.Builder builder;
    Context context=this;
    UserDbHelper userDbHelper;
    SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signup1);

        //Get values from the first sign up activity and save in this activity's variables
        fullname = getIntent().getStringExtra("fname");
        contactNo = getIntent().getStringExtra("contact");
        location = getIntent().getStringExtra("location");
        dob = getIntent().getStringExtra("dob");
        //Toast.makeText(signupActivity1.this,fullname+", "+contactNo+", "+location+", "+dob,Toast.LENGTH_LONG);
        create_accountbtn = (Button) findViewById(R.id.createaccount_btn);

        final StateProgressBar stateProgressBar = (StateProgressBar) findViewById(R.id.your_state_progress_bar_id);
        stateProgressBar.enableAnimationToCurrentState(true);
        stateProgressBar.setAnimationDuration(3000);
        stateProgressBar.setAnimationStartDelay(250);
        stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
        stateProgressBar.checkStateCompleted(true);

        username = (EditText) findViewById(R.id.username_ET);
        email = (EditText) findViewById(R.id.emailET);
        password1 = (EditText) findViewById(R.id.password_login_ET);
        password2 = (EditText) findViewById(R.id.retypepasswordET);

        builder = new AlertDialog.Builder(signupActivity1.this);

        create_accountbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                boolean checkvalidation = validate();

                if (checkvalidation) {
                    save_info();
                    userDbHelper= new UserDbHelper(context);
                    sqLiteDatabase= userDbHelper.getWritableDatabase();
                    userDbHelper.add_newuser(usernameString,password2string,sqLiteDatabase);

                    StringRequest stringRequest= new StringRequest(Request.Method.POST, server_url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {

                            builder.setTitle("Information Sent");
                            builder.setMessage("Your information is being " + response);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if(response.equals("inserted")){
                                        Intent signupScreen2 = new Intent(view.getContext(),signupActivity2.class);
                                        startActivity(signupScreen2);
                                    }
                                    else if(response.equals("Username Already Exists")) {
                                        Toast.makeText(signupActivity1.this, "Username Already Exists, Choose Another One", Toast.LENGTH_SHORT);
                                        username.setText("");
                                        password1.setText("");
                                        password2.setText("");
                                    }


                                }
                            });
                            AlertDialog alertDialog= builder.create();
                            alertDialog.show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(signupActivity1.this, "Error", Toast.LENGTH_SHORT);
                        }
                    }){

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String,String> params= new HashMap<String, String>();
                            params.put("username", usernameString);
                            params.put("pass", password2string);
                            params.put("fullname", fullname);
                            params.put("location", location);
                            params.put("contact", contactNo);
                            params.put("dob", dob);
                            params.put("email", emailString);
                            return params;

                        }
                    };
                    MySingleton.getInstance(signupActivity1.this).addToRequestQueue(stringRequest);

                }
                else
                    return;

            }
        });
    }

    public boolean validate(){
        done= true;
        errorString="";
        if(username.getText().toString().equals("")){
            errorString= errorString+ "Insert an Username\n";
            done=false;
        }

        if(email.getText().toString().equals("")){
            errorString= errorString+ "Insert an Email Address\n";
            done=false;
        }

        if(password1.getText().toString().equals("")){
            errorString= errorString+ "Insert password\n";
            done=false;
        }
        if(password2.getText().toString().equals("")){
            errorString= errorString+ "Insert password again\n";
            done=false;
        }
        return done;
    }

    public void save_info(){
        emailString= email.getText().toString();
        usernameString= username.getText().toString();
        passwordString= password1.getText().toString();
        password2string= password2.getText().toString();
        if(!passwordString.equals(password2string)) {
            Toast.makeText(signupActivity1.this, "Passwords Mismatch!", Toast.LENGTH_SHORT).show();
            password1.setText("");
            password2.setText("");
        }

    }
}
