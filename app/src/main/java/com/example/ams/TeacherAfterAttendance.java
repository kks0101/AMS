package com.example.ams;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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

/**
 * Activity to display the day's attendance details
 */


public class TeacherAfterAttendance extends BaseActivity {

    private TextView subjectCodeTextView, branchAttendanceTextView,subjectNameTextView, totalPresentTextView, totalAbsentTextView;
    private Button backToDashboard;
    private final String BASE_URL = "http://192.168.43.99:1234/ams/";
    private String subjectCode, groupName;
    private ListView listView;
    private ArrayList<ShortAttendanceDetail> attendanceDetails = new ArrayList<>();
    //using the same adapter as Short Attendance
    private ShortAttendanceListAdapter shortAttendanceListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_after_attendance);

        subjectCodeTextView = (TextView)findViewById(R.id.subjectCode);
        subjectNameTextView = (TextView)findViewById(R.id.subjectName);
        branchAttendanceTextView = (TextView)findViewById(R.id.branchAttendance);
        totalPresentTextView = (TextView) findViewById(R.id.totalPresent);
        totalAbsentTextView = (TextView)findViewById(R.id.totalAbsent);
        backToDashboard = (Button) findViewById(R.id.backToDashboard);
        listView = (ListView)findViewById(R.id.listView);


        subjectCode = getIntent().getStringExtra("subjectCode");
        groupName = getIntent().getStringExtra("groupName");


        GetCurrentAttendance getCurrentAttendance = new GetCurrentAttendance();
        getCurrentAttendance.execute(subjectCode, groupName);

        subjectCodeTextView.setText(subjectCode);
        branchAttendanceTextView.setText(groupName);

        backToDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherAfterAttendance.this, TeacherActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private class GetCurrentAttendance extends AsyncTask<String, String , String > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            showProgressDialog("Retrieving..please wait..");
        }


        @Override
        protected String doInBackground(String... strings) {
            ///ContentValues params = new ContentValues();

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("table_name",  "table_" + strings[0].toLowerCase().trim());
            params.put("subjectCode_tp", strings[1].toLowerCase().trim() + "_tp");
            params.put("subjectCode_tc", strings[1].toLowerCase().trim() + "_tc");
            String link = BASE_URL + "get_absentees.php";

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
            hideProgressDialog();
            //if string returned from doinbackground is null, that means Exception occured while connectioon to server
            if(s==null){
                Toast.makeText(TeacherAfterAttendance.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
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
                    org.json.JSONArray st1 = job1.getJSONArray("name");
                    org.json.JSONArray st2 = job2.getJSONArray("percent_attendance");
                    org.json.JSONArray st3 = job3.getJSONArray("emailId");

                    Log.d("TAG", Integer.toString(st1.length()));

                    for(int i=0;i<st1.length();i++){

                        Toast.makeText(TeacherAfterAttendance.this, "Made it ", Toast.LENGTH_LONG).show();
                        ShortAttendanceDetail shortAttendanceDetail = new ShortAttendanceDetail(st1.getString(i), st3.getString(i), st2.getDouble(i));
                        attendanceDetails.add(shortAttendanceDetail);

                        Log.d("TAG", st1.getString(i)+ st3.getString(i)+ st2.getDouble(i));

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("TAG", attendanceDetails.toString());

                shortAttendanceListAdapter = new ShortAttendanceListAdapter(getApplicationContext(), attendanceDetails);
                listView.setAdapter(shortAttendanceListAdapter);
            }
        }
    }
}
