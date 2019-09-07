package com.example.ams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class TeacherActivity extends BaseActivity implements View.OnClickListener{
    FirebaseAuth mAuth;
    private TextView textView;
    private Button logOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        mAuth = FirebaseAuth.getInstance();
        textView = (TextView)findViewById(R.id.emailTextView);
        logOut = (Button) findViewById(R.id.logout);
        textView.setText("Your Email " + mAuth.getCurrentUser().getEmail());
        logOut.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.logout){
            showProgressDialog("Logging You out!!");
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            startActivity(new Intent(TeacherActivity.this, MainActivity.class));
                            finish();
                        }
                    });

        }
    }
}
