package com.example.chatroom;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUser_list;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mToolbar=findViewById(R.id.user_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All user");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUser_list=findViewById(R.id.user_list);
        mUser_list.setHasFixedSize(true);
        mUser_list.setLayoutManager(new LinearLayoutManager(this));
        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions< User> options =new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(mUserRef,User.class)
                .build();

        FirebaseRecyclerAdapter<User,userViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<User, userViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull userViewHolder holder, int position, @NonNull User model) {

                holder.mUsername.setText(model.getName());
                holder.mUserStatus.setText(model.getStatus());

                Picasso.with(UserActivity.this).load(model.getThumb_image()).placeholder(R.drawable.default_image).into(holder.mUserImage);

                final String user_id=getRef(position).getKey();
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent toProfile=new Intent(UserActivity.this,ProfileActivity.class);
                        toProfile.putExtra("user_id",user_id);
                        startActivity(toProfile);
                    }
                });
            }

            @NonNull
            @Override
            public userViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
               View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_single_layout,viewGroup,false);
               userViewHolder userViewHolder=new userViewHolder(view);
               return  userViewHolder;
            }
        };
        mUser_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static  class userViewHolder extends RecyclerView.ViewHolder{

        TextView mUsername,mUserStatus;
        CircleImageView mUserImage;
        View mView;
        public userViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            mUsername=itemView.findViewById(R.id.TextView1);
            mUserStatus=itemView.findViewById(R.id.textView2);
            mUserImage=itemView.findViewById(R.id.user_single_image);

        }
    }
}
