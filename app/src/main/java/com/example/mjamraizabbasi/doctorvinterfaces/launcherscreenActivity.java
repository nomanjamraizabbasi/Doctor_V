package com.example.mjamraizabbasi.doctorvinterfaces;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class launcherscreenActivity extends Activity {

    TextView signup_link;
    Button signinBTN, quickdiagnosisBTN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_launcherscreen);

        signup_link= (TextView)findViewById(R.id.signUpET);
        signinBTN=(Button)findViewById(R.id.launcherloginBtn);
        quickdiagnosisBTN = (Button)findViewById(R.id.diagnosisBtn);

        signup_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpScreen = new Intent(view.getContext(),singupActivity.class);
                startActivity(signUpScreen);
            }
        });

        signinBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signinScreen = new Intent(view.getContext(),signInActivity.class);
                startActivity(signinScreen);
            }
        });

        quickdiagnosisBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent diagnosisScreen = new Intent(view.getContext(),loggedOutDiagnosis.class);
                startActivity(diagnosisScreen);
            }
        });
    }
}
