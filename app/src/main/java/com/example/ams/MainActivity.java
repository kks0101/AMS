package com.example.ams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends BaseActivity implements View.OnClickListener{
    private EditText emailField, passwordField;
    Button loginButton, studentSignUpButton, teacherSignUpButton;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailField = (EditText) findViewById(R.id.email);
        passwordField = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.loginButton);
        studentSignUpButton = (Button) findViewById(R.id.studentSignUPButton);
        teacherSignUpButton = (Button) findViewById(R.id.teacherSignUpButton);

        mAuth = FirebaseAuth.getInstance();
        loginButton.setOnClickListener(this);
        studentSignUpButton.setOnClickListener(this);
        teacherSignUpButton.setOnClickListener(this);
    }

    private void signIn(String email, String password){
        if(!validateForm()){
            return;
        }
        showProgressDialog("Logging You in...");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Intent intent = new Intent(getApplicationContext(), StudentActivity.class);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Authentication Field", Toast.LENGTH_LONG).show();
                        }
                        hideProgressDialog();
                    }
                });
    }
    public boolean validateForm(){

        boolean valid = true;
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        if(TextUtils.isEmpty(email)){
            emailField.setError("Required!");
            valid = false;
        }
        else
            emailField.setError(null);
        if(TextUtils.isEmpty(password)){
            passwordField.setError("Required!");
            valid = false;
        }
        else
            passwordField.setError(null);
        return valid;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.loginButton){
            //login
            signIn(emailField.getText().toString(), passwordField.getText().toString());
        }
        else if (view.getId() == R.id.studentSignUPButton){
            //create an Intent to signup Activity
            Intent intent = new Intent(getApplicationContext(), StudentRegister.class);
            startActivity(intent);
        }
        else if (view.getId() == R.id.teacherSignUpButton){
            Intent intent = new Intent(getApplicationContext(), TeacherRegister.class);
            startActivity(intent);
        }
    }
}
