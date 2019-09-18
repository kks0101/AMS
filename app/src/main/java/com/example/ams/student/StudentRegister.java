package com.example.ams.student;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ams.others.AppStatus;
import com.example.ams.others.BaseActivity;
import com.example.ams.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class StudentRegister extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, ActivityCompat.OnRequestPermissionsResultCallback{
    private EditText studentNameField, studentRegNo, studentEmailField, studentSemesterField , studentPhoneNoField, studentPassword, studentRetypePassword;
    private Spinner branchSpinner, groupSpinner;
    private FirebaseAuth mAuth;
    private Button studentRegister;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS= 1;
    private final String BASE_URL = "https://amscollege.000webhostapp.com/";
    //private final String BASE_URL = "http://192.168.43.99:1234/ams/";
    private static final String TAG_SUCCESS = "success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);
        //initializing each field
        studentEmailField = (EditText) findViewById(R.id.studentEmailField);
        studentNameField = (EditText) findViewById(R.id.studentNameField);
        studentRegNo = (EditText)findViewById(R.id.studentRegNo);
        studentSemesterField = (EditText)findViewById(R.id.studentSemesterField);
        studentPhoneNoField = (EditText) findViewById(R.id.studentPhoneNoField);
        studentPassword = (EditText)findViewById(R.id.passwordField);
        studentRetypePassword = (EditText)findViewById(R.id.retypePassword);

        branchSpinner = (Spinner)findViewById(R.id.branchSpinner);
        groupSpinner = (Spinner) findViewById(R.id.groupSpinner);
        studentRegister = (Button) findViewById(R.id.studentRegister);

        mAuth = FirebaseAuth.getInstance();
        ///check for permission
        if (ContextCompat.checkSelfPermission( StudentRegister.this, Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED )
        {

            ActivityCompat.requestPermissions(StudentRegister.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        }

        //creating adapter for branch Spinner
        String array[] = getApplicationContext().getResources().getStringArray(R.array.available_branch);

        ArrayAdapter<String> branchSpinnerArrayAdapter = new ArrayAdapter<String>(
                this,android.R.layout.simple_spinner_item,array){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.WHITE);
                }
                return view;
            }
        };

        branchSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        branchSpinner.setAdapter(branchSpinnerArrayAdapter);

        String groupArray[] = getApplicationContext().getResources().getStringArray(R.array.groups);

        ArrayAdapter<String> groupSpinnerArrayAdapter = new ArrayAdapter<String>(
                this,android.R.layout.simple_spinner_item,groupArray){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.WHITE);
                }
                return view;
            }
        };

        groupSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(groupSpinnerArrayAdapter);

        studentRegister.setOnClickListener(this);
        branchSpinner.setOnItemSelectedListener(this);
        groupSpinner.setOnItemSelectedListener(this);
    }

    private void createAccount(String email, String password){
        if(!validateForm()){
            return;
        }
        showProgressDialog("Registering..Please wait");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            //Account created successfully
                            //store rest of the fields in relational database
                            //Toast.makeText(StudentRegister.this, "Registered Successfully!!", Toast.LENGTH_LONG).show();
                            //Intent intent = new Intent(getApplicationContext(), TeacherActivity.class);

                            CreateNewStudent createNewStudent = new CreateNewStudent();
                            createNewStudent.execute();
                            // startActivity(intent);
                        }
                        else{
                            Toast.makeText(StudentRegister.this, "Account Authentication failed!!", Toast.LENGTH_LONG).show();
                            Log.e("Error Registering", task.getException().toString());

                        }
                        hideProgressDialog();
                    }
                });
    }

    private boolean validateForm(){
        boolean valid = true;
        String name = studentNameField.getText().toString();
        String regNo = studentRegNo.getText().toString();
        String email = studentEmailField.getText().toString();
        String phnNo = studentPhoneNoField.getText().toString();
        String password = studentPassword.getText().toString();
        String retypePassword = studentRetypePassword.getText().toString();
        String semester = studentSemesterField.getText().toString();

        if(TextUtils.isEmpty(name)){
            studentNameField.setError("Required!");
            valid = false;
        }
        else if(TextUtils.isEmpty(regNo)){
            studentRegNo.setError("Required!");
            valid = false;
        }
        else if(TextUtils.isEmpty(email)){
            studentEmailField.setError("Required!");
            valid = false;
        }
        else if (TextUtils.isEmpty(phnNo)){
            studentPhoneNoField.setError("Required!");
            valid = false;
        }
        else if (TextUtils.isEmpty(password)){
            studentPassword.setError("Required!");
            valid = false;
        }
        else if (TextUtils.isEmpty((retypePassword))){
            studentRetypePassword.setError("Required!");
            valid = false;
        }
        else{
            if(!password.equals(retypePassword)){
                studentRetypePassword.setError("Password dindn't match");
                valid = false;
            }
        }
        if(!regNo.matches("[0-9]+") || regNo.length()!=8){
            studentRegNo.setError("Should only be 8 digits!!");
            valid= false;
        }
        if(!semester.matches("[0-9]+")){
            studentSemesterField.setError("Invalid!!");
            valid= false;
        }
        if(semester.matches("[0-9]+") && Integer.parseInt(semester) <1 && Integer.parseInt(semester) >8  ){
            studentSemesterField.setError("Only between 1 and 8");
            valid = false;
        }
        //registration number should only contain integers

        if (ContextCompat.checkSelfPermission( StudentRegister.this, Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED ){
            valid = false;
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user!=null)
                user.delete();
            Toast.makeText(this, "You should provide permission!! ", Toast.LENGTH_LONG).show();
        }

        return valid;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }

        }
    }

    //class to create new Student user
    private class CreateNewStudent extends AsyncTask<String, String , String > {
        String name, email, phnNo, deviceId, userId, branch, groupName;
        int regNo, semester;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Updating..please wait..");
            name = studentNameField.getText().toString();
            regNo = Integer.parseInt(studentRegNo.getText().toString());
            email = studentEmailField.getText().toString();
            phnNo = studentPhoneNoField.getText().toString();
            semester = Integer.parseInt(studentSemesterField.getText().toString());
            branch = branchSpinner.getSelectedItem().toString();
            groupName = groupSpinner.getSelectedItem().toString();
            if (mAuth.getCurrentUser() != null)
                userId = mAuth.getCurrentUser().getUid();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            try {
                //uniquely identifies phone
                deviceId = telephonyManager.getDeviceId();
            } catch (SecurityException e) {
                Log.i("PERMISSION", e.toString());
            }
        }


        @Override
        protected String doInBackground(String... strings) {
            ///ContentValues params = new ContentValues();

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("name", name);
            params.put("regNo", regNo);
            params.put("emailId", email);
            params.put("phoneNo", phnNo);
            params.put("deviceId", deviceId);
            params.put("userId", userId);
            params.put("branch", branch);
            params.put("semester", semester);
            params.put("groupName", groupName);

            String link = BASE_URL + "create_student.php";

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
                Toast.makeText(StudentRegister.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null)
                    user.delete();
            } else {

                //otherwise string would contain the JSON returned from php
                    JSONParser parser = new JSONParser();

                    try {
                        JSONObject jsonObject = (JSONObject) parser.parse(s);



                        Object p = jsonObject.get("success");
                        int successCode = Integer.parseInt(p.toString());

                    if (successCode == 0) {
                        Toast.makeText(StudentRegister.this, "Some error occurred", Toast.LENGTH_LONG).show();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null)
                            user.delete();
                    }
                    else {
                        if (s.toLowerCase().contains("duplicate")) {

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null)
                                user.delete();
                            //if data is Not updated to mysql server
                            Toast.makeText(StudentRegister.this, "Student Reg No already exists", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(StudentRegister.this, "Registered Successfully!!", Toast.LENGTH_LONG).show();
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("details", 0); //Mode_private
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("user", "student");
                            editor.apply();
                            Intent intent = new Intent(getApplicationContext(), StudentActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void onClick(View view) {
        if (!AppStatus.getInstance(this).isOnline()) {
            Toast.makeText(this,"You are not online!!!!",Toast.LENGTH_LONG).show();
            return;
        }

        if (view.getId() == R.id.studentRegister) {
            createAccount(studentEmailField.getText().toString(), studentPassword.getText().toString());
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(view.getId() == R.id.branchSpinner)
            branchSpinner = (Spinner) adapterView.getItemAtPosition(i);
        if(view.getId() == R.id.groupSpinner)
            groupSpinner = (Spinner) adapterView.getItemAtPosition(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}
