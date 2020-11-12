package com.example.lapitchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
* Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
 /*   private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
*/
   // public FriendsFragment() {
        // Required empty public constructor
    //}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
  //   * @param param1 Parameter 1.
    // * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
 /*   public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_friends, container, false);

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;

    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(

                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendsDatabase

        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, final Friends friends, int i) {

                friendsViewHolder.setDate(friends.getDate());
            //    friendsViewHolder.setUserImage(friends.getImage(), getContext();


               final String list_user_id = getRef(i).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        final String userName = snapshot.child("name").getValue().toString();
                        final   String image = snapshot.child("image").getValue().toString();
                    //    String userOnline = snapshot.child("online").getValue().toString();

                        if(snapshot.hasChild("online")){

                         // Boolean userOnline = (Boolean) snapshot.child("online").getValue();
                           String userOnline = snapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnline(userOnline);

                        }

                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setUserImage(image, getContext());

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};

                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Option");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //click Event for each item
                                        if(which == 0){

                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);

                                        }
                                        else if(which == 1){
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", userName);

                                            startActivity(chatIntent);


                                        }


                                    }
                                });
                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        };

        mFriendsList.setAdapter(friendsRecyclerViewAdapter);

    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView){

            super(itemView);
            mView = itemView;

        }

        public void setDate(String date)
        {
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_status);
            userNameView.setText(date);

        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String image, Context ctx)
        {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.get().load(image).placeholder(R.drawable.avtar).into(userImageView);
        }


        public void setUserOnline(String online_status){

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            }
            else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }

        }

    }
}