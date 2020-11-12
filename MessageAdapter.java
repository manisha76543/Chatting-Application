package com.example.lapitchat;

import android.graphics.Color;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.SimpleTimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

   private List<Messages> mMessageList;
   private FirebaseAuth mAuth;
   private DatabaseReference mUserDatabase;
    public CircleImageView profileImage;


   public MessageAdapter(List<Messages> mMessageList){

       this.mMessageList = mMessageList;
   }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      //  mAuth = FirebaseAuth.getInstance();

       View v = LayoutInflater.from(parent.getContext())
               .inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

      //  mAuth = FirebaseAuth.getInstance();
    //  String current_user_id = mAuth.getCurrentUser().getUid();

       Messages c = mMessageList.get(position);
       String from_user = c.getFrom();

  /*   if(from_user.equals(current_user_id))
       {

           holder.messageText.setBackgroundColor(Color.WHITE);
           holder.messageText.setTextColor(Color.BLACK);
       }
       else
       {

           holder.messageText.setBackgroundResource(R.drawable.message_text_background);
           holder.messageText.setTextColor(Color.WHITE);
       }*/

       holder.messageText.setText(c.getMessage());
     //  holder.timeText.setText(c.getTime());

/*
        String message_type = c.getType();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = snapshot.child("name").getValue().toString();
                String image = snapshot.child("image").getValue().toString();
                holder.displayName.setText(name);

            //    Picasso.get(holder.profileImage.getContext()).load(image)
              //          .placeholder(R.drawable.avtar).into(holder.profileImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(message_type.equals("text"))
        {

            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        }
        else
        {
            holder.messageText.setVisibility(View.INVISIBLE);

        //    Picasso.get(holder.profileImage.getContext()).load(c.getMessage())
          //          .placeholder(R.drawable.avtar).into(holder.messageImage);


        }

*/

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;

        public ImageView messageImage;
        public TextView displayName;
        public TextView timeDisplay ;

        public Calendar calendar;



        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_layout);
            messageImage = (ImageView) itemView.findViewById(R.id.message_image_layout);
            displayName = (TextView) itemView.findViewById(R.id.name_text_layout);
            timeDisplay = (TextView) itemView.findViewById(R.id.time_text_layout);

            String date = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());


        }
    }




}

