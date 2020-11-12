package com.example.lapitchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
//import android.support.v7.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;

    private Toolbar mChatToolbar;
    private DatabaseReference mRootRef;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private StorageReference mImageStorage;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSentBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;

    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    public static final int GALLERY_PICK=1;
    private String mLastKey="";

    private int itemPos=0;

  //  private DatabaseReference mMessageDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

       mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");
      //  getSupportActionBar().setTitle(userName);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
       actionBar.setCustomView(action_bar_view);


        //---------------custom Action bar Title---------

        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);

        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        mChatSentBtn = (ImageButton) findViewById(R.id.chat_sent_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);

        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);

        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);

        loadMessages();

        mTitleView.setText(userName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

               String online = snapshot.child("online").getValue().toString();
              String image = snapshot.child("image").getValue().toString();

                if(online.equals("true")){
                    mLastSeenView.setText("Online");
                }
                else{

                   GetTimeAgo getTimeAgo = new GetTimeAgo();
                   long lastTime= 0;
                  //  long lastTime =  Long.parseLong(online);
                   try {
                        lastTime = Long.parseLong(online);
                   }
                   catch (NumberFormatException e)
                   {
                       Log.i("Message", e.getMessage());
                   }
                   // long lastTime = Long.valueOf(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);
                   // mLastSeenView.setText(online);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.hasChild(mChatUser))
                {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if(error != null)
                            {
                                Log.i("CHAT_LOG", error.getMessage().toString());
                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mChatSentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });

        //-----------------------------------------------last video

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });




        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                itemPos = 0;
             //   messagesList.clear();

                loadMoreMessages();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/"  + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();

            StorageReference filePath = mImageStorage.child("message_images").child(push_id + ".jpg");

            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful())
                    {
                        String download_url = task.getResult().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                                if(error != null)
                                {
                                    Log.d("CHAT_LOG", error.getMessage().toString());

                                }

                            }
                        });



                    }

                }
            });


        }

    }

    private void loadMoreMessages(){

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Messages message = snapshot.getValue(Messages.class);
                messagesList.add(itemPos++ , message);

                if(itemPos == 1)
                {
                    String messageKey = snapshot.getKey();
                    mLastKey = messageKey;
                }



                mAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10,0);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void loadMessages()
    {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Messages message = snapshot.getValue(Messages.class);

                itemPos++;
                if(itemPos == 1)
                {
                    String messageKey = snapshot.getKey();
                    mLastKey = messageKey;
                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() -1);

                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage()
    {
        String message = mChatMessageView.getText().toString();

        if(!TextUtils.isEmpty(message))
        {
            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put( "message" , message );
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);


            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                    if(error != null)
                    {
                        Log.d("CHAT_LOG", error.getMessage().toString());

                    }

                }
            });
        }

    }

}