package com.example.ams;

import androidx.annotation.NonNull;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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


public class MainActivity extends BaseActivity implements View.OnClickListener{


    private EditText emailField, passwordField;
    private Button loginButton, studentSignUpButton, teacherSignUpButton;
    private FirebaseAuth mAuth;
    private final String BASE_URL = "https://amscollege.000webhostapp.com/";
    //private final String BASE_URL = "http://192.168.43.99:1234/ams/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //User login persistence code -- uses SharedPreferences to keep track of which user is logged in
        //direct the intent as well as persist the login activity
        SharedPreferences pref = getApplicationContext().getSharedPreferences("details", 0); //Mode_private
        if(pref!=null){
            String userType = pref.getString("user", null);
            if(userType != null){

                if(userType.equals("student")){

                    Intent intent = new Intent(MainActivity.this, StudentActivity.class);
                    startActivity(intent);
                    finish();

                }
                else if(userType.equals("teacher")){
                    checkIfSubjectProvided();
                }
                else if (userType.equals("admin")){
                    Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }

        //initializing the containers
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


    /* Sign In function which first validates the entry and then uses Firebase sign in with email password
    * method to authenticate user. Once the user is authenticated by Firebase Service we are creating the teacher
    * entry in mysql table stores on PHP server*/


    private void signIn(String email, String password){

        //check for validation of the form
        if(!validateForm()){
            return;
        }

        showProgressDialog("Logging You in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if(task.isSuccessful()){

                            Log.d("Debug", "Successfully Logged in");
                            /*providing admin interface. Basically admin login is done using email and password
                            provided by server administrator*/

                            if(emailField.getText().toString().equals("admin@gmail.com")){

                                /*Shared Preference used in order to persist admin login*/
                                SharedPreferences pref = getApplicationContext().getSharedPreferences("details", 0); //Mode_private
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("user", "admin");
                                editor.apply();

                                /*This code is part of sending notification : It stores the token generated for admin
                                * on Firebase Realtime Database. This token uniquely identifies the device on which admin
                                * is currently logged in*/
                                FirebaseInstanceId.getInstance().getInstanceId()
                                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                if (!task.isSuccessful()) {
                                                    Log.w("TAG", "getInstanceId failed", task.getException());
                                                    return;
                                                }
                                                String token = task.getResult().getToken();
                                                Log.d("TAG", token);

                                                //Getting reference to database to store the token
                                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                                                mDatabase.child("admin").setValue(token);
                                            }
                                        });

                                //Once the admin is logged in , direct to main Admin Activity
                                Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else{
                                //This creates an object of SearchUser() class which extends Async Task to check if the user which is currently logging in
                                //as teacher or student. Depending on the result from the server, we will direct to the respective activity
                                SearchUser searchUser = new SearchUser();
                                searchUser.execute();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Authentication Field", Toast.LENGTH_LONG).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    /* This function checks if subject field is provided by User or not*/
    private void checkIfSubjectProvided(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("teachers");
        reference.addValueEventListener(new ValueEventListener() {
            boolean valid = false;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                //fetch data from Firebase database corresponding to current user
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    Log.d("info", childSnapshot.toString() + " // " + userId);
                    if(childSnapshot.getKey().equals(userId)){
                        valid = true;
                        break;
                    }
                }
                //if not fount
                if(!valid){
                    Intent intent = new Intent(MainActivity.this, TeacherSubject.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(MainActivity.this, TeacherActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        if (!AppStatus.getInstance(this).isOnline()) {

            Toast.makeText(this,"You are not online!!!!",Toast.LENGTH_LONG).show();
            return;
        }
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


    //used to make request to PHP server
    private class SearchUser extends AsyncTask<String, String , String > {
        String userId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            showProgressDialog("Updating..please wait..");
            userId = mAuth.getCurrentUser().getUid();
        }


        @Override
        protected String doInBackground(String... strings) {

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("userId", userId);
            String link = BASE_URL + "search_user.php";


            Set set = params.entrySet();
            Iterator iterator = set.iterator();

            try {
                //building up data in form of key1=value1&key2=value2.. which will be sent as POST request
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

                //to establish connection
                URL url=new URL(link);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Accept","*/*");
                OutputStream out = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

                Log.d("data", data);
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());


                String result = convertStreamToString(inputStream);
                httpURLConnection.disconnect();
                Log.d("TAG",result);
                return result;

            }
            catch(Exception e){

                Log.d("debug",e.getMessage());
                e.printStackTrace();

            }

            return null;

        }
        //helper function for converting the response from the server to JSON string
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
            //if string returned from doinbackground is null, that means Exception occurred while connection to server
            hideProgressDialog();
            if(s==null){
                Toast.makeText(MainActivity.this, "Could not connect to PHPServer", Toast.LENGTH_LONG).show();
                //if could not know whether the current user is student or teacher
                FirebaseAuth.getInstance().signOut();
            }
            else {

                //otherwise string would contain the JSON returned from php
                JSONParser parser = new JSONParser();
                try {

                    JSONObject  jsonObject = (JSONObject) parser.parse(s);
                    Object p = jsonObject.get("success");
                    int successCode = Integer.parseInt(p.toString());
                    if( successCode==0){

                        Toast.makeText(MainActivity.this, "Some error occurred", Toast.LENGTH_LONG).show();
                        //if could not know whether the current user is student or teacher
                        FirebaseAuth.getInstance().signOut();

                    }
                    else{

                        Object jsonUser = jsonObject.get("user");
                        String user = jsonUser.toString();
                        if(user.equals("null")){

                            Toast.makeText(MainActivity.this, "Unable to find records in mysql", Toast.LENGTH_SHORT).show();
                            showProgressDialog("Logging you out..");
                            FirebaseAuth.getInstance().signOut();

                        }
                        else if(user.equals("student")){

                            //To update the shared preference record
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("details", 0); //Mode_private
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("user", "student");
                            editor.apply();

                            Intent intent = new Intent(MainActivity.this, StudentActivity.class);
                            startActivity(intent);
                            finish();

                        }
                        else if(user.equals("teacher")){

                            SharedPreferences pref = getApplicationContext().getSharedPreferences("details", 0); //Mode_private
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("user", "teacher");
                            editor.apply();
                            //this checks on login, if Teacher has provided the subjects
                            checkIfSubjectProvided();

                            Intent intent = new Intent(MainActivity.this, TeacherActivity.class);
                            startActivity(intent);
                            finish();

                        }
                    }
                }catch(ParseException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
