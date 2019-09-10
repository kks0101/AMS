package com.example.ams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends BaseActivity implements View.OnClickListener{
    FirebaseAuth mAuth;
    private TextView textView;
    private Button logOut;
    private ListView teacherListView;
    FirebaseDatabase mDatabase;
    ArrayList<String> stringArrayList = new ArrayList<>();
    List<TeacherSubjectDetail> recievedList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        mAuth = FirebaseAuth.getInstance();
        textView = (TextView)findViewById(R.id.emailTextView);
        logOut = (Button) findViewById(R.id.logout);
        final LinearLayout linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        teacherListView = (ListView) findViewById(R.id.teacherListView);
        textView.setText("Your Email " + mAuth.getCurrentUser().getEmail());
        logOut.setOnClickListener(this);


        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        String userid=user.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("teachers");


        reference.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                linlaHeaderProgress.setVisibility(View.GONE);

                GenericTypeIndicator<List<TeacherSubjectDetail>> t = new GenericTypeIndicator<List<TeacherSubjectDetail>>() {};
                 recievedList = dataSnapshot.getValue(t);
                stringArrayList = new ArrayList<>();

                if(recievedList!=null) {
                    for (TeacherSubjectDetail tsd : recievedList) {
                        stringArrayList.add(tsd.getSubjectCode() + " -- " + tsd.getBranch());
                    }
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, stringArrayList);
                teacherListView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        teacherListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getApplicationContext(), stringArrayList.get(i), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(TeacherActivity.this, TeacherTakeAttendance.class);
                intent.putExtra("TeacherSubjectDetail", recievedList.get(i));
                startActivity(intent);

            }
        });
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
