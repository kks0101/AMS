package com.example.ams;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.strictmode.IntentReceiverLeakedViolation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Text;

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

public class TeacherProfile extends BaseActivity {
    FirebaseAuth mAuth;
    private TextView nameTextView, emailIdTextView, teacherIdTextView, verifiedTextView, phoneNoTextView;
    private final String BASE_URL = "http://192.168.43.99:1234/ams/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_profile);


        mAuth = FirebaseAuth.getInstance();
        nameTextView = (TextView)findViewById(R.id.nameTextView);
        emailIdTextView = (TextView)findViewById(R.id.emailIdTextView);
        teacherIdTextView = (TextView)findViewById(R.id.teacherIdTextView);
        verifiedTextView = (TextView)findViewById(R.id.verifiedTextView);
        phoneNoTextView = (TextView)findViewById(R.id.phoneNoTextView);

        Button logoutButton = (Button) findViewById(R.id.logoutTeacher);
        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(TeacherProfile.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        GetProfileDetails getProfileDetails = new GetProfileDetails();
        getProfileDetails.execute();

    }

    private class GetProfileDetails extends AsyncTask<String, String , String > {
        String userId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Retrieving details\n..please wait..");
            userId = mAuth.getCurrentUser().getUid();
        }


        @Override
        protected String doInBackground(String... strings) {
            ///ContentValues params = new ContentValues();

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("userId", userId);

            String link = BASE_URL + "get_teacher_details.php";

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
                Toast.makeText(TeacherProfile.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
                //if could not know whether the current user is student or teacher
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
                    Toast.makeText(TeacherProfile.this, "Some error occurred", Toast.LENGTH_LONG).show();
                    //if could not know whether the current user is student or teacher

                }
                else{
                        String name = jsonObject.get("name").toString();
                        String teacherId = jsonObject.get("teacherId").toString();
                        String emailId = jsonObject.get("emailId").toString();
                        String phoneNo = jsonObject.get("phoneNo").toString();
                        String verified = jsonObject.get("verified").toString();

                        nameTextView.setText(name);
                        teacherIdTextView.setText(teacherId);
                        emailIdTextView.setText(emailId);
                        if(Integer.parseInt(verified) == 0) {
                            verifiedTextView.setText("You are not verified!");
                            verifiedTextView.setTextColor(Color.RED);
                        }
                        else{
                            verifiedTextView.setText("You are verified. :)");
                            verifiedTextView.setTextColor(Color.GREEN);
                        }
                        phoneNoTextView.setText(phoneNo);
                }
            }
        }
    }
}
