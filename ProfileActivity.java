package com.example.lapitchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendCount;
    private Button mProfileSendReqBtn,mProfileDecline;

    private DatabaseReference mUserDatabase;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    private FirebaseUser mCurrent_user;

    private String mCurrent_state;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();


        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_DisplayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendCount = (TextView) findViewById(R.id.profile_total_friend);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);
        mProfileDecline = (Button) findViewById(R.id.profile_decline);

        mCurrent_state = "not_friends";

        mProfileDecline.setVisibility(View.INVISIBLE);
        mProfileDecline.setEnabled(false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading user data");
        mProgressDialog.setMessage("Please wait while we load user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.avtar).into(mProfileImage);

                //-------------FRIENDS LIST/REQUEST FEATURE--------------------
                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id))
                        {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received"))
                            {
                             //   mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "req_received";
                                mProfileSendReqBtn.setText("Accept friend request");

                                mProfileDecline.setVisibility(View.INVISIBLE);
                                mProfileDecline.setEnabled(true);


                            }
                            else if(req_type.equals("sent"))
                            {
                                mCurrent_state="req_sent";
                                mProfileSendReqBtn.setText("Cancel friend request");

                                mProfileDecline.setVisibility(View.INVISIBLE);
                                mProfileDecline.setEnabled(false);

                            }

                            mProgressDialog.dismiss();
                        }

                        else
                        {
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if( dataSnapshot.hasChild(user_id))
                                    {
                                       mCurrent_state="friends";
                                       mProfileSendReqBtn.setText("unfriend this person");

                                        mProfileDecline.setVisibility(View.INVISIBLE);
                                        mProfileDecline.setEnabled(false);

                                    }


                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                      //  mProgressDialog.dismiss();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendReqBtn.setEnabled(false);

                //----------------- NOT FRIEND STATE--------------

                if(mCurrent_state.equals("not_friends"))
                {
                    DatabaseReference newNotificationref = mRootRef.child("notification").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String,String> notificationData  = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");



                    Map requestMap = new HashMap();

                        requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + "/request_type", "sent");
                        requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                        requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                        mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {


                                if(error != null){
                                    Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                                }
                                mProfileSendReqBtn.setEnabled(true);

                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("cancel Friend Request");

                            }
                        });

                }

                //--------------------------cancel request state------------------

                if(mCurrent_state.equals("req_sent"))
                {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {


                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("Send friend request");

                                    mProfileDecline.setVisibility(View.INVISIBLE);
                                    mProfileDecline.setEnabled(false);

                                }
                            });
                        }
                    });
                }
                //-----------------------------------REQ RECEIVE STATE--------------------
                if(mCurrent_state.equals("req_received"))
                {
                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                            if(error == null){

                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendReqBtn.setText("Unfriend this persion");

                                mProfileDecline.setVisibility(View.INVISIBLE);
                                mProfileDecline.setEnabled(false);
                            }
                            else
                            {
                                String databaseError = error.getMessage();
                                Toast.makeText(ProfileActivity.this, databaseError,Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }

                //------------------------------------------- UNFRIEND -------------------------------

                if(mCurrent_state.equals("friends"))
                {
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                            if(error == null){

                               // mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "not_friends";
                                mProfileSendReqBtn.setText("Send Friend Request");

                                mProfileDecline.setVisibility(View.INVISIBLE);
                                mProfileDecline.setEnabled(false);

                            }
                            else
                            {
                                String databaseError = error.getMessage();
                                Toast.makeText(ProfileActivity.this, databaseError,Toast.LENGTH_SHORT).show();

                            }
                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });


                }

            }
        });

    }
}