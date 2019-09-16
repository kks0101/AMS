package com.example.ams;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {
    private Button verifyTeacher, logOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        verifyTeacher = (Button) findViewById(R.id.verifyTeacher);
        logOut = (Button)findViewById(R.id.logoutAdmin);

        verifyTeacher.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, VerifyTeacherActivity.class);
                startActivity(intent);
            }
        });


        logOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences pref = getApplicationContext().getSharedPreferences("details", 0); //Mode_private
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("user", null);
                editor.commit();
                Intent intent  = new Intent(AdminActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
