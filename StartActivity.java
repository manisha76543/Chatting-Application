package com.example.lapitchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button nRegBtn;
    private Button nLogInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        nRegBtn = (Button) findViewById(R.id.start_reg_btn);
        nLogInBtn =(Button)findViewById(R.id.start_login_btn);

        nRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent  reg_intent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(reg_intent);
                finish();
            }
        });

        nLogInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent  reg_intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(reg_intent);
                finish();
            }
        });
    }
}