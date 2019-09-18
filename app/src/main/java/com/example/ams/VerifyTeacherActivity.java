package com.example.ams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class VerifyTeacherActivity extends BaseActivity {

    private final String BASE_URL = "https://amscollege.000webhostapp.com/";
    //private final String BASE_URL = "http://192.168.43.99:1234/ams/";
    private RecyclerView recyclerView;

    ArrayList<TeacherDetails> teacherDetailsArrayList= new ArrayList<>();
    private TeacherDetailAdapter teacherDetailAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_teacher);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        GetUnverifiedTeachers getUnverifiedTeachers = new GetUnverifiedTeachers();
        getUnverifiedTeachers.execute();


        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                final Dialog dialog = new Dialog(VerifyTeacherActivity.this);
                dialog.setContentView(R.layout.teacher_dialog);
                dialog.setTitle("Details :");
                dialog.setCancelable(false);
                TextView nameTextView = (TextView) dialog.findViewById(R.id.nameTextView);
                TextView emailIdTextView = (TextView)dialog.findViewById(R.id.emailIdTextView);
                TextView teacherIdTextView = (TextView)dialog.findViewById(R.id.teacherIdTextView);
                TextView phoneNoTextView = (TextView) dialog.findViewById(R.id.phoneNoTextView);

                nameTextView.setText(teacherDetailsArrayList.get(position).getName());
                emailIdTextView.setText(teacherDetailsArrayList.get(position).getEmailId());
                teacherIdTextView.setText(teacherDetailsArrayList.get(position).getTeacherId());
                phoneNoTextView.setText(teacherDetailsArrayList.get(position).getPhoneNo());

                Button approveButton = (Button) dialog.findViewById(R.id.approveButton);
                Button disApproveButton = (Button) dialog.findViewById(R.id.disApproveButton);
                // if button is clicked, close the custom dialog
                final String teacherID = teacherDetailsArrayList.get(position).getTeacherId();
                Log.d("id", teacherID);
                final int pos = position;
                approveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {

                            Toast.makeText(getApplicationContext(),"You are not online!!!!",Toast.LENGTH_LONG).show();
                        }else {
                            final String teacherId = teacherDetailsArrayList.get(pos).getTeacherId();
                            VerifyTeacher verifyTeacher = new VerifyTeacher();
                            verifyTeacher.execute(teacherID);
                            teacherDetailsArrayList.remove(pos);
                            teacherDetailAdapter.notifyDataSetChanged();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tokens");
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                                        Log.d("TAG", childSnapshot.toString());
                                        if(childSnapshot.getKey().equals(teacherId)){
                                            String token = childSnapshot.getValue().toString();
                                            Log.d("TAG", token);
                                            try {
                                                FireMessage fm = new FireMessage("New Request For Verification", "Your Requested is approved!!");
                                                fm.sendToToken(token);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            dialog.dismiss();
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });



                        }
                    }
                });
                disApproveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                if(!isFinishing())
                    dialog.show();

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


    }

    private class GetUnverifiedTeachers extends AsyncTask<String, String , String > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            showProgressDialog("Retrieving..please wait..");
        }


        @Override
        protected String doInBackground(String... strings) {
            String link = BASE_URL + "get_unverified_teacher_details.php";

            try {
                String data = "";

                URL url=new URL(link);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Accept","*/*");
                OutputStream out = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

                //Log.d("data", data);
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
            hideProgressDialog();
            //if string returned from doinbackground is null, that means Exception occured while connectioon to server
            if(s==null){
                Toast.makeText(VerifyTeacherActivity.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
            }
            else {

                //otherwise string would contain the JSON returned from php
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = null;
                try {

                    //jsonObject = (JSONObject) parser.parse(s);
                    org.json.JSONArray jb= new org.json.JSONArray(s);
                    org.json.JSONObject job1 = (org.json.JSONObject) jb.getJSONObject(0);
                    org.json.JSONObject job2 = (org.json.JSONObject) jb.getJSONObject(1);
                    org.json.JSONObject job3 = (org.json.JSONObject) jb.getJSONObject(2);
                    org.json.JSONObject job4 = (org.json.JSONObject) jb.getJSONObject(3);
                    org.json.JSONObject job5 = (org.json.JSONObject) jb.getJSONObject(4);
                    org.json.JSONArray st1 = job1.getJSONArray("name");
                    org.json.JSONArray st2 = job2.getJSONArray("teacherId");
                    org.json.JSONArray st3 = job3.getJSONArray("emailId");
                    org.json.JSONArray st4 = job4.getJSONArray("phoneNo");
                    org.json.JSONArray st5 = job5.getJSONArray("verified");
                    Log.d("len", Integer.toString(st1.length()));
                    for(int i=0;i<st1.length();i++){
                        //Toast.makeText(VerifyTeacherActivity.this, "Made it ", Toast.LENGTH_LONG).show();
                        TeacherDetails teacherDetails = new TeacherDetails(st1.getString(i), st3.getString(i), st2.getString(i), st4.getString(i), st5.getString(i));
                        teacherDetailsArrayList.add(teacherDetails);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("list", teacherDetailsArrayList.toString());
                teacherDetailAdapter = new TeacherDetailAdapter(getApplicationContext(), teacherDetailsArrayList);
                recyclerView.setAdapter(teacherDetailAdapter);
            }
        }
    }

    private class VerifyTeacher extends AsyncTask<String, String , String > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            showProgressDialog("Updating..please wait..");
        }


        @Override
        protected String doInBackground(String... strings) {
            ///ContentValues params = new ContentValues();

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("teacherId", strings[0].trim());

            String link = BASE_URL + "verify_teacher.php";

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
                Toast.makeText(VerifyTeacherActivity.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
                //if could not know whether the current user is student or teacher
                FirebaseAuth.getInstance().signOut();
            }
            else {

                    JSONParser parser = new JSONParser();
                    org.json.simple.JSONObject jsonObject = null;
                    try {
                        jsonObject = (org.json.simple.JSONObject) parser.parse(s);
                    }catch(ParseException e){
                        e.printStackTrace();
                    }
                    int successCode = 0;
                    if(jsonObject!=null) {
                        Object p = jsonObject.get("success");
                        successCode = Integer.parseInt(p.toString());
                    }
                    if(jsonObject==null || successCode==0){
                        Toast.makeText(VerifyTeacherActivity.this, "Unable to connect to server", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Teacher Verified!", Toast.LENGTH_LONG).show();
                    }

            }
        }
    }
}
