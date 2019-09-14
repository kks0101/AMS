package com.example.ams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
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

public class MainActivity extends BaseActivity implements View.OnClickListener{
    private EditText emailField, passwordField;
    Button loginButton, studentSignUpButton, teacherSignUpButton;
    private FirebaseAuth mAuth;

    private final String BASE_URL = "http://192.168.43.99:1234/ams/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            Intent intent = new Intent(this, TeacherActivity.class);
            startActivity(intent);
            finish();
        }

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
        int result = 0;
        showProgressDialog("Logging You in...");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d("Debug", "Successfully Logged in");
                            SearchUser searchUser = new SearchUser();
                            searchUser.execute();
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
            ///ContentValues params = new ContentValues();

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("userId", userId);

            String link = BASE_URL + "search_user.php";

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
                //teacherId is defined in sql as primary key
                //so if any user login with the same teacherId, delete this already created user in Firebase


                return result;
            }
            catch(Exception e){
                Log.d("debug",e.getMessage());
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
            //if string returned from doinbackground is null, that means Exception occured while connectioon to server
            hideProgressDialog();
            if(s==null){
                Toast.makeText(MainActivity.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
                //if could not know whether the current user is student or teacher
                FirebaseAuth.getInstance().signOut();
            }
            else {

                //otherwise string would contain the JSON returned from php
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = null;
                try {
                    jsonObject = (JSONObject) parser.parse(s);
                }catch(ParseException e){
                    e.printStackTrace();
                }
                int successCode = 0;
                if(jsonObject!=null) {
                    Object p = jsonObject.get("success");
                    successCode = Integer.parseInt(p.toString());
                }
                if( jsonObject!=null && successCode==0){
                    Toast.makeText(MainActivity.this, "Some error occurred", Toast.LENGTH_LONG).show();
                    //if could not know whether the current user is student or teacher
                    FirebaseAuth.getInstance().signOut();
                }
                else{
                    Object jsonUser = jsonObject.get("user");
                    String user = jsonUser.toString();
                    if(user.equals("null")){
                        Toast.makeText(MainActivity.this, "Unable to find records in mysql", Toast.LENGTH_SHORT).show();
                        showProgressDialog("Logging you.. out");
                        FirebaseAuth.getInstance().signOut();
                    }
                    else if(user.equals("student")){
                        Intent intent = new Intent(MainActivity.this, StudentActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else if(user.equals("teacher")){
                        Intent intent = new Intent(MainActivity.this, TeacherActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }
    }
}
