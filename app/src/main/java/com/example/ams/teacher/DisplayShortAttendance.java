package com.example.ams.teacher;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ams.others.AppStatus;
import com.example.ams.others.BaseActivity;
import com.example.ams.R;

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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/*This Activity displays List of students having attendance less than 75 %.*/


public class DisplayShortAttendance extends BaseActivity {
    private final String BASE_URL = "https://amscollege.000webhostapp.com/";
    //private final String BASE_URL = "http://192.168.43.99:1234/ams/";


    private ListView listView;
    private ArrayList<ShortAttendanceDetail> shortAttendanceDetailArrayList = new ArrayList<>();
    private ShortAttendanceListAdapter shortAttendanceListAdapter;
    private String emailIdList = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_short_attendance);

        listView = (ListView)findViewById(R.id.listView);
        Button notify = (Button)findViewById(R.id.notifyEmail);

        //get subjectCode and group to display the short attendance particulars
        final String subjectCode = getIntent().getStringExtra("subject");
        final String group = getIntent().getStringExtra("group");

        new MaterialShowcaseView.Builder(this)
                .setTarget(notify)
                .setDismissText("GOT IT")
                .setContentText("Click to notify all the defaulters via MAIL")
                .build();
        //This provides teacher the facility to send email to all the students having short attendance
        notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {
                    Toast.makeText(getApplicationContext(),"You are not online!!!!",Toast.LENGTH_LONG).show();
                }
                else {
                    sendMail(subjectCode, group);
                }
            }
        });

        //Async Task class which retrieves the list of the students with attendance less than 75 %
        GetListLessThan75 getListLessThan75 = new GetListLessThan75();
        getListLessThan75.execute(group, subjectCode);

    }

    //method to send email via Intent
    private void sendMail(String subjectCode, String group) {
        //list of the short attendance students who are to be notified
        String recipientList = emailIdList;
        String[] recipients = recipientList.split(",");

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, subjectCode + " Short Attendance" );
        String email_body = "\tThis is to notify that your attendance is below 75%\nKindly attend clases or you will be debarred from examinations";
        intent.putExtra(Intent.EXTRA_TEXT, email_body);
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, "Choose an email client"));

    }


    //class to get the list of students having attendance less than 75 %
    private class GetListLessThan75 extends AsyncTask<String, String , String > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            showProgressDialog("Retrieving..please wait..");
        }


        @Override
        protected String doInBackground(String... strings) {

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("table_name",  "table_" + strings[0].toLowerCase().trim());
            params.put("subjectCode_tp", strings[1].toLowerCase().trim() + "_tp");
            params.put("subjectCode_tc", strings[1].toLowerCase().trim() + "_tc");
            String link = BASE_URL + "get_lessthan_75.php";

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
                Toast.makeText(DisplayShortAttendance.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
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
                        double percent = Double.parseDouble(st2.getString(i));
                        DecimalFormat decimalFormat = new DecimalFormat("##.00");
                        String dec = decimalFormat.format(percent);

                        //handling the recyclerView display via Custom Adapter
                        ShortAttendanceDetail shortAttendanceDetail = new ShortAttendanceDetail(st1.getString(i), st3.getString(i), Double.parseDouble(dec));
                        shortAttendanceDetailArrayList.add(shortAttendanceDetail);
                        //Email Id list of short attendance students (used in sending email)
                        emailIdList += st3.getString(i) + ",";

                        Log.d("TAG", st1.getString(i)+ st3.getString(i)+ st2.getDouble(i));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("TAG", shortAttendanceDetailArrayList.toString());
                //configuring the adapter to display the student list in recycler view
                shortAttendanceListAdapter = new ShortAttendanceListAdapter(getApplicationContext(), shortAttendanceDetailArrayList);
                listView.setAdapter(shortAttendanceListAdapter);
            }
        }
    }
}
