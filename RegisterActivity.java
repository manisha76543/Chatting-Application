package com.example.lapitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "";
    private TextInputEditText mDisplayName;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private Button mCreateBtn;

    private DatabaseReference mUserDatabase;
    private Toolbar mToolbar;

    private DatabaseReference mDatabase;

    //Progress
   private ProgressDialog mRegProgress;

    //Firebase Auth
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolbar Set
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       mRegProgress = new ProgressDialog(this);

       mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


//firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDisplayName = (TextInputEditText) findViewById(R.id.reg_display_name);
        mEmail = (TextInputEditText) findViewById(R.id.reg_email);
        mPassword = (TextInputEditText) findViewById(R.id.reg_password);
        mCreateBtn = (Button) findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name = mDisplayName.getText().toString();
                 String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if(!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password))
                {
                    mRegProgress.setTitle("Registering user");
                    mRegProgress.setMessage("Please wait while we create your account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    register_user(display_name,email,password);

                }
         /*       Log.i("name",display_name);
                Log.i("email",email);
                Log.i("pass",password);*/

            }
        });

    }

    private void register_user(final String display_name, final String email, final String password) {
        Log.i("inside","Inside register user");
        Log.i("name",display_name);
        Log.i("email",email);
        Log.i("pass",password);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    String DeviceToken = FirebaseInstanceId.getInstance().getToken();


                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", display_name);
                    userMap.put("status", "Hi there i'm using lapit chat app");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", DeviceToken);

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                mRegProgress.dismiss();

                             //   String current_user_id = mAuth.getCurrentUser().getUid();
                             //   String DeviceToken = FirebaseInstanceId.getInstance().getToken();

                              //  mUserDatabase.child(current_user_id).child("device_token").setValue(DeviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                               //     @Override
                                 //   public void onSuccess(Void aVoid) {

                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();


                                    }
                           //     });


                         //   }

                        }
                    });






                }
                else
                {
                    Log.w("error", "createUserWithEmail:failure", task.getException());
                    mRegProgress.hide();

                    Toast.makeText(RegisterActivity.this, "Can not sign in. Please check the form and try again ", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}