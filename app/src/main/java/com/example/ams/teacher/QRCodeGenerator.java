package com.example.ams.teacher;

import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ams.others.AppStatus;
import com.example.ams.others.BaseActivity;
import com.example.ams.R;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This is the activity which displays the QRCode. This QRCode is obtained after the teacher clicks
 * "Take Attendance" Button in previous Activity. The QR image is send in form of byte Array via Intent.
 * This byte Array is converted into Bitmap, which is then rendered in ImageView.
 *
 * This Activity is provided with a Back Button and "Finish Attendance" button. Whenever teacher clicks on
 * Finish Attendance button, the backend is configured such that the attendance is recorded successfully.
 *
 * In case, teacher clicks back Button or press phone back button, it is assumed that teacher does not want to take attendance.
 * In this case, attendance record is reset, to avoid inconsistency in attendance record.
 */

public class QRCodeGenerator extends BaseActivity implements View.OnClickListener{

    private ImageView qrCodeImage;
    private Button finishTakingAttendance;
    private ImageButton backButton;
    private String subjectCode, groupName;

    private final String BASE_URL = "https://amscollege.000webhostapp.com/";
    //private final String BASE_URL = "http://192.168.43.99:1234/ams/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_generator);

        qrCodeImage = (ImageView)findViewById(R.id.qrCodeImage);
        finishTakingAttendance = (Button) findViewById(R.id.finishQrCode);
        backButton = (ImageButton)findViewById(R.id.back_Button);

        //getting QR Image in form of Byte Array
        Intent intent = getIntent();
        byte [] byteArray = intent.getByteArrayExtra("bitmap");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        qrCodeImage.setImageBitmap(bmp);

        backButton.setOnClickListener(this);

        subjectCode = intent.getStringExtra("subject");
        groupName = intent.getStringExtra("group");

        finishTakingAttendance.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {
            Toast.makeText(getApplicationContext(),"You are not online!!!!",Toast.LENGTH_LONG).show();
            return;
        }

        //if finish taking attendance button is clicked, successfully record the attendance and
        //direct teacher to a activity that displays the stats of that class(total present, total absent,
        //list of absentees)
        if(view == view.findViewById(R.id.finishQrCode)){

            //showing an alert dialog box
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage("Finish Taking Attendance? This cant be undone.");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), "Attendance Successfully taken", Toast.LENGTH_LONG).show();

                    //Successfully storing the attendance record: stores server side code, so must be done on different thread
                    AlterTable alterTable = new AlterTable();
                    alterTable.execute();
                    Intent intent = new Intent(QRCodeGenerator.this, TeacherAfterAttendance.class);
                    intent.putExtra("subjectCode", subjectCode);
                    intent.putExtra("groupName", groupName);
                    startActivity(intent);
                    finish();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //if user select "No", just cancel this dialog and continue with app
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else if (view == view.findViewById(R.id.back_Button)){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage("All attendance will be lost. Do You want to exit?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //delete all the attendance i.e refresh the flush field in the attedance entry of current subject
                    ResetCount resetCount = new ResetCount();
                    String group_name = getIntent().getStringExtra("group");
                    String subjectCode = getIntent().getStringExtra("subject");
                    resetCount.execute(group_name, subjectCode);
                    Intent intent = new Intent(QRCodeGenerator.this, TeacherActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //if user select "No", just cancel this dialog and continue with app
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();


        }
    }

    //alert to show dialog box: if user clicks on Ok (exits) by pressing back button whole attendance will be lost
    //else finish Taking attendance button is provided
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("All attendance will be lost. Do You want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //delete all the attendance i.e refresh the flush field in the attedance entry of current subject
                ResetCount resetCount = new ResetCount();
                String group_name = getIntent().getStringExtra("group");
                String subjectCode = getIntent().getStringExtra("subject");
                resetCount.execute(group_name, subjectCode);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private class AlterTable extends AsyncTask<String, String , String >{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            showProgressDialog("Finishing attendance\n..please wait..");
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
            params.put("groupName",  groupName.toLowerCase().trim());
            params.put("subjectCode", subjectCode.toLowerCase().trim());
            params.put("column_name", "_"+currentDate);
            String link = BASE_URL + "update_post_attendance.php";

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
            if(s==null){
                Toast.makeText(QRCodeGenerator.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(QRCodeGenerator.this, "Unable to connect to server", Toast.LENGTH_LONG).show();
                }
                else {
                    finish();
                }
            }
        }
    }



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
                Toast.makeText(QRCodeGenerator.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(QRCodeGenerator.this, "Unable to connect to server", Toast.LENGTH_LONG).show();
                }
                else {
                    finish();
                }
            }
        }
    }

    private class ResetCount extends AsyncTask<String, String , String >{

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
            params.put("subjectCode_tp", strings[1].toLowerCase().trim() + "_tp");
            String link = BASE_URL + "reset_count.php";

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
                Toast.makeText(QRCodeGenerator.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
            }
            else {

                //otherwise string would contain the JSON returned from php
                JSONParser parser = new JSONParser();
                try {
                    JSONObject jsonObject = (org.json.simple.JSONObject) parser.parse(s);
                    Object p = jsonObject.get("success");
                    int successCode = Integer.parseInt(p.toString());

                    if(successCode==0){
                        Toast.makeText(QRCodeGenerator.this, "Unable to connect to server", Toast.LENGTH_LONG).show();
                    }
                    else {
                        finish();
                    }
                }catch(ParseException e){
                    e.printStackTrace();
                }
            }
        }
    }

}
