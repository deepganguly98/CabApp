package com.cabapp.deep;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverRegisterActivity extends AppCompatActivity {
    private EditText mEmail, mPassword;
    //private Button mRegister;
    private FirebaseAuth mAuth;
    private String email="",password="";
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEmail = (EditText) findViewById(R.id.editText);
        mPassword = (EditText) findViewById(R.id.editText2);



        mAuth=FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(DriverRegisterActivity.this, DriverMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;

                }
            }
        };


        FloatingActionButton mRegister = (FloatingActionButton) findViewById(R.id.fab);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                email = mEmail.getText().toString();
                password = mPassword.getText().toString();
                if( (email.equalsIgnoreCase("")) || (password.equalsIgnoreCase(""))){
                    Toast.makeText(DriverRegisterActivity.this, "please fill in details", Toast.LENGTH_SHORT).show();

                }
                else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(DriverRegisterActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                            } else {
                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(user_id);
                                current_user_db.setValue(true);
                                Intent intent = new Intent(DriverRegisterActivity.this, CustomerMapActivity.class);
                                startActivity(intent);
                                finish();
                                return;
                            }
                        }


                    });
                }
            }
        });




    }
    @Override
    public void onBackPressed(){

        Intent intent = new Intent(DriverRegisterActivity.this,DriverLoginActivity.class);
        startActivity(intent);
        finish();
        return;
    }

}
