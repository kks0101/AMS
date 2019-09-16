package com.example.ams;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;

import org.json.JSONException;
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
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import javax.security.auth.Subject;

public class StudentActivity extends BaseActivity {
    private final String BASE_URL = "http://192.168.43.99:1234/ams/";
    private FirebaseAuth mAuth;
    private List<String> subjectList = new ArrayList<>();
    private TextView detailTextView;
    private ListView listView;
    private IntentIntegrator qrScan;
    private Button scanQrCodeButton;

    private RecyclerView recyclerView;
    ArrayList<TeacherSubjectDetail> recievedList = new ArrayList<>();
    LinearLayout linlaHeaderProgress;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS= 1;
    String name , regNo, emailId, branch, semester, phoneNo,groupName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        linlaHeaderProgress = (LinearLayout) findViewById(R.id.progressBar);
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        scanQrCodeButton = (Button) findViewById(R.id.giveAttendance);

        if (ContextCompat.checkSelfPermission( StudentActivity.this,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {

            ActivityCompat.requestPermissions(StudentActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION );

        }
        if (ContextCompat.checkSelfPermission( StudentActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED )
        {

            ActivityCompat.requestPermissions(StudentActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        }

        ImageButton profileButton = (ImageButton)findViewById(R.id.profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentActivity.this, StudentProfile.class);
                startActivity(intent);
            }
        });


        mAuth = FirebaseAuth.getInstance();



        GetProfileDetails getProfileDetails = new GetProfileDetails();
        getProfileDetails.execute();

        qrScan = new IntentIntegrator(this);
        scanQrCodeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(StudentActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(), "You need to provide the location access Permission", Toast.LENGTH_LONG).show();
                }
                else {
                    if (ContextCompat.checkSelfPermission( StudentActivity.this,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ){
                        Toast.makeText(getApplicationContext(), "You need to provide Permission to read phone state", Toast.LENGTH_LONG).show();
                    }
                    else {
                        qrScan.initiateScan();
                    }
                }

            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String deviceId = null;
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ContextCompat.checkSelfPermission( StudentActivity.this, Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED ){
                    Toast.makeText(getApplicationContext(), "Permission required to read phone state!", Toast.LENGTH_LONG).show();
                }
                else{
                    try {
                        //uniquely identifies phone
                        deviceId = telephonyManager.getDeviceId();
                    }
                    catch(SecurityException e){
                        Log.i("PERMISSION", e.toString());
                    }
                    Log.d("TAG", deviceId);
                        GetSpecificDetails getSpecificDetails = new GetSpecificDetails();
                        getSpecificDetails.execute(deviceId, groupName, recievedList.get(position).getSubjectCode());
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission( StudentActivity.this,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {

            ActivityCompat.requestPermissions(StudentActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION );

        }
        if (ContextCompat.checkSelfPermission( StudentActivity.this,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {

            ActivityCompat.requestPermissions(StudentActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        }
    }

    private double distanceLoc(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    //to get the result after scan
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                //if qr contains data
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(result.getContents());
                    String subjectCode = obj.getString("subject");
                    String groupName = obj.getString("group");
                    String long1 = obj.getString("longitude");
                    String lat1 = obj.getString("latitude");
                    String dateObtained = obj.getString("date");
                    String timeObtained = obj.getString("time");
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    String longitude="", latitude="";

                    GPSTracker gps = new GPSTracker(StudentActivity.this);

                    if(gps.canGetLocation()){
                        latitude = Double.toString(gps.getLatitude());
                        longitude = Double.toString(gps.getLongitude());
                        // \n is for new line
                    }else{
                        // can't get location
                        // GPS or Network is not enabled
                        // Ask user to enable GPS/network in settings
                        gps.showSettingsAlert();
                    }
                    Location location1 = new Location("");
                    location1.setLongitude(Double.parseDouble(long1));
                    location1.setLatitude(Double.parseDouble(lat1));
                    Location location2 = new Location("");
                    location2.setLongitude(Double.parseDouble(longitude));
                    location2.setLatitude(Double.parseDouble(latitude));
                    double distance = distanceLoc(Double.parseDouble(lat1), Double.parseDouble(long1), Double.parseDouble(latitude), Double.parseDouble(longitude))*1000;
                    //double distance = location1.distanceTo(location2);
                    int valid = 1;
                    Log.d("DIS", Double.toString(distance) + " " + latitude +" "+ longitude);
                    if(distance>=5.0000){
                        valid =0;
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
                    String getCurrentDate = sdf.format(new Date());
                    if(!getCurrentDate.equals(dateObtained))
                        valid = 0;
                    SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
                    String getCurrentTime = sdf1.format(new Date());
                    //Date date1  = sdf1.parse(getCurrentTime);
                    Log.d("DIS", Double.toString(distance) + " " + latitude +" "+ longitude +" " +  getCurrentDate + " "+getCurrentTime);
                    if(valid==1) {
                        GiveAttendance giveAttendance = new GiveAttendance();
                        giveAttendance.execute(subjectCode.toLowerCase().trim(), groupName.toLowerCase().trim());
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Your Attendance could not be updated\nCredentials mismatch", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class GiveAttendance extends AsyncTask<String, String , String > {
        String deviceId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            showProgressDialog("Updating..please wait..");
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
            params.put("deviceId", deviceId);
            params.put("table_name", "table_" + strings[1].toLowerCase().trim());

            params.put("subjectCode_tp", strings[0].toLowerCase().trim() + "_tp");
            params.put("subjectCode_tc", strings[0].toLowerCase().trim() + "_tc");
            Log.d("TAG", strings[0].toLowerCase().trim() + "_tp" + strings[1].toLowerCase().trim());
            String link = BASE_URL + "give_attendance.php";

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


            //otherwise string would contain the JSON returned from php
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) parser.parse(s);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int successCode = 0;
            if (jsonObject != null) {
                Object p = jsonObject.get("success");
                successCode = Integer.parseInt(p.toString());
            }
            if (jsonObject != null && successCode == 0) {
                Toast.makeText(StudentActivity.this, "Some error occurred", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Attendance updated", Toast.LENGTH_LONG).show();
            }

        }
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

            String link = BASE_URL + "get_student_details.php";

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
                Toast.makeText(StudentActivity.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
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
                if( jsonObject==null || successCode==0){
                    Toast.makeText(StudentActivity.this, "Some error occurred", Toast.LENGTH_LONG).show();
                    //if could not know whether the current user is student or teacher

                }
                else{
                    name = jsonObject.get("name").toString();
                    regNo = jsonObject.get("regNo").toString();
                    emailId = jsonObject.get("emailId").toString();
                    branch = jsonObject.get("branch").toString();
                    semester = jsonObject.get("semester").toString();
                    phoneNo = jsonObject.get("phoneNo").toString();
                    groupName = jsonObject.get("groupName").toString();

                    GetSubjectDetails getSubjectDetails = new GetSubjectDetails();
                    getSubjectDetails.execute(groupName);
                }
            }
        }
    }

    private class GetSubjectDetails extends AsyncTask<String, String , String > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            showProgressDialog("Getting Subjects..please wait..");
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
                Toast.makeText(StudentActivity.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
            } else {

                //otherwise string would contain the JSON returned from php
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = null;
                try {
                    //jsonObject = (JSONObject) parser.parse(s);
                    org.json.JSONArray jb= new org.json.JSONArray(s);
                    org.json.JSONObject job1 = (org.json.JSONObject) jb.getJSONObject(0);
                    org.json.JSONObject job2 = (org.json.JSONObject) jb.getJSONObject(1);
                    org.json.JSONArray st1 = job1.getJSONArray("subject_code");
                    org.json.JSONArray st2 = job2.getJSONArray("subject_name");
                    for(int i=0;i<st1.length();i++){
                        recievedList.add(new TeacherSubjectDetail(st1.getString(i), branch, st2.getString(i)));
                    }
                    linlaHeaderProgress.setVisibility(View.GONE);
                    StudentSubjectAdapter adapter = new StudentSubjectAdapter(getApplicationContext(), recievedList);

                    //setting adapter to recyclerview
                    recyclerView.setAdapter(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private class GetSpecificDetails extends AsyncTask<String, String , String > {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Retrieving details\n..please wait..");
        }


        @Override
        protected String doInBackground(String... strings) {
            ///ContentValues params = new ContentValues();

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("deviceId", strings[0].toLowerCase().trim());
            params.put("table_name",  "table_" + strings[1].toLowerCase().trim());
            params.put("subjectCode_tp", strings[2].toLowerCase().trim() + "_tp");
            params.put("subjectCode_tc", strings[2].toLowerCase().trim() + "_tc");

            String link = BASE_URL + "get_attendance_detail.php";

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
                Toast.makeText(StudentActivity.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
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
                if( jsonObject==null || successCode==0){
                    Toast.makeText(StudentActivity.this, "Some error occurred", Toast.LENGTH_LONG).show();
                    //if could not know whether the current user is student or teacher

                }
                else{
                    String total_present = jsonObject.get("total_present").toString();
                    String total_class = jsonObject.get("total_class").toString();

                    final Dialog dialog = new Dialog(StudentActivity.this);
                    dialog.setContentView(R.layout.student_dialog);
                    dialog.setTitle("Details :");

                    TextView totalClassTextView = (TextView) dialog.findViewById(R.id.totalClassTextView);
                    TextView totalPresentTextView = (TextView) dialog.findViewById(R.id.totalPresentTextView);
                    TextView totalAbsentTextView = (TextView) dialog.findViewById(R.id.totalAbsentTextView);
                    TextView moreToAttendTextView = (TextView) dialog.findViewById(R.id.moreToAttendTextView);
                    TextView percentTextView = (TextView) dialog.findViewById(R.id.percentTextView);

                    totalClassTextView.setText(total_class);
                    totalPresentTextView.setText(total_present);
                    totalAbsentTextView.setText(Integer.toString(Integer.parseInt(total_class) - Integer.parseInt(total_present)));
                    int totC = Integer.parseInt(total_class);
                    int totP = Integer.parseInt(total_present);
                    int d = 3*totC - 4*totP;
                    moreToAttendTextView.setText(Integer.toString(d));
                    double percent = (double)totP/totC;
                    DecimalFormat decimalFormat = new DecimalFormat("##.00");
                    String dec = decimalFormat.format(percent);
                    percentTextView.setText(dec + " %");
                    Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                    // if button is clicked, close the custom dialog
                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    if(!isFinishing())
                        dialog.show();

                }
            }
        }
    }
}
