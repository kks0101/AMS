package com.example.ams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TeacherSubject extends BaseActivity {
    private Spinner branchSpinner, subjectSpinner;
    private List<String> subjectList;
    private ListView listView;
    List<String> selectedSubjectList = new ArrayList<>();
    //to get the list of objects of TeacherSubjectDetail to be used in Firebase realtime db
    ArrayList<TeacherSubjectDetail> teacherSubjectDetailsList = new ArrayList<>();
    private Button addSubject, adddSubjectsToDb;
    private TeacherSubjectAdapter teacherSubjectAdapter;
    private DatabaseReference mDatabase;
    private FirebaseDatabase firebaseDatabase;
    private final String BASE_URL = "http://192.168.43.99:1234/ams/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_subject);

        branchSpinner = (Spinner)findViewById(R.id.branch);
        subjectSpinner = (Spinner) findViewById(R.id.subjectSpinner);
        listView = (ListView) findViewById(R.id.listView);
        addSubject = (Button)findViewById(R.id.addSubject);
        adddSubjectsToDb = (Button) findViewById(R.id.addSubjectsToDb);
        //to store list of subjects and grouups selected
        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference("teachers");
        teacherSubjectAdapter = new TeacherSubjectAdapter(getApplicationContext(), teacherSubjectDetailsList);
        listView.setAdapter(teacherSubjectAdapter);



        ArrayAdapter<CharSequence> branchAdapter = ArrayAdapter.createFromResource(this,
                R.array.available_branch, android.R.layout.simple_spinner_item);
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        branchSpinner.setAdapter(branchAdapter);
        //function to get the subjects of the selected branch from the spinner
        //searchForSubject(branchSpinner.getSelectedItem().toString());

        //creating adapter for group spinner
        subjectList = new ArrayList<>();
        /*//list to contain subjects of specific branch fetched from server
        ArrayAdapter<String> subjectSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, subjectList);

        subjectSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectSpinnerAdapter);*/

        branchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                subjectList.clear();
                String branch =  adapterView.getItemAtPosition(i).toString();
                //Toast.makeText(TeacherSubject.this, branch, Toast.LENGTH_LONG).show();
                searchForSubject(branch);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(TeacherSubject.this, subjectSpinner.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




        //add onClickListener on add button
        addSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String branch = branchSpinner.getSelectedItem().toString();
                String subjectCode = subjectSpinner.getSelectedItem().toString();
                TeacherSubjectDetail teacherSubjectDetail = new TeacherSubjectDetail(subjectCode, branch);
                if(!teacherSubjectDetailsList.contains(teacherSubjectDetail)) {
                    teacherSubjectDetailsList.add(teacherSubjectDetail);
                    selectedSubjectList.add(branch + " -- " + subjectCode);
                    teacherSubjectAdapter.notifyDataSetChanged();
                }
            }
        });

        adddSubjectsToDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //to ensure that Teacher do provide subjects
                if(teacherSubjectDetailsList==null){
                    Toast.makeText(getApplicationContext(), "Please enter subjects", Toast.LENGTH_LONG).show();
                }
                else {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    //if user is currently logged in
                    if (user != null) {
                        String userId = user.getUid();

                        mDatabase.child(userId).setValue(teacherSubjectDetailsList);
                        Intent intent = new Intent(TeacherSubject.this, TeacherActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Toast.makeText(TeacherSubject.this, "Data Updated", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void searchForSubject(String branch){
        GetSubjectDetails getSubjectDetails = new GetSubjectDetails();
        getSubjectDetails.execute(branch);
    }

    private class GetSubjectDetails extends AsyncTask<String, String , String > {
        String branch;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            showProgressDialog("Updating..please wait..");
            branch = branchSpinner.getSelectedItem().toString();

        }


        @Override
        protected String doInBackground(String... strings) {
            ///ContentValues params = new ContentValues();

            HashMap<String, Object> params = new HashMap<String, Object>();
            String branch_table_name = "subject_" + strings[0].toLowerCase();
            params.put("branch_table_name", branch_table_name.trim());

            String link = BASE_URL + "get_subject_details.php";

            Set set = params.entrySet();
            Iterator iterator = set.iterator();
            try {
                String data = "";
                while (iterator.hasNext()) {
                    Map.Entry mEntry = (Map.Entry) iterator.next();
                    data += URLEncoder.encode(mEntry.getKey().toString(), "UTF-8") + "=" +
                            URLEncoder.encode(mEntry.getValue().toString(), "UTF-8");
                    data += "&";
                }

                if (data != null && data.length() > 0 && data.charAt(data.length() - 1) == '&') {
                    data = data.substring(0, data.length() - 1);
                }
                Log.d("Debug", data);

                URL url = new URL(link);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Accept", "*/*");
                OutputStream out = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

                Log.d("data", data);
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());


                String result = convertStreamToString(inputStream);
                httpURLConnection.disconnect();
                Log.d("TAG", result);
                //teacherId is defined in sql as primary key
                //so if any user login with the same teacherId, delete this already created user in Firebase


                return result;
            } catch (Exception e) {
                Log.d("debug", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        private String convertStreamToString(InputStream inputStream) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder("");
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            hideProgressDialog();
            //if string returned from doinbackground is null, that means Exception occured while connectioon to server
            if (s == null) {
                Toast.makeText(TeacherSubject.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
            } else {

                //otherwise string would contain the JSON returned from php
                JSONParser parser = new JSONParser();
                JSONArray  jsonArray = null;
                try {
                    //jsonObject = (JSONObject) parser.parse(s);
                    jsonArray = (JSONArray)parser.parse(s);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //List<String> subjects = new ArrayList<>();
                if(jsonArray==null)
                    Toast.makeText(TeacherSubject.this, "ARRAY empty !", Toast.LENGTH_LONG).show();
                if(jsonArray!=null) {
                    for (Object json : jsonArray) {
                        subjectList.add(json.toString());
                    }
                }
                //list to contain subjects of specific branch fetched from server
                ArrayAdapter<String> subjectSpinnerAdapter = new ArrayAdapter<String>(TeacherSubject.this,
                        android.R.layout.simple_spinner_item, subjectList);

                subjectSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subjectSpinner.setAdapter(subjectSpinnerAdapter);
                Log.d("SUB", subjectList.toString());
            }
        }
    }
}
