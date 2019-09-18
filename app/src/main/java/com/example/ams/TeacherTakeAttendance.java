package com.example.ams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TeacherTakeAttendance extends BaseActivity {
    private TextView displaySubjectCode, displayGroup, displaySubjectName;
    private Button generateQr, lessThan75, generatePdf, backToDashboard;
    private static int QRCodeWidth = 500;
    private final String BASE_URL = "https://amscollege.000webhostapp.com/";
    //private final String BASE_URL = "http://192.168.43.99:1234/ams/";
    private Bitmap bitmap;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    //Boolean isLocationPermissionGranted = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_take_attendance);
        //getting intent data from previous activity (TeacherActivity)
        TeacherSubjectDetail teacherSubjectDetail = (TeacherSubjectDetail) getIntent().getSerializableExtra("TeacherSubjectDetail");
        displaySubjectCode = (TextView)findViewById(R.id.displaySubjectCode);
        displayGroup = (TextView)findViewById(R.id.displayGroup);
        displaySubjectName = (TextView)findViewById(R.id.subjectName);

        generateQr = (Button)findViewById(R.id.scanQrCode);
        lessThan75 = (Button)findViewById(R.id.lessAttendance);
        generatePdf = (Button) findViewById(R.id.generatePDF);
        backToDashboard = (Button)findViewById(R.id.backToDashboard);

        if (ContextCompat.checkSelfPermission( TeacherTakeAttendance.this,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {

            ActivityCompat.requestPermissions(TeacherTakeAttendance.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION );

        }

        if(teacherSubjectDetail!=null) {
            displaySubjectCode.setText(teacherSubjectDetail.getSubjectCode());
            displayGroup.setText(teacherSubjectDetail.getBranch());
            displaySubjectName.setText(teacherSubjectDetail.getSubjectName());
        }

        generateQr.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //to refresh the flush field to note the current present absent for the desired subject
                if (ContextCompat.checkSelfPermission(TeacherTakeAttendance.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(), "You need to provide the location access Permission", Toast.LENGTH_LONG).show();
                }
                else {
                    RefreshTheFlushField refreshTheFlushField = new RefreshTheFlushField();
                    refreshTheFlushField.execute(displayGroup.getText().toString(), displaySubjectCode.getText().toString());
                }

            }
        });

        generatePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {

                    Toast.makeText(getApplicationContext(),"You are not online!!!!",Toast.LENGTH_LONG).show();
                }else {
                    GeneratePdf generatePdf = new GeneratePdf();
                    generatePdf.execute();
                }
//                Intent intent = new Intent(TeacherTakeAttendance.this, PdfViewer.class);
//                intent.putExtra("group", displayGroup.getText().toString());
//                intent.putExtra("subject", displaySubjectCode.getText().toString());
//                startActivity(intent);
            }
        });


        lessThan75.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherTakeAttendance.this, DisplayShortAttendance.class);
                intent.putExtra("group", displayGroup.getText().toString());
                intent.putExtra("subject", displaySubjectCode.getText().toString());
                startActivity(intent);
            }
        });

        backToDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherTakeAttendance.this, TeacherActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    //inorder to refresh the flush field of the database
    private class RefreshTheFlushField extends AsyncTask<String, String , String >{

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
            params.put("table_name",  "table_" + strings[0].toLowerCase().trim());
            params.put("subjectCode_tc", strings[1].toLowerCase().trim() + "_tc");
            String link = BASE_URL + "refresh_the_flush_field.php";

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
            if(s==null){
                Toast.makeText(TeacherTakeAttendance.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
            }
            else {

                //otherwise string would contain the JSON returned from php
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
                if(jsonObject!=null && successCode==0){
                    Toast.makeText(TeacherTakeAttendance.this, "Unable to connect to server", Toast.LENGTH_LONG).show();
                }
                else {
                    GenerateQr generateQr = new GenerateQr();
                    generateQr.execute();
                }
            }
        }
    }


    private Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRCodeWidth, QRCodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.black):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_8888);

        bitmap.setPixels(pixels, 0, QRCodeWidth, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }
    //class to generate QR Code
    class GenerateQr extends AsyncTask<String, String, String>{
        String subject, branch;
        String getCurrentDate , getCurrentTime;
        String latitude = "", longitude="";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Generating QR Code..Please Wait");
            subject = displaySubjectCode.getText().toString();
            branch = displayGroup.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
            getCurrentDate = sdf.format(new Date());
            SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
            getCurrentTime = sdf1.format(new Date());

            GPSTracker gps = new GPSTracker(TeacherTakeAttendance.this);

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

            Log.d("DIS", latitude + " " + longitude);
        }

        @Override
        protected String doInBackground(String... strings) {
            JSONObject credentials = new JSONObject();


            try{
                credentials.put("subject", subject);
                credentials.put("group", branch);
                credentials.put("latitude", latitude);
                credentials.put("longitude", longitude);
                credentials.put("date", getCurrentDate);
                credentials.put("time", getCurrentTime);
            }catch (JSONException e){
                e.printStackTrace();
            }
            String value = credentials.toString();
            try {
                bitmap = TextToImageEncode(value);
                Intent intent = new Intent(TeacherTakeAttendance.this, QRCodeGenerator.class);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                intent.putExtra("bitmap", byteArray);
                intent.putExtra("subject", subject);
                intent.putExtra("group", branch);
                startActivity(intent);
            } catch (WriterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            hideProgressDialog();
        }
    }

    private class GeneratePdf extends AsyncTask<String, String , String >{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            showProgressDialog("Generating pdf\n..please wait..");
        }


        @Override
        protected String doInBackground(String... strings) {
            ///ContentValues params = new ContentValues();
            //to get the current date of attendance
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");

            String currentDate = simpleDateFormat.format(calendar.getTime());
            //Toast.makeText(getApplicationContext(), currentDate, Toast.LENGTH_LONG).show();
            Log.d("TAG", currentDate);
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("groupName",  displayGroup.getText().toString());
            params.put("subjectCode", displaySubjectCode.getText().toString());
            String link = BASE_URL + "generate_pdf.php";

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
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Accept","*/*");
                OutputStream out = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

                Log.d("data", data);
                return data;
            }
            catch(Exception e){
                Log.d("debug",e.getMessage());
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            hideProgressDialog();
            //if string returned from doinbackground is null, that means Exception occured while connectioon to server
            if(s==null){
                Toast.makeText(TeacherTakeAttendance.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
            }
            else {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_URL + "generate_pdf.php"+"?" + s));
                    startActivity(browserIntent);
                    Toast.makeText(getApplicationContext(), "Pdf Downloading", Toast.LENGTH_LONG).show();
            }
        }
    }
}
