package com.example.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText editText_name, editText_email, editText_pass, editText_confirm_pass;
    private Button btn_register, btn_login;
    private ProgressBar register_progressbar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRootRef, defaultUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editText_name = (EditText) findViewById(R.id.editText_register_name);
        editText_email = (EditText) findViewById(R.id.editText_register_email);
        editText_pass = (EditText) findViewById(R.id.editText_register_pass);
        editText_confirm_pass = (EditText) findViewById(R.id.editText_register_confirm_pass);
        btn_register = (Button) findViewById(R.id.btn_register_register);
        btn_login = (Button) findViewById(R.id.btn_register_login);
        register_progressbar = (ProgressBar) findViewById(R.id.register_progressbar);

        mAuth = FirebaseAuth.getInstance();
        usersRootRef = FirebaseDatabase.getInstance().getReference().child("Users");

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = editText_name.getText().toString();
                String email = editText_email.getText().toString();
                String pass = editText_pass.getText().toString();
                String confirm_pass = editText_confirm_pass.getText().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(confirm_pass)){
                    Toast.makeText(RegisterActivity.this, "Please Fill in all fields", Toast.LENGTH_SHORT).show();
                }else if (!pass.equals(confirm_pass)){
                    Toast.makeText(RegisterActivity.this, "Password Mismatch", Toast.LENGTH_SHORT).show();
                }else{
                    register_progressbar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                defaultUserRef = usersRootRef.child(mAuth.getCurrentUser().getUid());
                                defaultUserRef.child("user_name").setValue(name);
                                defaultUserRef.child("user_image").setValue("default_image")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    register_progressbar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(RegisterActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                                                    goToAccountSettings();
                                                }
                                            }
                                        });
                                }else{
                                register_progressbar.setVisibility(View.INVISIBLE);
                                Toast.makeText(RegisterActivity.this, "Error Creating Account", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });
    }

    private void goToAccountSettings() {
        Intent account_settings_intent = new Intent(RegisterActivity.this, AccountSettingsActivity.class);
        startActivity(account_settings_intent);
        finish();
    }

    private void goToLogin() {
        Intent login_intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(login_intent);
        finish();
    }
}
