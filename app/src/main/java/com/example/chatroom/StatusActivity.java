package com.example.chatroom;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StatusActivity extends AppCompatActivity {

    private Toolbar mStatusToolbar;
    private Button mStatusBtn;
    private TextInputLayout mStatusText;
    private DatabaseReference mStatusRef;
    private FirebaseUser current_user;
    private ProgressDialog mStatusProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mStatusToolbar=findViewById(R.id.status_app_bar);
        setSupportActionBar(mStatusToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatusProgress=new ProgressDialog(StatusActivity.this);

        String Status_value=getIntent().getStringExtra("Status_value");

        mStatusBtn=findViewById(R.id.status_btn);
        mStatusText=findViewById(R.id.status_text);
        mStatusText.getEditText().setText(Status_value);

        current_user= FirebaseAuth.getInstance().getCurrentUser();
        String uid=current_user.getUid();
        mStatusRef= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusProgress.setTitle("Saving Changes");
                mStatusProgress.setMessage("Please Wait");
                mStatusProgress.show();
                String status=mStatusText.getEditText().getText().toString();
                mStatusRef.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mStatusProgress.dismiss();
                        }
                        else
                        {   mStatusProgress.hide();
                            Toast.makeText(StatusActivity.this,"Error occured",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
