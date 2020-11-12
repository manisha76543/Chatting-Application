package com.example.lapitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView mUsersList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = (Toolbar) findViewById(R.id.users_appbar);
        setSupportActionBar(mToolbar);
       getSupportActionBar().setTitle("All users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

      //  setSupportActionBar(mToolbar);

        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));



    }

    @Override
    protected void onStart() {
        super.onStart();





       FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
               Users.class,
               R.layout.users_single_layout,
               UsersViewHolder.class,
               mUsersDatabase
       ) {
           @Override
           protected void populateViewHolder(UsersViewHolder usersViewHolder, Users users, int i) {

               usersViewHolder.setDisplayName(users.getName());
              usersViewHolder.setUserStatus(users.getStatus());
              usersViewHolder.setUserImage(users.getImage(), getApplicationContext());

              final String user_id = getRef(i).getKey();

              usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {

                      Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                      profileIntent.putExtra("user_id", user_id);
                      startActivity(profileIntent);

                  }
              });


           }
       };

       mUsersList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder
    {

        View mView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            mView=itemView;
        }

        //public  void setName(String name)
        public  void setDisplayName(String name)
        {
            TextView mUserNameView = (TextView) mView.findViewById(R.id.user_single_name);
            mUserNameView.setText(name);

        }

        public void setUserStatus(String status)
        {
            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);

        }

        public void setUserImage(String image, Context ctx)
        {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.get().load(image).placeholder(R.drawable.avtar).into(userImageView);
        }


    }

}