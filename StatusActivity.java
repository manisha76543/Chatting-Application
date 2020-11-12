package com.example.lapitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button mSavebtn;
    private TextInputEditText mStatus;

    //Firebase
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    //progress
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid =mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);




        mToolbar = (Toolbar) findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("status_value");


        mStatus = (TextInputEditText) findViewById(R.id.status_input);
        mSavebtn = (Button) findViewById(R.id.status_save_btn);

      //  mStatus.getEditText().setText(status_value);
        mStatus.getText().toString();

        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //progress
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving changes");
                mProgress.setMessage("please wait while we save the changes");
                mProgress.show();


                String status = mStatus.getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            mProgress.dismiss();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "There was some error in saving cganges", Toast.LENGTH_LONG).show();
                        }

                    }
                });

            }
        });

    }
}