package com.example.chatroom;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;
    private Button mLoginBtn;
    private ProgressDialog mProgress;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserdatabaseref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginEmail=findViewById(R.id.login_email_id);
        mLoginPassword=findViewById(R.id.login_password_id);
        mLoginBtn=findViewById(R.id.login_btn);
        mAuth=FirebaseAuth.getInstance();
        mUserdatabaseref=FirebaseDatabase.getInstance().getReference().child("Users");


        mProgress=new ProgressDialog(this);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=mLoginEmail.getEditText().getText().toString().trim();
                String password=mLoginPassword.getEditText().getText().toString().trim();

                if(!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password))
                    mProgress.setTitle("Logging In");
                mProgress.setMessage("Please Wait while we check your credentials");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                loginUser(email,password);
            }
        });

    }
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mProgress.dismiss();
                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {
                            String newToken=instanceIdResult.getToken();
                            Log.e("newToken",newToken);
                            String current_uid=mAuth.getCurrentUser().getUid();
                            mUserdatabaseref.child(current_uid).child("Device_token").setValue(newToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {


                                    Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
                                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();

                                }
                            });


                        }
                    });

                }
                else {
                    mProgress.hide();
                    Toast.makeText(LoginActivity.this,"Error Occured",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}
