package com.example.ams;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.Map;
import java.util.Set;

public class DisplayShortAttendance extends BaseActivity {

    private final String BASE_URL = "http://192.168.43.99:1234/ams/";
    ArrayList<HashMap<String, String>> shortList = new ArrayList<>();
    ListView listView;
    ArrayList<ShortAttendanceDetail> shortAttendanceDetailArrayList = new ArrayList<>();
    private ShortAttendanceListAdapter shortAttendanceListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_short_attendance);
        listView = (ListView)findViewById(R.id.listView);
        String subjectCode = getIntent().getStringExtra("subject");
        String group = getIntent().getStringExtra("group");

        GetListLessThan75 getListLessThan75 = new GetListLessThan75();
        getListLessThan75.execute(group, subjectCode);


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
            ///ContentValues params = new ContentValues();

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
                        Toast.makeText(DisplayShortAttendance.this, "Made it ", Toast.LENGTH_LONG).show();
                        ShortAttendanceDetail shortAttendanceDetail = new ShortAttendanceDetail(st1.getString(i), st3.getString(i), st2.getDouble(i));
                        shortAttendanceDetailArrayList.add(shortAttendanceDetail);
                        //HashMap<String , String> map = new HashMap<>();
                        //map.put("name", st1.getString(i));
                        //map.put("percent", st2.getString(i));
                        //map.put("emailId", st3.getString(i));
                        //shortList.add(map);
                        //subjectList.add(st1.getString(i) + "-" + st2.getString(i));
                        Log.d("TAG", st1.getString(i)+ st3.getString(i)+ st2.getDouble(i));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("TAG", shortAttendanceDetailArrayList.toString());
                shortAttendanceListAdapter = new ShortAttendanceListAdapter(getApplicationContext(), shortAttendanceDetailArrayList);
                listView.setAdapter(shortAttendanceListAdapter);
            }
        }
    }
}
