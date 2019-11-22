package com.example.chatroom;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firestore.admin.v1beta1.Progress;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private TextView mDispname,mDispStatus,mProfileFriendCount;
    private ImageView mProfileImages;
    private Button mprofileSendRequest,mDeclineReq;
    private DatabaseReference mDatabaseReference;
    private ProgressDialog mProgress;
    private DatabaseReference mFriendreqRef;
    private DatabaseReference mFriendlistRef;
    private DatabaseReference mNotificationref;
    private FirebaseUser mAuthUser;
    private String mCurrentState="Not_Friend";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final String uid2= getIntent().getStringExtra("user_id");

        mDispname=findViewById(R.id.profile_dispname);
        mDispStatus=findViewById(R.id.profile_status);
        mProfileFriendCount=findViewById(R.id.profile_FriendsCount);
        mProfileImages=findViewById(R.id.profile_image);
        mprofileSendRequest=findViewById(R.id.profile_send);
        mDeclineReq=findViewById(R.id.profile_decline_request);

        mProgress=new ProgressDialog(ProfileActivity.this);
        mProgress.setTitle("Loading");
        mProgress.setMessage("Please wait while user data is loaded");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(uid2);
        mFriendreqRef=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendlistRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationref=FirebaseDatabase.getInstance().getReference().child("Notification");

        mAuthUser=FirebaseAuth.getInstance().getCurrentUser();

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mDispname.setText(dataSnapshot.child("name").getValue().toString());
                mDispStatus.setText(dataSnapshot.child("status").getValue().toString());
                Picasso.with(ProfileActivity.this).load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.default_image).into(mProfileImages);
                //_____________Friend list_____________________

                mFriendreqRef.child(mAuthUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(uid2))
                        {
                            String req_type=dataSnapshot.child(uid2).child("request_type").getValue().toString();
                            if(req_type.equals("sent")){
                                mCurrentState="req_sent";
                                mprofileSendRequest.setText("CANCEL FRIEND REQUEST");
                                mDeclineReq.setVisibility(View.INVISIBLE);
                                mDeclineReq.setEnabled(false);
                            }
                            else if(req_type.equals("received")) {
                                mCurrentState="req_received";
                                mprofileSendRequest.setText("ACCEPT FRIEND REQUEST");

                                mDeclineReq.setVisibility(View.VISIBLE);
                                mDeclineReq.setEnabled(true);
                            }
                            mProgress.dismiss();
                        }
                        else{
                            mFriendlistRef.child(mAuthUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(uid2)){
                                        mCurrentState="friend";
                                        mprofileSendRequest.setText("UNFRIEND");
                                        mDeclineReq.setVisibility(View.INVISIBLE);
                                        mDeclineReq.setEnabled(false);

                                        mProgress.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }

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

        mprofileSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mprofileSendRequest.setEnabled(false);

                //___________________send request__________________________

                if(mCurrentState.equals("Not_Friend")){

                    mFriendreqRef.child(mAuthUser.getUid()).child(uid2).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFriendreqRef.child(uid2).child(mAuthUser.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String,String> notificationData =new HashMap<>();
                                        notificationData.put("from",mAuthUser.getUid());
                                        notificationData.put("type","request");
                                        mNotificationref.child(uid2).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mCurrentState="req_sent";
                                                mprofileSendRequest.setText("CANCEL FRIEND REQUEST");
                                                mDeclineReq.setVisibility(View.INVISIBLE);
                                                mDeclineReq.setEnabled(false);
                                                // Toast.makeText(ProfileActivity.this,"Request sent successfully",Toast.LENGTH_SHORT ).show();
                                            }
                                        });

                                    }
                                });
                            }
                            else{
                                Toast.makeText(ProfileActivity.this,"Failed Send Request",Toast.LENGTH_SHORT ).show();
                            }
                            mprofileSendRequest.setEnabled(true);
                        }
                    });

                }
                //_________________cancel request_____________________

                if(mCurrentState.equals("req_sent")){
                    mFriendreqRef.child(mAuthUser.getUid()).child(uid2).child("request_sent").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendreqRef.child(uid2).child(mAuthUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mprofileSendRequest.setEnabled(true);
                                    mCurrentState="Not_Friend";
                                    mprofileSendRequest.setText("SEND FRIEND REQUEST");
                                    mDeclineReq.setVisibility(View.INVISIBLE);
                                    mDeclineReq.setEnabled(false);

                                }
                            });
                        }
                    });

                }

                //________________Accept request state

                if(mCurrentState.equals("req_received")){

                    final String currentDate= DateFormat.getDateTimeInstance().format(new Date());
                    mFriendlistRef.child(mAuthUser.getUid()).child(uid2).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendlistRef.child(uid2).child(mAuthUser.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendreqRef.child(mAuthUser.getUid()).child(uid2).child("request_sent").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendreqRef.child(uid2).child(mAuthUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mprofileSendRequest.setEnabled(true);
                                                    mCurrentState="friend";
                                                    mprofileSendRequest.setText("UNFRIEND");
                                                    mDeclineReq.setVisibility(View.INVISIBLE);
                                                    mDeclineReq.setEnabled(false);

                                                }
                                            });
                                        }
                                    });

                                }
                            });
                        }
                    });

                }
            }
        });

    }
}
