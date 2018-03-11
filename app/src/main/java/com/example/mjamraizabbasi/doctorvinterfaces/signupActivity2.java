package com.example.mjamraizabbasi.doctorvinterfaces;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.kofigyan.stateprogressbar.StateProgressBar;

public class signupActivity2 extends Activity {


    Button signin_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signup2);

        final StateProgressBar stateProgressBar = (StateProgressBar) findViewById(R.id.your_state_progress_bar_id);
        stateProgressBar.enableAnimationToCurrentState(true);
        stateProgressBar.setAnimationDuration(3000);
        stateProgressBar.setAnimationStartDelay(250);
        stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
        stateProgressBar.checkStateCompleted(true);

        signin_btn= (Button)findViewById(R.id.signin_btn);
        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInScreen= new Intent(view.getContext(),signInActivity.class);
                startActivity(signInScreen);
            }
        });
    }
}
